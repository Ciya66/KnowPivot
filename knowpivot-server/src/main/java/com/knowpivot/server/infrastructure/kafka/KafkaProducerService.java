package com.knowpivot.server.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 生产者 — 发送文档解析消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送文档解析消息到 document-parsing topic
     */
    public void sendDocumentParsingMessage(DocumentParsingMessage message) {
        String topic = "document-parsing";
        String key = message.getDocId().toString();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Kafka 消息序列化失败: docId={}", message.getDocId(), e);
            throw new RuntimeException("消息序列化失败", e);
        }

        log.info("发送文档解析消息: topic={}, key={}, payload={}", topic, key, payload);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka 消息发送失败: docId={}", message.getDocId(), ex);
            } else {
                log.info("Kafka 消息发送成功: docId={}, partition={}, offset={}",
                        message.getDocId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
