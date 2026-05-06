package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.ai.entity.AiMessage;

/**
 * AI 消息服务。
 */
public interface AiMessageService extends IService<AiMessage> {

    AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel);
}
