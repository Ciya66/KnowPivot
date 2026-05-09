package com.knowpivot.server.infrastructure.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public EmbeddingClient(
            @Value("${knowpivot.embedding.base-url}") String baseUrl,
            @Value("${knowpivot.embedding.api-key:}") String apiKey,
            @Value("${knowpivot.embedding.model:text-embedding-3-small}") String model) {
        this.model = model;
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        this.webClient = builder.build();
    }

    /**
     * 将单条文本转为向量
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
                .bodyValue(Map.of("input", texts, "model", model))
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

        log.debug("Embedding 完成: texts={}, vectors={}", texts.length, result.length);
        return result;
    }
}
