package com.knowpivot.server.interfaces.dto.request;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateConversationRequest {

    @JsonLongId
    private Long kbId;

    @Size(max = 255, message = "标题不能超过 255 字符")
    private String title;
}
