package com.knowpivot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Python Agent API 请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRunRequest {

    private String sessionId;
    private String query;
    private List<AgentContext.ChatMessage> history;
    private Map<String, Object> config;
    private String indexName;
    /**
     * 向量库中检索到的文档引用
     */
    private List<SourceReference> references;
    private String systemPrompt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceReference {
        private String segmentId;
        private String content;
        private String docId;
        private Integer pageNum;
        private Double similarity;
    }
}
