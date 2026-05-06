package com.knowpivot.server.interfaces.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowpivot.server.ai.model.AgentResponse;
import com.knowpivot.server.application.service.ChatApplicationService;
import com.knowpivot.server.infrastructure.common.PageResult;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.request.CreateConversationRequest;
import com.knowpivot.server.interfaces.dto.request.SendMessageRequest;
import com.knowpivot.server.interfaces.dto.response.ConversationResponse;
import com.knowpivot.server.interfaces.dto.response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 智能对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final ChatApplicationService chatApplicationService;
    private final ObjectMapper objectMapper;

    /**
     * 创建会话
     */
    @PostMapping("/conversations")
    public Result<ConversationResponse> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse response = chatApplicationService.createConversation(request);
        return Result.ok("创建成功", response);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<PageResult<ConversationResponse>> listConversations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ConversationResponse> result = chatApplicationService.listConversations(pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable Long conversationId) {
        chatApplicationService.deleteConversation(conversationId);
        return Result.ok("删除成功", null);
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<PageResult<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<MessageResponse> result = chatApplicationService.getMessages(conversationId, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 发送消息 (SSE 流式响应)
     */
    @PostMapping(value = "/chat/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return chatApplicationService.sendMessage(request)
                .map(response -> {
                    try {
                        String event = response.getEvent();
                        String data = objectMapper.writeValueAsString(response);
                        return "event: " + event + "\ndata: " + data + "\n\n";
                    } catch (Exception e) {
                        log.error("SSE serialization error", e);
                        return "event: error\ndata: {\"errorMessage\":\"序列化失败\"}\n\n";
                    }
                })
                .onErrorResume(e -> {
                    log.error("SSE stream error", e);
                    String errorEvent = "event: error\ndata: {\"errorMessage\":\"" + e.getMessage() + "\"}\n\n";
                    return Flux.just(errorEvent);
                });
    }
}
