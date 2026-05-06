package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.domain.entity.Message;
import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    @JsonLongId
    private Long messageId;
    private String role;
    private String content;
    private Integer tokenCount;
    private List<Message.Reference> references;
    private Integer feedback;
    private LocalDateTime createTime;
}
