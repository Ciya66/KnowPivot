package com.knowpivot.server.domain.service;

import java.util.List;

/**
 * 知识检索领域服务接口 — 定义向量检索的业务语义
 * 具体实现由 Infrastructure 层完成
 */
public interface KnowledgeSearchService {

    /**
     * 在知识库中搜索与 query 最相关的文本片段
     *
     * @param indexName             Redis 向量索引名
     * @param query                 查询文本（需要先 Embedding 为向量）
     * @param topK                  返回最相似的 K 条
     * @param similarityThreshold   相似度阈值（低于此值的结果被过滤）
     * @return 检索结果列表
     */
    List<SearchHit> search(String indexName, String query, int topK, double similarityThreshold);

    /**
     * 初始化知识库向量索引
     */
    void createIndex(String indexName);

    /**
     * 删除知识库向量索引
     */
    void dropIndex(String indexName);

    /**
     * 检索命中结果
     */
    record SearchHit(
            String vectorId,
            String content,
            String docId,
            Integer pageNum,
            Double similarity
    ) {}
}
