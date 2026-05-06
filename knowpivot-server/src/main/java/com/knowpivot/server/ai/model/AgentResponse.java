package com.knowpivot.server.ai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Python Agent API 响应体 (SSE 事件)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /**
     * 事件类型: message / references / done / error
     */
    private String event;

    /**
     * 消息增量内容 (event=message)
     */
    private String delta;

    /**
     * 引用来源 (event=references)
     */
    private List<SourceReference> sources;

    /**
     * 消息ID (event=done)
     */
    @JsonProperty("messageId")
    private String messageId;

    /**
     * Token 消耗 (event=done)
     */
    private Integer tokenCount;

    /**
     * 完成原因 (event=done)
     */
    private String finishReason;

    /**
     * 错误信息 (event=error)
     */
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceReference {
        private String docName;
        private String segmentId;
        private String content;
        private Integer pageNum;
    }
}
