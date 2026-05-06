package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.Document;
import com.knowpivot.server.domain.enums.DocumentStatus;

import java.util.List;

public interface DocumentRepository {

    Document findById(Long id);

    void save(Document document);

    void updateById(Document document);

    void deleteById(Long id);

    List<Document> findByKbId(Long kbId, Integer status, int pageNum, int pageSize);

    long countByKbId(Long kbId);

    long countByKbIdAndStatus(Long kbId, DocumentStatus status);
}
