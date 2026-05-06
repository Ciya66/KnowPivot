package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowpivot.server.domain.entity.Document;
import com.knowpivot.server.domain.enums.DocumentStatus;
import com.knowpivot.server.domain.repository.DocumentRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.DocumentMapper;
import com.knowpivot.server.infrastructure.persistence.po.DocumentPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepository {

    private final DocumentMapper documentMapper;
    private final POConverter converter;

    @Override
    public Document findById(Long id) {
        DocumentPO po = documentMapper.selectById(id);
        return converter.toDocument(po);
    }

    @Override
    public void save(Document document) {
        DocumentPO po = converter.toPO(document);
        documentMapper.insert(po);
        document.setId(po.getId());
    }

    @Override
    public void updateById(Document document) {
        DocumentPO po = converter.toPO(document);
        documentMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        documentMapper.deleteById(id);
    }

    @Override
    public List<Document> findByKbId(Long kbId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<DocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentPO::getKbId, kbId);
        if (status != null) {
            wrapper.eq(DocumentPO::getStatus, status);
        }
        wrapper.orderByDesc(DocumentPO::getCreatedAt);
        Page<DocumentPO> page = documentMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(converter::toDocument)
                .collect(Collectors.toList());
    }

    @Override
    public long countByKbId(Long kbId) {
        LambdaQueryWrapper<DocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentPO::getKbId, kbId);
        return documentMapper.selectCount(wrapper);
    }

    @Override
    public long countByKbIdAndStatus(Long kbId, DocumentStatus status) {
        LambdaQueryWrapper<DocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentPO::getKbId, kbId)
                .eq(DocumentPO::getStatus, status.getCode());
        return documentMapper.selectCount(wrapper);
    }
}
