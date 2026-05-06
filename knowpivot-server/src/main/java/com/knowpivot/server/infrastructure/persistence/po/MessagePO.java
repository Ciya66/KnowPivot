package com.knowpivot.server.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_message")
public class MessagePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long conversationId;

    private Integer role;

    private String content;

    private Integer tokenCount;

    private String references;

    private Integer feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
