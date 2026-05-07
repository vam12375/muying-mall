package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.ai.dto.AiChatContextMessage;
import com.muyingmall.ai.entity.AiMessage;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.mapper.AiMessageMapper;
import com.muyingmall.ai.service.AiMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

/**
 * AI 消息服务实现。
 */
@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements AiMessageService {

    @Override
    public AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel) {
        return recordMessage(conversationId, userId, role, content, intent, riskLevel, null);
    }

    @Override
    public AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel, String toolResults) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setIntent(intent);
        message.setRiskLevel(StringUtils.hasText(riskLevel) ? riskLevel : AiRiskLevel.LOW.getCode());
        message.setToolResults(toolResults);
        message.setCreateTime(LocalDateTime.now());
        save(message);
        return message;
    }

    @Override
    public List<AiMessage> listRecentMessages(Long conversationId, Integer userId, int limit) {
        if (conversationId == null || userId == null) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        List<AiMessage> messages = list(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId)
                .eq(AiMessage::getUserId, userId)
                .orderByDesc(AiMessage::getCreateTime)
                .orderByDesc(AiMessage::getId)
                .last("LIMIT " + safeLimit));
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<AiMessage> listRecentMessagesForAdmin(Long conversationId, int limit) {
        if (conversationId == null) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        List<AiMessage> messages = list(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByDesc(AiMessage::getCreateTime)
                .orderByDesc(AiMessage::getId)
                .last("LIMIT " + safeLimit));
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<AiChatContextMessage> listContextMessages(Long conversationId, Integer userId, int maxContextChars) {
        int safeMaxChars = Math.min(Math.max(maxContextChars, 1), 256 * 1024);
        List<AiMessage> recentMessages = listRecentMessages(conversationId, userId, 500);
        List<AiMessage> selectedMessages = new ArrayList<>();
        int currentChars = 0;

        // 从最新消息向前裁剪，确保传给 Agent 的同窗上下文不超过 256k 字符。
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            AiMessage message = recentMessages.get(i);
            String content = message.getContent();
            int contentLength = content == null ? 0 : content.length();
            if (!selectedMessages.isEmpty() && currentChars + contentLength > safeMaxChars) {
                break;
            }
            selectedMessages.add(message);
            currentChars += contentLength;
        }

        Collections.reverse(selectedMessages);
        return selectedMessages.stream().map(this::toContextMessage).toList();
    }

    private AiChatContextMessage toContextMessage(AiMessage message) {
        AiChatContextMessage contextMessage = new AiChatContextMessage();
        contextMessage.setId(message.getId());
        contextMessage.setRole(message.getRole());
        contextMessage.setContent(message.getContent());
        contextMessage.setIntent(message.getIntent());
        contextMessage.setRiskLevel(message.getRiskLevel());
        contextMessage.setCreateTime(message.getCreateTime());
        return contextMessage;
    }
}
