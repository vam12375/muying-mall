package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.ai.entity.AiConversation;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.mapper.AiConversationMapper;
import com.muyingmall.ai.service.AiConversationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * AI 会话服务实现。
 */
@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation>
        implements AiConversationService {

    @Override
    public AiConversation getOrCreateConversation(Integer userId, Long conversationId, String channel, String firstMessage) {
        if (conversationId != null) {
            AiConversation existing = getById(conversationId);
            if (existing != null && (existing.getUserId() == null || existing.getUserId().equals(userId))) {
                return existing;
            }
        }

        AiConversation conversation = new AiConversation();
        conversation.setConversationNo(generateConversationNo());
        conversation.setUserId(userId);
        conversation.setChannel(StringUtils.hasText(channel) ? channel : "WEB");
        conversation.setStatus("OPEN");
        conversation.setRiskLevel(AiRiskLevel.LOW.getCode());
        conversation.setLastMessage(limitText(firstMessage, 500));
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        save(conversation);
        return conversation;
    }

    @Override
    public void refreshConversation(Long conversationId, String intent, String riskLevel, String lastMessage, String status) {
        if (conversationId == null) {
            return;
        }
        AiConversation conversation = getById(conversationId);
        if (conversation == null) {
            return;
        }
        conversation.setCurrentIntent(intent);
        if (StringUtils.hasText(riskLevel)) {
            conversation.setRiskLevel(riskLevel);
        }
        if (StringUtils.hasText(lastMessage)) {
            conversation.setLastMessage(limitText(lastMessage, 500));
        }
        if (StringUtils.hasText(status)) {
            conversation.setStatus(status);
        }
        conversation.setUpdateTime(LocalDateTime.now());
        updateById(conversation);
    }

    private String generateConversationNo() {
        String datePart = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "AIC" + datePart + randomPart;
    }

    private String limitText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
