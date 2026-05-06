package com.knowpivot.server.infrastructure.redis;

import com.knowpivot.server.domain.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识检索服务实现 — 委托 RedisVectorStore 执行向量检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private final RedisVectorStore redisVectorStore;

    @Override
    public List<SearchHit> search(String indexName, String query, int topK, double similarityThreshold) {
        // 注意：query 文本需要先经过 Embedding 服务转换为向量
        // 这里暂时由调用方（AI Gateway）负责 Embedding
        // 后续可在此处集成 Embedding API
        log.info("知识检索请求: indexName={}, topK={}, threshold={}", indexName, topK, similarityThreshold);

        // TODO: 将 query 文本 Embedding 为向量后调用 redisVectorStore.searchSimilar()
        // float[] queryVector = embeddingClient.embed(query);
        // return redisVectorStore.searchSimilar(indexName, queryVector, topK, similarityThreshold)
        //        .stream()
        //        .map(r -> new SearchHit(r.getVectorId(), r.getContent(), r.getDocId(), r.getPageNum(), r.getScore()))
        //        .toList();

        throw new UnsupportedOperationException("需要接入 Embedding 服务后启用向量检索");
    }

    @Override
    public void createIndex(String indexName) {
        redisVectorStore.createIndex(indexName);
    }

    @Override
    public void dropIndex(String indexName) {
        redisVectorStore.dropIndex(indexName);
    }
}
