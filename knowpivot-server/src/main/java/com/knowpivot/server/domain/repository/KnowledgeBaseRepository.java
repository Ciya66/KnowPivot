package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseRepository {

    KnowledgeBase findById(Long id);

    void save(KnowledgeBase knowledgeBase);

    void updateById(KnowledgeBase knowledgeBase);

    void deleteById(Long id);

    List<KnowledgeBase> findByCreatorId(Long creatorId, int pageNum, int pageSize);

    long countByCreatorId(Long creatorId);
}
