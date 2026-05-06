package com.knowpivot.server.domain.service;

import com.knowpivot.server.domain.entity.Document;
import com.knowpivot.server.domain.enums.DocumentStatus;
import com.knowpivot.server.domain.repository.DocumentRepository;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 文档领域服务
 */
@Service
@RequiredArgsConstructor
public class DocumentDomainService {

    private final DocumentRepository documentRepository;

    /**
     * 获取文档并校验存在性
     */
    public Document getDocumentOrThrow(Long docId) {
        Document doc = documentRepository.findById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }
        return doc;
    }

    /**
     * 校验文档是否属于指定知识库
     */
    public void checkDocumentBelongsToKb(Long docId, Long kbId) {
        Document doc = getDocumentOrThrow(docId);
        if (!doc.getKbId().equals(kbId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "文档不属于该知识库");
        }
    }
}
