package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    @JsonLongId
    private Long conversationId;
    private String title;
    @JsonLongId
    private Long kbId;
    private LocalDateTime createTime;
    private LocalDateTime lastMessageTime;
}
