package com.knowpivot.server.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话聚合根
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    private Long id;
    private Long userId;
    private Long kbId;
    private String title;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * 添加消息并更新最后消息时间
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        this.lastMessageTime = message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now();
    }

    /**
     * 更新会话标题（首次对话时自动生成）
     */
    public void updateTitle(String title) {
        if (this.title == null || this.title.isBlank() || "新对话".equals(this.title)) {
            this.title = title;
        }
    }
}
