package com.knowpivot.server.ai.client;

import com.knowpivot.server.ai.model.AgentResponse;
import com.knowpivot.server.ai.model.AgentRunRequest;
import com.knowpivot.server.ai.model.GenerateTitleRequest;
import com.knowpivot.server.ai.model.GenerateTitleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Python Agent HTTP 客户端 - 调用 Python AI 服务
 */
@Slf4j
@Component
public class PythonAgentClient {

    private final WebClient webClient;

    public PythonAgentClient(
            @Value("${ai.gateway.base-url}") String baseUrl,
            @Value("${ai.gateway.api-key}") String apiKey,
            @Value("${ai.gateway.timeout:60000}") int timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 调用 Python Agent API，返回 SSE 流
     */
    public Flux<AgentResponse> runAgent(AgentRunRequest request) {
//        return webClient.post()
//                .uri("/api/v1/agent/run")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .retrieve()
//                .bodyToFlux(AgentResponse.class)
//                .onErrorMap(e -> {
//                    log.error("Python Agent API call failed: {}", e.getMessage());
//                    return new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
//                });
        return webClient.post()
                .uri("/api/v1/agent/run")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Python API error body: {}", body);
                                    return Mono.error(new RuntimeException(body));
                                })
                )
                .bodyToFlux(AgentResponse.class);
    }

    /**
     * 调用生成标题的 API
     */
    public Mono<GenerateTitleResponse> generateTitle(GenerateTitleRequest request) {
        return webClient.post()
                .uri("/api/v1/agent/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GenerateTitleResponse.class)
                .onErrorMap(e -> {
                    log.error("Generate title API call failed: {}", e.getMessage());
                    return new RuntimeException("生成标题失败: " + e.getMessage(), e);
                });
    }
}
