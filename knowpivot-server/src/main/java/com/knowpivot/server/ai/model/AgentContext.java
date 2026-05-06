package com.knowpivot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI Agent 调用上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {

    private Long conversationId;
    private Long kbId;
    private String indexName;
    private List<ChatMessage> history;
    private Map<String, Object> config;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
