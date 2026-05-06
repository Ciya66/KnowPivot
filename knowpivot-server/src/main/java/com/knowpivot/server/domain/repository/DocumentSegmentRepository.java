package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.DocumentSegment;

import java.util.List;

public interface DocumentSegmentRepository {

    void save(DocumentSegment segment);

    void saveBatch(List<DocumentSegment> segments);

    List<DocumentSegment> findByDocId(Long docId);

    void deleteByDocId(Long docId);

    void deleteByKbId(Long kbId);
}
