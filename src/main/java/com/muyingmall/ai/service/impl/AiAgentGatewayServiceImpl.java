package com.muyingmall.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import com.muyingmall.ai.enums.AiAgentIntent;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.service.AiAgentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FastAPI Agent 网关实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAgentGatewayServiceImpl implements AiAgentGatewayService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.agent.base-url:http://localhost:8001}")
    private String agentBaseUrl;

    @Override
    public AiChatResponse chat(Integer userId, String authorizationHeader, AiChatRequest request) {
        String url = agentBaseUrl.replaceAll("/$", "") + "/api/v1/chat";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_id", userId);
        payload.put("conversation_id", request.getConversationId());
        payload.put("message", request.getMessage());
        payload.put("channel", request.getChannel());
        payload.put("baby_age_month", request.getBabyAgeMonth());
        payload.put("metadata", request.getMetadata());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Map.class);
            Map<?, ?> body = response.getBody();
            if (body == null) {
                return fallbackResponse(request.getConversationId(), "AI Agent 服务返回为空。");
            }
            return objectMapper.convertValue(body, AiChatResponse.class);
        } catch (IllegalArgumentException | RestClientException e) {
            log.error("调用 FastAPI Agent 失败: {}", e.getMessage(), e);
            return fallbackResponse(request.getConversationId(), "AI Agent 服务暂不可用，已保留问题用于后续人工处理。");
        }
    }

    private AiChatResponse fallbackResponse(Long conversationId, String message) {
        AiChatResponse response = new AiChatResponse();
        response.setConversationId(conversationId);
        response.setAnswer(message);
        response.setIntent(AiAgentIntent.UNKNOWN.getCode());
        response.setRiskLevel(AiRiskLevel.MEDIUM.getCode());
        response.setHumanHandoffRequired(true);
        response.setSuggestions(List.of("稍后重试", "联系人工客服", "查看订单列表"));
        return response;
    }
}
