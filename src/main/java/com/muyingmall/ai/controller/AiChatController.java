package com.muyingmall.ai.controller;

import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import com.muyingmall.ai.entity.AiConversation;
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 AI 聊天入口。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "用户-AI助手", description = "AI导购、售后判断与人工工单入口")
public class AiChatController {

    private final UserContext userContext;
    private final AiConversationService aiConversationService;
    private final AiMessageService aiMessageService;
    private final AiAgentGatewayService aiAgentGatewayService;

    @PostMapping("/chat")
    @Operation(summary = "AI Agent 聊天")
    public Result<AiChatResponse> chat(@RequestBody @Valid AiChatRequest request, HttpServletRequest servletRequest) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录后再使用AI助手");
        }

        AiConversation conversation = aiConversationService.getOrCreateConversation(
                userId,
                request.getConversationId(),
                request.getChannel(),
                request.getMessage());
        request.setConversationId(conversation.getId());

        aiMessageService.recordMessage(conversation.getId(), userId, "USER", request.getMessage(), null, "LOW");

        String authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        AiChatResponse response = aiAgentGatewayService.chat(userId, authorizationHeader, request);
        response.setConversationId(conversation.getId());

        aiMessageService.recordMessage(
                conversation.getId(),
                userId,
                "ASSISTANT",
                response.getAnswer(),
                response.getIntent(),
                response.getRiskLevel());
        aiConversationService.refreshConversation(
                conversation.getId(),
                response.getIntent(),
                response.getRiskLevel(),
                request.getMessage(),
                Boolean.TRUE.equals(response.getHumanHandoffRequired()) ? "HANDOFF" : "OPEN");

        return Result.success(response);
    }
}
