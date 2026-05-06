package com.knowpivot.server.domain.service;

import com.knowpivot.server.domain.entity.KnowledgeBase;
import com.knowpivot.server.domain.repository.KnowledgeBaseRepository;
import com.knowpivot.server.domain.repository.KbMemberRepository;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 知识库领域服务 - 处理跨聚合的业务规则
 */
@Service
@RequiredArgsConstructor
public class KnowledgeDomainService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KbMemberRepository kbMemberRepository;

    /**
     * 校验用户是否有知识库访问权限
     */
    public void checkPermission(Long kbId, Long userId) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 创建者直接拥有权限
        if (kb.getCreatorId().equals(userId)) {
            return;
        }
        // 非创建者需要是成员
        if (!kbMemberRepository.existsByKbIdAndUserId(kbId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 校验知识库是否存在且可用
     */
    public KnowledgeBase getKnowledgeBaseOrThrow(Long kbId) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识库不存在");
        }
        return kb;
    }
}
