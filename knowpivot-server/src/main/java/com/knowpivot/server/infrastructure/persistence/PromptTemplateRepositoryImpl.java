package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowpivot.server.domain.entity.PromptTemplate;
import com.knowpivot.server.domain.repository.PromptTemplateRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.PromptTemplateMapper;
import com.knowpivot.server.infrastructure.persistence.po.PromptTemplatePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PromptTemplateRepositoryImpl implements PromptTemplateRepository {

    private final PromptTemplateMapper promptTemplateMapper;
    private final POConverter converter;

    @Override
    public PromptTemplate findByCode(String code) {
        LambdaQueryWrapper<PromptTemplatePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplatePO::getCode, code)
                .eq(PromptTemplatePO::getIsActive, true);
        PromptTemplatePO po = promptTemplateMapper.selectOne(wrapper);
        return converter.toPromptTemplate(po);
    }

    @Override
    public void save(PromptTemplate template) {
        PromptTemplatePO po = converter.toPO(template);
        promptTemplateMapper.insert(po);
        template.setId(po.getId());
    }

    @Override
    public void updateById(PromptTemplate template) {
        PromptTemplatePO po = converter.toPO(template);
        promptTemplateMapper.updateById(po);
    }
}
