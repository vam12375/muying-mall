package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.ai.entity.AiMessage;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.mapper.AiMessageMapper;
import com.muyingmall.ai.service.AiMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * AI 消息服务实现。
 */
@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements AiMessageService {

    @Override
    public AiMessage recordMessage(Long conversationId, Integer userId, String role, String content, String intent, String riskLevel) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setIntent(intent);
        message.setRiskLevel(StringUtils.hasText(riskLevel) ? riskLevel : AiRiskLevel.LOW.getCode());
        message.setCreateTime(LocalDateTime.now());
        save(message);
        return message;
    }
}
