package com.muyingmall.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.ai.dto.AiTicketUpdateRequest;
import com.muyingmall.ai.entity.AiConversation;
import com.muyingmall.ai.entity.AiMessage;
import com.muyingmall.ai.entity.AiSupportTicket;
import com.muyingmall.ai.entity.AiToolCallLog;
import com.muyingmall.ai.service.AiConversationService;
import com.muyingmall.ai.service.AiMessageService;
import com.muyingmall.ai.service.AiSupportTicketService;
import com.muyingmall.ai.service.AiToolCallLogService;
import com.muyingmall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 AI Agent 监控接口。
 */
@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
@Tag(name = "后台-AI Agent", description = "AI会话、工具日志与人工工单管理")
public class AdminAiController {

    private final AiConversationService aiConversationService;
    private final AiMessageService aiMessageService;
    private final AiToolCallLogService aiToolCallLogService;
    private final AiSupportTicketService aiSupportTicketService;

    @GetMapping("/conversations")
    @Operation(summary = "分页查询 AI 会话")
    public Result<Page<AiConversation>> listConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer userId) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), AiConversation::getStatus, status)
                .eq(userId != null, AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getUpdateTime);
        return Result.success(aiConversationService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "查询 AI 会话消息明细")
    public Result<List<AiMessage>> listConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "100") Integer limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        // 管理端用于排查 Agent 决策链路，不限制用户ID，由 /admin/** 鉴权保护。
        return Result.success(aiMessageService.listRecentMessagesForAdmin(conversationId, safeLimit));
    }

    @GetMapping("/tool-logs")
    @Operation(summary = "分页查询 Agent 工具调用日志")
    public Result<Page<AiToolCallLog>> listToolLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) Boolean success) {
        LambdaQueryWrapper<AiToolCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(conversationId != null, AiToolCallLog::getConversationId, conversationId)
                .eq(StringUtils.hasText(toolName), AiToolCallLog::getToolName, toolName)
                .eq(StringUtils.hasText(intent), AiToolCallLog::getIntent, intent)
                .eq(success != null, AiToolCallLog::getSuccess, success)
                .orderByDesc(AiToolCallLog::getCreateTime);
        return Result.success(aiToolCallLogService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/tickets")
    @Operation(summary = "分页查询 AI 工单")
    public Result<Page<AiSupportTicket>> listTickets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Integer userId) {
        LambdaQueryWrapper<AiSupportTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), AiSupportTicket::getStatus, status)
                .eq(StringUtils.hasText(riskLevel), AiSupportTicket::getRiskLevel, riskLevel)
                .eq(userId != null, AiSupportTicket::getUserId, userId)
                .orderByDesc(AiSupportTicket::getCreateTime);
        return Result.success(aiSupportTicketService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/tickets/{ticketId}")
    @Operation(summary = "更新 AI 工单状态")
    public Result<AiSupportTicket> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody @Valid AiTicketUpdateRequest request) {
        return Result.success(aiSupportTicketService.updateTicket(ticketId, request), "AI工单已更新");
    }
}
