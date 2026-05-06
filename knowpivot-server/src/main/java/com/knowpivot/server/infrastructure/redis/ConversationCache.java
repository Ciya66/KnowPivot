package com.knowpivot.server.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowpivot.server.ai.model.AgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话历史缓存 — Redis List 结构
 * 用于快速获取最近 N 轮对话历史，避免频繁查 DB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "conversation:history:";
    private static final long TTL_HOURS = 24;

    /**
     * 追加消息到缓存
     */
    public void appendMessage(Long conversationId, AgentContext.ChatMessage message) {
        String key = KEY_PREFIX + conversationId;
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("缓存消息序列化失败: conversationId={}", conversationId, e);
        }
    }

    /**
     * 获取最近 N 轮历史（从尾部向前取）
     */
    public List<AgentContext.ChatMessage> getRecentHistory(Long conversationId, int maxCount) {
        String key = KEY_PREFIX + conversationId;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return new ArrayList<>();
        }

        // 从尾部取最近 maxCount 条
        long start = Math.max(0, size - maxCount);
        List<String> jsonList = redisTemplate.opsForList().range(key, start, size - 1);
        if (jsonList == null) {
            return new ArrayList<>();
        }

        List<AgentContext.ChatMessage> messages = new ArrayList<>();
        for (String json : jsonList) {
            try {
                messages.add(objectMapper.readValue(json, AgentContext.ChatMessage.class));
            } catch (JsonProcessingException e) {
                log.error("缓存消息反序列化失败: {}", json, e);
            }
        }
        return messages;
    }

    /**
     * 清空会话历史缓存
     */
    public void clearHistory(Long conversationId) {
        String key = KEY_PREFIX + conversationId;
        redisTemplate.delete(key);
        log.debug("会话缓存已清除: conversationId={}", conversationId);
    }
}
