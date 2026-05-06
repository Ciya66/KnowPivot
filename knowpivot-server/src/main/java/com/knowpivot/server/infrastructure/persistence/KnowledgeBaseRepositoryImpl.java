package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowpivot.server.domain.entity.KnowledgeBase;
import com.knowpivot.server.domain.repository.KnowledgeBaseRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.KnowledgeBaseMapper;
import com.knowpivot.server.infrastructure.persistence.po.KnowledgeBasePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class KnowledgeBaseRepositoryImpl implements KnowledgeBaseRepository {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final POConverter converter;

    @Override
    public KnowledgeBase findById(Long id) {
        KnowledgeBasePO po = knowledgeBaseMapper.selectById(id);
        return converter.toKnowledgeBase(po);
    }

    @Override
    public void save(KnowledgeBase knowledgeBase) {
        KnowledgeBasePO po = converter.toPO(knowledgeBase);
        knowledgeBaseMapper.insert(po);
        knowledgeBase.setId(po.getId());
    }

    @Override
    public void updateById(KnowledgeBase knowledgeBase) {
        KnowledgeBasePO po = converter.toPO(knowledgeBase);
        knowledgeBaseMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        knowledgeBaseMapper.deleteById(id);
    }

    @Override
    public List<KnowledgeBase> findByCreatorId(Long creatorId, int pageNum, int pageSize) {
        LambdaQueryWrapper<KnowledgeBasePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBasePO::getCreatorId, creatorId)
                .orderByDesc(KnowledgeBasePO::getCreatedAt);
        Page<KnowledgeBasePO> page = knowledgeBaseMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(converter::toKnowledgeBase)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        LambdaQueryWrapper<KnowledgeBasePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBasePO::getCreatorId, creatorId);
        return knowledgeBaseMapper.selectCount(wrapper);
    }
}
