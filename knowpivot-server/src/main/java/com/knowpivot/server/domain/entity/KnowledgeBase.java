package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.valueobject.KnowledgeConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库聚合根
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase {

    private Long id;
    private String name;
    private String description;
    private String indexName;
    private KnowledgeConfig config;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 创建知识库时自动生成 Redis Vector 索引名
     */
    public static String generateIndexName(Long kbId) {
        return "idx_" + kbId;
    }

    /**
     * 更新知识库信息
     */
    public void update(String name, String description, KnowledgeConfig config) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (config != null) {
            this.config = config;
        }
    }
}
