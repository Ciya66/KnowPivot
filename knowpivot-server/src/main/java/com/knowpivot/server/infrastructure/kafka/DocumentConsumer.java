package com.knowpivot.server.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowpivot.server.domain.entity.Document;
import com.knowpivot.server.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者 — 监听文档解析消息
 *
 * 收到消息后，更新文档状态为"解析中"。
 * 实际的文档解析、向量化由 Python AI 服务完成。
 * Python 端消费同一 topic 后进行 Embedding 并写入 Redis 向量索引。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentConsumer {

    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.document-parsing}", groupId = "document-group")
    public void onDocumentParsing(ConsumerRecord<String, String> record) {
        log.info("收到文档解析消息: key={}, partition={}, offset={}",
                record.key(), record.partition(), record.offset());

        try {
            DocumentParsingMessage message = objectMapper.readValue(record.value(), DocumentParsingMessage.class);

            // 更新文档状态为"解析中"
            Document doc = documentRepository.findById(message.getDocId());
            if (doc == null) {
                log.warn("文档不存在，跳过: docId={}", message.getDocId());
                return;
            }

            doc.markAsParsing();
            documentRepository.updateById(doc);
            log.info("文档状态已更新为解析中: docId={}", message.getDocId());

            // 实际的解析/向量化由 Python 端消费同一 topic 完成
            // Python 端流程：读取 MinIO 文件 → 文本切片 → Embedding → 写入 Redis 向量索引
            // 解析完成后 Python 回调 Java 端更新状态为 INDEXED

        } catch (Exception e) {
            log.error("处理文档解析消息异常: key={}", record.key(), e);
        }
    }
}
