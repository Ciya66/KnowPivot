package com.knowpivot.server.ai.gateway;

import com.knowpivot.server.ai.model.AgentContext;
import com.knowpivot.server.ai.model.AgentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AI Agent 网关接口 - 统一 AI 调用入口
 */
public interface AgentGateway {

    /**
     * 执行对话，返回流式响应
     */
    Flux<AgentResponse> chat(AgentContext context, String query);

    /**
     * 生成对话标题
     */
    Mono<String> generateTitle(String userMessage);
}
