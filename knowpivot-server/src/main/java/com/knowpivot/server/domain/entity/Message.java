package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.enums.FeedbackType;
import com.knowpivot.server.domain.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Long id;
    private Long conversationId;
    private MessageRole role;
    private String content;
    private Integer tokenCount;
    private List<Reference> references;
    private FeedbackType feedback;
    private LocalDateTime createdAt;

    /**
     * 引用来源值对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reference {
        private String docId;
        private String docName;
        private String segmentId;
        private String content;
        private Integer pageNum;
    }
}
