package com.knowpivot.server.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeConfig {

    /**
     * 切片大小（字符数）
     */
    @Builder.Default
    private Integer chunkSize = 500;

    /**
     * 切片重叠大小
     */
    @Builder.Default
    private Integer overlapSize = 50;

    /**
     * 相似度阈值
     */
    @Builder.Default
    private Double similarityThreshold = 0.7;
}
