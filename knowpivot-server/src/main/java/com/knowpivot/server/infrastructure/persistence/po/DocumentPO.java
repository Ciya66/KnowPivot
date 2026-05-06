package com.knowpivot.server.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_document")
public class DocumentPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long kbId;

    private String fileName;

    private String storagePath;

    private Long fileSize;

    private Integer status;

    private Integer chunkCount;

    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
