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
        log.info("[RAG-0] 知识检索入口: indexName={}, query='{}', topK={}, threshold={}",
                indexName, query.length() > 50 ? query.substring(0, 50) + "..." : query, topK, similarityThreshold);

        float[] queryVector = embeddingClient.embed(query);

        List<SearchHit> hits = redisVectorStore.searchSimilar(indexName, queryVector, topK, similarityThreshold)
                .stream()
                .map(r -> new SearchHit(r.getVectorId(), r.getContent(), r.getDocId(), r.getPageNum(), r.getScore()))
                .toList();

        log.info("[RAG-5] 最终结果: hitCount={}", hits.size());
        for (int i = 0; i < hits.size(); i++) {
            SearchHit h = hits.get(i);
            log.info("[RAG-5]   [{}] similarity={}, docId={}, content='{}'",
                    i, h.similarity() != null ? String.format("%.4f", h.similarity()) : "null",
                    h.docId(),
                    h.content() != null && h.content().length() > 60 ? h.content().substring(0, 60) + "..." : h.content());
        }
        return hits;
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
