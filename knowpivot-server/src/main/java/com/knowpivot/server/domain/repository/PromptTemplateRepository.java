package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.PromptTemplate;

public interface PromptTemplateRepository {

    PromptTemplate findByCode(String code);

    void save(PromptTemplate template);

    void updateById(PromptTemplate template);
}
