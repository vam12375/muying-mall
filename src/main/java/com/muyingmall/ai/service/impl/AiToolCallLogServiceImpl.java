package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.ai.dto.AiToolCallLogRequest;
import com.muyingmall.ai.entity.AiToolCallLog;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.mapper.AiToolCallLogMapper;
import com.muyingmall.ai.service.AiToolCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AI 工具调用日志服务实现。
 */
@Service
@RequiredArgsConstructor
public class AiToolCallLogServiceImpl extends ServiceImpl<AiToolCallLogMapper, AiToolCallLog>
        implements AiToolCallLogService {

    private final ObjectMapper objectMapper;

    @Override
    public AiToolCallLog recordToolCall(Integer userId, AiToolCallLogRequest request) {
        AiToolCallLog log = new AiToolCallLog();
        log.setTraceId(StringUtils.hasText(request.getTraceId()) ? request.getTraceId() : UUID.randomUUID().toString());
        log.setConversationId(request.getConversationId());
        log.setMessageId(request.getMessageId());
        log.setUserId(userId);
        log.setIntent(request.getIntent());
        log.setRiskLevel(StringUtils.hasText(request.getRiskLevel()) ? request.getRiskLevel() : AiRiskLevel.LOW.getCode());
        log.setToolName(request.getToolName());
        log.setToolType(StringUtils.hasText(request.getToolType()) ? request.getToolType() : "BUSINESS_API");
        log.setRequestPayload(toJson(request.getRequestPayload()));
        log.setResponsePayload(toJson(request.getResponsePayload()));
        log.setSuccess(Boolean.TRUE.equals(request.getSuccess()));
        log.setErrorMessage(request.getErrorMessage());
        log.setDurationMs(request.getDurationMs());
        log.setCreateTime(LocalDateTime.now());
        save(log);
        return log;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // JSON 字段必须写入合法 JSON，序列化失败时保留原始文本用于排查。
            try {
                return objectMapper.writeValueAsString(Map.of("raw", String.valueOf(value)));
            } catch (JsonProcessingException ignored) {
                return "{\"raw\":\"serialization_failed\"}";
            }
        }
    }
}
