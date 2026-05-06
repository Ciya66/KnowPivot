package com.knowpivot.server.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.knowpivot.server.domain.entity.KnowledgeBase;
import com.knowpivot.server.domain.entity.KbMember;
import com.knowpivot.server.domain.enums.KbMemberRole;
import com.knowpivot.server.domain.repository.KnowledgeBaseRepository;
import com.knowpivot.server.domain.repository.KbMemberRepository;
import com.knowpivot.server.domain.repository.DocumentRepository;
import com.knowpivot.server.domain.service.KnowledgeDomainService;
import com.knowpivot.server.domain.service.KnowledgeSearchService;
import com.knowpivot.server.domain.valueobject.KnowledgeConfig;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.ResultCode;
import com.knowpivot.server.infrastructure.util.IdGenerator;
import com.knowpivot.server.interfaces.dto.request.CreateKnowledgeBaseRequest;
import com.knowpivot.server.interfaces.dto.request.UpdateKnowledgeBaseRequest;
import com.knowpivot.server.interfaces.dto.response.KnowledgeBaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库用例服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeApplicationService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KbMemberRepository kbMemberRepository;
    private final DocumentRepository documentRepository;
    private final KnowledgeDomainService knowledgeDomainService;
    private final KnowledgeSearchService knowledgeSearchService;
    private final IdGenerator idGenerator;

    /**
     * 创建知识库
     */
    @Transactional
    public KnowledgeBaseResponse create(CreateKnowledgeBaseRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        long kbId = idGenerator.nextId();

        KnowledgeBase kb = KnowledgeBase.builder()
                .id(kbId)
                .name(request.getName())
                .description(request.getDescription())
                .indexName(KnowledgeBase.generateIndexName(kbId))
                .config(request.getConfig() != null ? request.getConfig() : KnowledgeConfig.builder().build())
                .creatorId(userId)
                .isDeleted(0)
                .build();

        knowledgeBaseRepository.save(kb);

        // 创建 Redis 向量索引
        try {
            knowledgeSearchService.createIndex(kb.getIndexName());
        } catch (Exception e) {
            log.warn("创建向量索引失败，不影响知识库创建: indexName={}", kb.getIndexName(), e);
        }

        // 创建者自动成为管理员
        KbMember member = KbMember.builder()
                .id(idGenerator.nextId())
                .kbId(kbId)
                .userId(userId)
                .role(KbMemberRole.ADMIN)
                .build();
        kbMemberRepository.save(member);

        return toResponse(kb, 0L);
    }

    /**
     * 获取知识库列表
     */
    public PageResult<KnowledgeBaseResponse> list(int pageNum, int pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        List<KnowledgeBase> list = knowledgeBaseRepository.findByCreatorId(userId, pageNum, pageSize);
        long total = knowledgeBaseRepository.countByCreatorId(userId);

        List<KnowledgeBaseResponse> responses = list.stream()
                .map(kb -> {
                    long docCount = documentRepository.countByKbId(kb.getId());
                    return toResponse(kb, docCount);
                })
                .collect(Collectors.toList());

        return PageResult.of(total, responses);
    }

    /**
     * 获取知识库详情
     */
    public KnowledgeBaseResponse getDetail(Long kbId) {
        KnowledgeBase kb = knowledgeDomainService.getKnowledgeBaseOrThrow(kbId);
        long docCount = documentRepository.countByKbId(kbId);
        return toResponse(kb, docCount);
    }

    /**
     * 更新知识库
     */
    @Transactional
    public void update(Long kbId, UpdateKnowledgeBaseRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        knowledgeDomainService.checkPermission(kbId, userId);

        KnowledgeBase kb = knowledgeDomainService.getKnowledgeBaseOrThrow(kbId);
        kb.update(request.getName(), request.getDescription(), request.getConfig());
        knowledgeBaseRepository.updateById(kb);
    }

    /**
     * 删除知识库（逻辑删除）
     */
    @Transactional
    public void delete(Long kbId) {
        long userId = StpUtil.getLoginIdAsLong();
        knowledgeDomainService.checkPermission(kbId, userId);

        KnowledgeBase kb = knowledgeDomainService.getKnowledgeBaseOrThrow(kbId);

        // 删除 Redis 向量索引
        try {
            knowledgeSearchService.dropIndex(kb.getIndexName());
        } catch (Exception e) {
            log.warn("删除向量索引失败: indexName={}", kb.getIndexName(), e);
        }

        knowledgeBaseRepository.deleteById(kbId);
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase kb, long docCount) {
        return KnowledgeBaseResponse.builder()
                .kbId(kb.getId())
                .name(kb.getName())
                .description(kb.getDescription())
                .indexName(kb.getIndexName())
                .config(kb.getConfig())
                .creatorId(kb.getCreatorId())
                .docCount(docCount)
                .createTime(kb.getCreatedAt())
                .build();
    }
}
