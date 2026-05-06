package com.knowpivot.server.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_kb_member")
public class KbMemberPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long kbId;

    private Long userId;

    private Integer role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
