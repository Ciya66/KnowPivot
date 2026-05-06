package com.knowpivot.server.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_document_segment")
public class DocumentSegmentPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long docId;

    private Long kbId;

    private String vectorId;

    private String content;

    private Integer pageNum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
