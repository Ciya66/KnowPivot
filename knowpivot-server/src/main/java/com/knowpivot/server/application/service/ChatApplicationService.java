package com.knowpivot.server.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.knowpivot.server.ai.gateway.AgentGateway;
import com.knowpivot.server.ai.model.AgentContext;
import com.knowpivot.server.ai.model.AgentResponse;
import com.knowpivot.server.domain.entity.Conversation;
import com.knowpivot.server.domain.entity.KnowledgeBase;
import com.knowpivot.server.domain.entity.Message;
import com.knowpivot.server.domain.enums.MessageRole;
import com.knowpivot.server.domain.repository.ConversationRepository;
import com.knowpivot.server.domain.repository.MessageRepository;
import com.knowpivot.server.domain.service.KnowledgeDomainService;
import com.knowpivot.server.domain.service.TokenDomainService;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.ResultCode;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.redis.ConversationCache;
import com.knowpivot.server.infrastructure.util.IdGenerator;
import com.knowpivot.server.interfaces.dto.request.CreateConversationRequest;
import com.knowpivot.server.interfaces.dto.request.SendMessageRequest;
import com.knowpivot.server.interfaces.dto.response.ConversationResponse;
import com.knowpivot.server.interfaces.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天用例服务 - 核心编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatApplicationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeDomainService knowledgeDomainService;
    private final TokenDomainService tokenDomainService;
    private final AgentGateway agentGateway;
    private final ConversationCache conversationCache;
    private final IdGenerator idGenerator;

    /**
     * 创建会话
     */
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        long userId = StpUtil.getLoginIdAsLong();

        Conversation conversation = Conversation.builder()
                .id(idGenerator.nextId())
                .userId(userId)
                .kbId(request.getKbId())
                .title(request.getTitle() != null ? request.getTitle() : "新对话")
                .lastMessageTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        conversationRepository.save(conversation);

        return ConversationResponse.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .kbId(conversation.getKbId())
                .createTime(conversation.getCreatedAt())
                .lastMessageTime(conversation.getLastMessageTime())
                .build();
    }

    /**
     * 发送消息并获取流式响应 (SSE)
     */
    public Flux<AgentResponse> sendMessage(SendMessageRequest request) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 获取会话并校验
        Conversation conversation = conversationRepository.findById(request.getConversationId());
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 2. 保存用户消息
        Message userMessage = Message.builder()
                .id(idGenerator.nextId())
                .conversationId(conversation.getId())
                .role(MessageRole.USER)
                .content(request.getContent())
                .tokenCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);
        conversation.addMessage(userMessage);

        // 缓存用户消息到 Redis
        conversationCache.appendMessage(conversation.getId(),
                new AgentContext.ChatMessage("user", request.getContent()));

        // 3. 预扣 Token（按消息长度估算），done 事件到达后按实际值调整
        long estimatedTokens = Math.max(100, request.getContent().length() * 2);
        tokenDomainService.deductToken(userId, estimatedTokens, conversation.getId().toString());

        // 4. 构建 AI 上下文
        AgentContext context = buildAgentContext(conversation);

        // 5. 调用 AI 网关
        return agentGateway.chat(context, request.getContent())
                .doOnNext(response -> {
                    if ("done".equals(response.getEvent())) {
                        handleDoneEvent(conversation, response, userId, estimatedTokens);
                    }
                })
                .doOnError(e -> {
                    log.error("AI 调用失败，回退预扣 Token: conversation={}, estimatedTokens={}",
                            conversation.getId(), estimatedTokens, e);
                    try {
                        tokenDomainService.refundToken(userId, estimatedTokens,
                                conversation.getId().toString());
                    } catch (Exception refundEx) {
                        log.error("Token 回退失败: userId={}", userId, refundEx);
                    }
                });
    }

    /**
     * 获取会话历史消息
     */
    public PageResult<MessageResponse> getMessages(Long conversationId, int pageNum, int pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }

        List<Message> messages = messageRepository.findByConversationId(conversationId, pageNum, pageSize);
        long total = messageRepository.countByConversationId(conversationId);

        List<MessageResponse> responses = messages.stream()
                .map(msg -> MessageResponse.builder()
                        .messageId(msg.getId())
                        .role(msg.getRole().name().toLowerCase())
                        .content(msg.getContent())
                        .tokenCount(msg.getTokenCount())
                        .references(msg.getReferences())
                        .feedback(msg.getFeedback() != null ? msg.getFeedback().getCode() : null)
                        .createTime(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResult.of(total, responses);
    }

    /**
     * 获取会话列表
     */
    public PageResult<ConversationResponse> listConversations(int pageNum, int pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        List<Conversation> conversations = conversationRepository.findByUserId(userId, pageNum, pageSize);
        List<ConversationResponse> responses = conversations.stream()
                .map(conv -> ConversationResponse.builder()
                        .conversationId(conv.getId())
                        .title(conv.getTitle())
                        .kbId(conv.getKbId())
                        .createTime(conv.getCreatedAt())
                        .lastMessageTime(conv.getLastMessageTime())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(conversations.size(), responses);
    }

    /**
     * 删除会话
     */
    @Transactional
    public void deleteConversation(Long conversationId) {
        long userId = StpUtil.getLoginIdAsLong();
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        conversationRepository.deleteById(conversationId);
        conversationCache.clearHistory(conversationId);
    }

    private AgentContext buildAgentContext(Conversation conversation) {
        // 优先从 Redis 缓存加载最近 20 轮历史
        List<AgentContext.ChatMessage> history = conversationCache.getRecentHistory(conversation.getId(), 20);
        return AgentContext.builder()
                .conversationId(conversation.getId())
                .kbId(conversation.getKbId())
                .history(history)
                .build();
    }

    private void handleDoneEvent(Conversation conversation, AgentResponse response,
                                  Long userId, long estimatedTokens) {
        try {
            // 保存 AI 回复
            Message aiMessage = Message.builder()
                    .id(idGenerator.nextId())
                    .conversationId(conversation.getId())
                    .role(MessageRole.ASSISTANT)
                    .content("")
                    .tokenCount(response.getTokenCount() != null ? response.getTokenCount() : 0)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.save(aiMessage);
            conversation.addMessage(aiMessage);

            conversationCache.appendMessage(conversation.getId(),
                    new AgentContext.ChatMessage("assistant", aiMessage.getContent()));
            conversationRepository.updateById(conversation);

            // 调整 Token：实际消耗与预扣的差额
            long actualTokens = response.getTokenCount() != null ? response.getTokenCount() : 0;
            long diff = actualTokens - estimatedTokens;
            if (diff > 0) {
                // 实际 > 预估：补扣差额
                tokenDomainService.deductToken(userId, diff, conversation.getId().toString());
            } else if (diff < 0) {
                // 实际 < 预估：退还差额
                tokenDomainService.refundToken(userId, -diff, conversation.getId().toString());
            }
        } catch (Exception e) {
            // 保存/调整失败：回退全部预扣 Token
            log.error("handleDoneEvent 失败，回退预扣 Token: conversationId={}", conversation.getId(), e);
            try {
                tokenDomainService.refundToken(userId, estimatedTokens, conversation.getId().toString());
            } catch (Exception refundEx) {
                log.error("Token 回退失败: userId={}", userId, refundEx);
            }
        }
    }
}
