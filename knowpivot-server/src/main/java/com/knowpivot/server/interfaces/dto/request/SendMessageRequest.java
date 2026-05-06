package com.knowpivot.server.interfaces.dto.request;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @JsonLongId
    @NotNull(message = "会话 ID 不能为空")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容不能超过 10000 字符")
    private String content;
}
