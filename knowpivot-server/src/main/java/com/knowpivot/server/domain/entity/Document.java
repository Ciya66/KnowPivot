package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档聚合根
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    private Long id;
    private Long kbId;
    private String fileName;
    private String storagePath;
    private Long fileSize;
    private DocumentStatus status;
    private Integer chunkCount;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 标记为解析中
     */
    public void markAsParsing() {
        if (this.status != DocumentStatus.UPLOADED) {
            throw new IllegalStateException("仅已上传状态的文档可标记为解析中");
        }
        this.status = DocumentStatus.PARSING;
    }

    /**
     * 标记为已索引
     */
    public void markAsIndexed(int chunkCount) {
        if (this.status != DocumentStatus.PARSING) {
            throw new IllegalStateException("仅解析中状态的文档可标记为已索引");
        }
        this.status = DocumentStatus.INDEXED;
        this.chunkCount = chunkCount;
    }

    /**
     * 标记为解析失败
     */
    public void markAsFailed() {
        this.status = DocumentStatus.FAILED;
    }
}
