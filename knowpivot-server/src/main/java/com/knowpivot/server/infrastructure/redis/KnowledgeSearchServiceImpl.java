package com.knowpivot.server.infrastructure.redis;

import com.knowpivot.server.domain.service.KnowledgeSearchService;
import com.knowpivot.server.infrastructure.embedding.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识检索服务实现 — Embedding + Redis 向量检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private final RedisVectorStore redisVectorStore;
    private final EmbeddingClient embeddingClient;

    @Override
    public List<SearchHit> search(String indexName, String query, int topK, double similarityThreshold) {
        log.info("知识检索: indexName={}, topK={}, threshold={}", indexName, topK, similarityThreshold);

        float[] queryVector = embeddingClient.embed(query);
        return redisVectorStore.searchSimilar(indexName, queryVector, topK, similarityThreshold)
                .stream()
                .map(r -> new SearchHit(r.getVectorId(), r.getContent(), r.getDocId(), r.getPageNum(), r.getScore()))
                .toList();
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
