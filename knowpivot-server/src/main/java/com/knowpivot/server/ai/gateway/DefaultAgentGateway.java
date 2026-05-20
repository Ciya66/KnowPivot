package com.knowpivot.server.ai.gateway;

import com.knowpivot.server.ai.client.PythonAgentClient;
import com.knowpivot.server.ai.model.*;
import com.knowpivot.server.ai.strategy.ModelStrategy;
import com.knowpivot.server.domain.repository.PromptTemplateRepository;
import com.knowpivot.server.domain.service.KnowledgeSearchService;
import com.knowpivot.server.infrastructure.common.ResultCode;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认 AI Agent 网关实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAgentGateway implements AgentGateway {

    private final PythonAgentClient pythonAgentClient;
    private final ModelStrategy modelStrategy;
    private final PromptTemplateRepository promptTemplateRepository;

    private final KnowledgeSearchService knowledgeSearchService;

    @Override
    public Flux<AgentResponse> chat(AgentContext context, String query) {
        try {
            // 1. 加载 Prompt 模板
            PromptTemplate promptTemplate = loadPromptTemplate();

            // 2. 构建请求
            Map<String, Object> config = context.getConfig() != null ? new HashMap<>(context.getConfig()) : new HashMap<>();
            config.put("model", modelStrategy.getModelCode());

            List<AgentRunRequest.SourceReference> references = new ArrayList<>();
            if (context.getIndexName() != null) {
                log.info("[RAG] 开始检索: indexName={}, kbId={}", context.getIndexName(), context.getKbId());
                List<KnowledgeSearchService.SearchHit> hits = knowledgeSearchService.search(
                        context.getIndexName(),
                        query,
                        5,
                        0.7
                );

                references = hits.stream()
                        .filter(hit -> {
                            boolean valid = hit.content() != null && hit.docId() != null && hit.pageNum() != null;
                            if (!valid) {
                                log.warn("[RAG] 跳过无效引用: vectorId={}, contentNull={}, docIdNull={}, pageNumNull={}",
                                        hit.vectorId(), hit.content() == null, hit.docId() == null, hit.pageNum() == null);
                            }
                            return valid;
                        })
                        .map(hit -> AgentRunRequest.SourceReference.builder()
                                .docId(hit.docId())
                                .content(hit.content())
                                .pageNum(hit.pageNum())
                                .similarity(hit.similarity())
                                .segmentId(hit.vectorId())
                                .build())
                        .toList();
                log.info("[RAG] references 构建完成: count={}", references.size());
            } else {
                log.info("[RAG] indexName 为空，跳过知识检索");
            }

            // 发送 Agent 请求
            AgentRunRequest request = AgentRunRequest.builder()
                    .sessionId(String.valueOf(context.getConversationId()))
                    .query(query)
                    .history(context.getHistory())
                    .config(config)
                    .indexName(context.getIndexName())
                    .references(references)
                    .systemPrompt(promptTemplate.getSystemPrompt())
                    .build();

            log.info("Calling AI Agent: session={}, model={}, references={}, systemPrompt={}",
                    request.getSessionId(), modelStrategy.getModelCode(), references.size(),
                    promptTemplate.getSystemPrompt() != null ? promptTemplate.getSystemPrompt().length() + " chars" : "null");

            // 3. 调用 Python 服务
            return pythonAgentClient.runAgent(request)
                    .doOnError(e -> log.error("AI Agent call failed", e));

        } catch (Exception e) {
            log.error("AgentGateway error", e);
            return Flux.error(new BusinessException(ResultCode.MODEL_SERVICE_UNAVAILABLE));
        }
    }

    @Override
    public Mono<String> generateTitle(String userMessage) {
        GenerateTitleRequest request = GenerateTitleRequest.builder()
                .userMessage(userMessage)
                .build();

        return pythonAgentClient.generateTitle(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        log.info("Generated title successfully: {}", response.getTitle());
                        return response.getTitle();
                    } else {
                        log.warn("Failed to generate title, using default. Error: {}", response.getErrorMessage());
                        return "新对话";
                    }
                })
                .onErrorResume(e -> {
                    log.error("Generate title failed, using default", e);
                    return Mono.just("新对话");
                });
    }

    private PromptTemplate loadPromptTemplate() {
        com.knowpivot.server.domain.entity.PromptTemplate template =
                promptTemplateRepository.findByCode("SYSTEM_PROMPT");
        if (template != null) {
            return PromptTemplate.builder()
                    .systemPrompt(template.getContent())
                    .build();
        }
        // 默认 Prompt
        return PromptTemplate.builder()
                .systemPrompt("你是一个智能知识库助手，请基于提供的文档内容回答用户问题。")
                .build();
    }
}
