package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.ai.dto.AiChatContextMessage;
import com.muyingmall.ai.entity.AiMessage;

import java.util.List;

/**
 * AI 消息服务。
 */
public interface AiMessageService extends IService<AiMessage> {

    AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel);

    AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel, String toolResults);

    List<AiMessage> listRecentMessages(Long conversationId, Integer userId, int limit);

    List<AiMessage> listRecentMessagesForAdmin(Long conversationId, int limit);

    List<AiChatContextMessage> listContextMessages(Long conversationId, Integer userId, int maxContextChars);
}
