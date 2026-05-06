package com.knowpivot.server.infrastructure.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档解析 Kafka 消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParsingMessage {

    /**
     * 文档 ID
     */
    private Long docId;

    /**
     * 知识库 ID
     */
    private Long kbId;

    /**
     * MinIO 存储路径
     */
    private String storagePath;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * Redis 向量索引名
     */
    private String indexName;
}
