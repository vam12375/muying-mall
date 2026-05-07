package com.muyingmall.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import com.muyingmall.ai.entity.AiConversation;
import com.muyingmall.ai.entity.AiMessage;
import com.muyingmall.ai.service.AiAgentGatewayService;
import com.muyingmall.ai.service.AiConversationService;
import com.muyingmall.ai.service.AiMessageService;
import com.muyingmall.common.api.Result;
import com.muyingmall.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用户端 AI 聊天入口。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "用户-AI助手", description = "AI导购、售后判断与人工工单入口")
public class AiChatController {

    private static final int AI_CONTEXT_MAX_CHARS = 256 * 1024;
    private static final int DEFAULT_HISTORY_MESSAGE_LIMIT = 100;

    private final UserContext userContext;
    private final AiConversationService aiConversationService;
    private final AiMessageService aiMessageService;
    private final AiAgentGatewayService aiAgentGatewayService;
    private final ObjectMapper objectMapper;

    @GetMapping("/conversations")
    @Operation(summary = "分页查询当前用户 AI 会话")
    public Result<Page<AiConversation>> listConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录后再查看AI会话");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
                .eq(StringUtils.hasText(status), AiConversation::getStatus, status)
                .orderByDesc(AiConversation::getUpdateTime);
        return Result.success(aiConversationService.page(new Page<>(safePage, safeSize), wrapper));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "查询当前用户 AI 会话消息")
    public Result<List<AiMessage>> listMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "100") Integer limit) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录后再查看AI消息");
        }
        if (!isConversationOwner(conversationId, userId)) {
            return Result.forbidden("无权查看该AI会话");
        }

        int safeLimit = Math.min(Math.max(limit, 1), DEFAULT_HISTORY_MESSAGE_LIMIT);
        return Result.success(aiMessageService.listRecentMessages(conversationId, userId, safeLimit));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI Agent 流式聊天")
    public ResponseEntity<StreamingResponseBody> chat(
            @RequestBody @Valid AiChatRequest request,
            HttpServletRequest servletRequest) {
        PreparedChat prepared = prepareChat(request, servletRequest);
        if (prepared == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(this::writeUnauthorizedSse);
        }

        StreamingResponseBody body = aiAgentGatewayService.streamChat(
                prepared.userId(),
                prepared.authorizationHeader(),
                request,
                response -> persistAssistantResponse(prepared, request, response));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    @PostMapping(value = "/chat/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "AI Agent 非流式聊天")
    public Result<AiChatResponse> chatJson(@RequestBody @Valid AiChatRequest request, HttpServletRequest servletRequest) {
        PreparedChat prepared = prepareChat(request, servletRequest);
        if (prepared == null) {
            return Result.unauthorized("请先登录后再使用AI助手");
        }

        AiChatResponse response = aiAgentGatewayService.chat(prepared.userId(), prepared.authorizationHeader(), request);
        persistAssistantResponse(prepared, request, response);

        return Result.success(response);
    }

    private PreparedChat prepareChat(AiChatRequest request, HttpServletRequest servletRequest) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return null;
        }

        AiConversation conversation = aiConversationService.getOrCreateConversation(
                userId,
                request.getConversationId(),
                request.getChannel(),
                request.getMessage());
        request.setConversationId(conversation.getId());

        aiMessageService.recordMessage(conversation.getId(), userId, "USER", request.getMessage(), null, "LOW");
        request.setMaxContextChars(AI_CONTEXT_MAX_CHARS);
        request.setHistory(aiMessageService.listContextMessages(conversation.getId(), userId, AI_CONTEXT_MAX_CHARS));

        String authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return new PreparedChat(userId, conversation, authorizationHeader);
    }

    private void persistAssistantResponse(PreparedChat prepared, AiChatRequest request, AiChatResponse response) {
        response.setConversationId(prepared.conversation().getId());
        aiMessageService.recordMessage(
                prepared.conversation().getId(),
                prepared.userId(),
                "ASSISTANT",
                response.getAnswer(),
                response.getIntent(),
                response.getRiskLevel(),
                serializeToolResults(response));
        aiConversationService.refreshConversation(
                prepared.conversation().getId(),
                response.getIntent(),
                response.getRiskLevel(),
                request.getMessage(),
                Boolean.TRUE.equals(response.getHumanHandoffRequired()) ? "HANDOFF" : "OPEN");
    }

    private boolean isConversationOwner(Long conversationId, Integer userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        AiConversation conversation = aiConversationService.getById(conversationId);
        return conversation != null && userId.equals(conversation.getUserId());
    }

    private String serializeToolResults(AiChatResponse response) {
        if (response == null || response.getToolResults() == null || response.getToolResults().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(response.getToolResults());
        } catch (Exception e) {
            return null;
        }
    }

    private void writeUnauthorizedSse(java.io.OutputStream outputStream) throws IOException {
        String event = "event: error\n"
                + "data: {\"message\":\"请先登录后再使用AI助手\"}\n\n";
        outputStream.write(event.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private record PreparedChat(Integer userId, AiConversation conversation, String authorizationHeader) {
    }
}
