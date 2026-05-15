package com.knowpivot.server.infrastructure.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Embedding 客户端 — 调用 OpenAI 兼容的 /v1/embeddings 接口
 */
@Slf4j
@Component
public class EmbeddingClient {

    private final WebClient webClient;
    private final String model;
    private final int dimensions;

    public EmbeddingClient(
            @Value("${knowpivot.embedding.base-url}") String baseUrl,
            @Value("${knowpivot.embedding.api-key:}") String apiKey,
            @Value("${knowpivot.embedding.model:text-embedding-3-small}") String model,
            @Value("${knowpivot.vector.dimension:1024}") int dimensions) {
        this.model = model;
        this.dimensions = dimensions;
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        this.webClient = builder.build();
    }

    /**
     * 将单条文本转为向量(用户问题转向量)
     */
    public float[] embed(String text) {
        return embed(new String[]{text})[0];
    }

    /**
     * 批量文本转为向量
     */
    public float[][] embed(String[] texts) {
        JsonNode response = webClient.post()
                .uri("/v1/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("input", texts, "model", model, "dimensions", dimensions))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("data")) {
            throw new RuntimeException("Embedding 服务返回异常: " + response);
        }

        JsonNode data = response.get("data");
        float[][] result = new float[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            JsonNode embeddingNode = data.get(i).get("embedding");
            float[] vec = new float[embeddingNode.size()];
            for (int j = 0; j < embeddingNode.size(); j++) {
                vec[j] = (float) embeddingNode.get(j).asDouble();
            }
            result[i] = vec;
        }

        if (result.length > 0) {
            int actualDim = result[0].length;
            if (actualDim != dimensions) {
                log.warn("[RAG-1] Embedding 维度不匹配: 模型返回 {} 维, 期望 {} 维", actualDim, dimensions);
            }
            StringBuilder preview = new StringBuilder();
            for (int k = 0; k < Math.min(5, actualDim); k++) {
                if (k > 0) preview.append(", ");
                preview.append(String.format("%.6f", result[0][k]));
            }
            log.info("[RAG-1] Embedding 完成: dim={}, byteSize={}, 前5维=[{}]",
                    actualDim, actualDim * 4, preview);
        }
        return result;
    }
}
