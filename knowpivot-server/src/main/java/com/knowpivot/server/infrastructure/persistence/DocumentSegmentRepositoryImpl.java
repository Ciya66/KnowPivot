package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowpivot.server.domain.entity.DocumentSegment;
import com.knowpivot.server.domain.repository.DocumentSegmentRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.DocumentSegmentMapper;
import com.knowpivot.server.infrastructure.persistence.po.DocumentSegmentPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DocumentSegmentRepositoryImpl implements DocumentSegmentRepository {

    private final DocumentSegmentMapper documentSegmentMapper;
    private final POConverter converter;

    @Override
    public void save(DocumentSegment segment) {
        DocumentSegmentPO po = converter.toPO(segment);
        documentSegmentMapper.insert(po);
        segment.setId(po.getId());
    }

    @Override
    public void saveBatch(List<DocumentSegment> segments) {
        for (DocumentSegment segment : segments) {
            save(segment);
        }
    }

    @Override
    public List<DocumentSegment> findByDocId(Long docId) {
        LambdaQueryWrapper<DocumentSegmentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentSegmentPO::getDocId, docId);
        return documentSegmentMapper.selectList(wrapper).stream()
                .map(converter::toDocumentSegment)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDocId(Long docId) {
        LambdaQueryWrapper<DocumentSegmentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentSegmentPO::getDocId, docId);
        documentSegmentMapper.delete(wrapper);
    }

    @Override
    public void deleteByKbId(Long kbId) {
        LambdaQueryWrapper<DocumentSegmentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentSegmentPO::getKbId, kbId);
        documentSegmentMapper.delete(wrapper);
    }
}
