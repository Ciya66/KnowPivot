package com.knowpivot.server.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.knowpivot.server.domain.entity.Document;
import com.knowpivot.server.domain.entity.DocumentSegment;
import com.knowpivot.server.domain.entity.KnowledgeBase;
import com.knowpivot.server.domain.enums.DocumentStatus;
import com.knowpivot.server.domain.repository.DocumentRepository;
import com.knowpivot.server.domain.repository.DocumentSegmentRepository;
import com.knowpivot.server.domain.service.DocumentDomainService;
import com.knowpivot.server.domain.service.KnowledgeDomainService;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.ResultCode;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.kafka.DocumentParsingMessage;
import com.knowpivot.server.infrastructure.kafka.KafkaProducerService;
import com.knowpivot.server.infrastructure.minio.MinioStorageClient;
import com.knowpivot.server.infrastructure.redis.RedisVectorStore;
import com.knowpivot.server.infrastructure.util.IdGenerator;
import com.knowpivot.server.interfaces.dto.request.IndexingCallbackRequest;
import com.knowpivot.server.interfaces.dto.response.DocumentResponse;
import com.knowpivot.server.interfaces.dto.response.IndexingCallbackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档用例服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApplicationService {

    private final DocumentRepository documentRepository;
    private final DocumentSegmentRepository documentSegmentRepository;
    private final KnowledgeDomainService knowledgeDomainService;
    private final DocumentDomainService documentDomainService;
    private final MinioStorageClient minioStorageClient;
    private final KafkaProducerService kafkaProducerService;
    private final RedisVectorStore redisVectorStore;
    private final IdGenerator idGenerator;

    /**
     * 上传文档
     */
    @Transactional
    public DocumentResponse upload(Long kbId, MultipartFile file, String fileName) {
        long userId = StpUtil.getLoginIdAsLong();
        knowledgeDomainService.checkPermission(kbId, userId);

        KnowledgeBase kb = knowledgeDomainService.getKnowledgeBaseOrThrow(kbId);

        // 生成存储路径
        long docId = idGenerator.nextId();
        String objectName = "documents/" + kbId + "/" + docId + "_" + file.getOriginalFilename();

        // 上传到 MinIO
        minioStorageClient.uploadFile(objectName, file);

        // 保存文档记录
        Document doc = Document.builder()
                .id(docId)
                .kbId(kbId)
                .fileName(fileName != null ? fileName : file.getOriginalFilename())
                .storagePath(objectName)
                .fileSize(file.getSize())
                .status(DocumentStatus.UPLOADED)
                .chunkCount(0)
                .version(1)
                .isDeleted(0)
                .build();
        documentRepository.save(doc);

        // 发送 Kafka 消息触发文档解析
        DocumentParsingMessage message = DocumentParsingMessage.builder()
                .docId(doc.getId())
                .kbId(kbId)
                .storagePath(objectName)
                .fileName(doc.getFileName())
                .indexName(kb.getIndexName())
                .build();
        kafkaProducerService.sendDocumentParsingMessage(message);

        return toResponse(doc);
    }

    /**
     * 获取文档列表
     */
    public PageResult<DocumentResponse> list(Long kbId, Integer status, int pageNum, int pageSize) {
        List<Document> list = documentRepository.findByKbId(kbId, status, pageNum, pageSize);
        long total = documentRepository.countByKbId(kbId);
        List<DocumentResponse> responses = list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResult.of(total, responses);
    }

    /**
     * 删除文档 — 同时清理 Redis 向量和 MinIO 文件
     */
    @Transactional
    public void delete(Long docId) {
        Document doc = documentDomainService.getDocumentOrThrow(docId);

        // 清理 Redis 向量
        try {
            List<com.knowpivot.server.domain.entity.DocumentSegment> segments =
                    documentSegmentRepository.findByDocId(docId);
            for (var seg : segments) {
                redisVectorStore.deleteVector(seg.getVectorId());
            }
            documentSegmentRepository.deleteByDocId(docId);
        } catch (Exception e) {
            log.warn("清理 Redis 向量失败: docId={}", docId, e);
        }

        // 清理 MinIO 文件
        try {
            minioStorageClient.deleteFile(doc.getStoragePath());
        } catch (Exception e) {
            log.warn("清理 MinIO 文件失败: path={}", doc.getStoragePath(), e);
        }

        documentRepository.deleteById(docId);
        log.info("文档已删除: docId={}", docId);
    }

    /**
     * Python 解析完成回调 — 存储向量到 Redis，保存切片记录到数据库，更新文档状态
     */
    @Transactional
    public IndexingCallbackResponse indexingCallback(IndexingCallbackRequest request) {
        Document doc = documentDomainService.getDocumentOrThrow(request.getDocId());
        if (doc.getStatus() != DocumentStatus.PARSING) {
            throw new BusinessException(ResultCode.BUSINESS_PROCESS_FAILED,
                    "文档状态不是解析中，无法接收索引回调: docId=" + request.getDocId()
                            + ", status=" + doc.getStatus());
        }

        List<RedisVectorStore.VectorData> vectors = new ArrayList<>();
        List<DocumentSegment> segments = new ArrayList<>();

        for (IndexingCallbackRequest.ChunkData chunk : request.getChunks()) {
            String vectorId = request.getDocId() + ":" +
                    String.format("%06d", chunk.getPageNum() != null ? chunk.getPageNum() : 0);

            float[] embeddingArray = new float[chunk.getEmbedding().size()];
            for (int i = 0; i < chunk.getEmbedding().size(); i++) {
                embeddingArray[i] = chunk.getEmbedding().get(i);
            }

            vectors.add(RedisVectorStore.VectorData.builder()
                    .vectorId(vectorId)
                    .content(chunk.getContent())
                    .kbId(String.valueOf(request.getKbId()))
                    .docId(String.valueOf(request.getDocId()))
                    .pageNum(chunk.getPageNum())
                    .embedding(embeddingArray)
                    .build());

            segments.add(DocumentSegment.builder()
                    .id(idGenerator.nextId())
                    .docId(request.getDocId())
                    .kbId(request.getKbId())
                    .vectorId(vectorId)
                    .content(chunk.getContent())
                    .pageNum(chunk.getPageNum())
                    .build());
        }

        redisVectorStore.storeVectors(request.getIndexName(), vectors);
        documentSegmentRepository.saveBatch(segments);

        doc.markAsIndexed(request.getChunks().size());
        documentRepository.updateById(doc);

        log.info("文档索引完成: docId={}, chunks={}", request.getDocId(), request.getChunks().size());

        return IndexingCallbackResponse.builder()
                .docId(request.getDocId())
                .chunkCount(request.getChunks().size())
                .status("INDEXED")
                .build();
    }

    private DocumentResponse toResponse(Document doc) {
        return DocumentResponse.builder()
                .docId(doc.getId())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .status(doc.getStatus().getCode())
                .chunkCount(doc.getChunkCount())
                .createTime(doc.getCreatedAt())
                .updateTime(doc.getUpdatedAt())
                .build();
    }
}
