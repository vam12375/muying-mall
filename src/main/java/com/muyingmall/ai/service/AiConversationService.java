package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.ai.entity.AiConversation;

/**
 * AI 会话服务。
 */
public interface AiConversationService extends IService<AiConversation> {

    AiConversation getOrCreateConversation(Integer userId, Long conversationId, String channel, String firstMessage);

    void refreshConversation(Long conversationId, String intent, String riskLevel, String lastMessage, String status);
}
