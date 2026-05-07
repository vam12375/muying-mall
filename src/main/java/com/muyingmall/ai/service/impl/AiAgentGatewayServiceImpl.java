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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
        String url = agentBaseUrl.replaceAll("/$", "") + "/api/v1/chat/json";
        Map<String, Object> payload = buildPayload(userId, request);

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

    @Override
    public StreamingResponseBody streamChat(
            Integer userId,
            String authorizationHeader,
            AiChatRequest request,
            Consumer<AiChatResponse> completionHandler) {
        String url = agentBaseUrl.replaceAll("/$", "") + "/api/v1/chat";
        Map<String, Object> payload = buildPayload(userId, request);

        return outputStream -> {
            try {
                restTemplate.execute(
                        url,
                        HttpMethod.POST,
                        httpRequest -> {
                            httpRequest.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            httpRequest.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                            if (StringUtils.hasText(authorizationHeader)) {
                                httpRequest.getHeaders().set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                            }
                            objectMapper.writeValue(httpRequest.getBody(), payload);
                        },
                        response -> {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                                forwardSse(reader, writer, completionHandler);
                            }
                            return null;
                        });
            } catch (Exception e) {
                log.error("流式调用 FastAPI Agent 失败: {}", e.getMessage(), e);
                AiChatResponse fallback = fallbackResponse(request.getConversationId(), "AI Agent 服务暂不可用，已保留问题用于后续人工处理。");
                try {
                    // 流式链路失败也要产出完成响应，确保用户消息和兜底回复都能进入审计闭环。
                    completionHandler.accept(fallback);
                } catch (Exception persistException) {
                    log.error("记录流式 AI 兜底响应失败: {}", persistException.getMessage(), persistException);
                }
                writeSseError(outputStream, fallback);
            }
        };
    }

    private Map<String, Object> buildPayload(Integer userId, AiChatRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_id", userId);
        payload.put("conversation_id", request.getConversationId());
        payload.put("message", request.getMessage());
        payload.put("channel", request.getChannel());
        payload.put("baby_age_month", request.getBabyAgeMonth());
        payload.put("metadata", request.getMetadata());
        payload.put("history", request.getHistory());
        payload.put("max_context_chars", request.getMaxContextChars());
        return payload;
    }

    private void forwardSse(
            BufferedReader reader,
            OutputStreamWriter writer,
            Consumer<AiChatResponse> completionHandler) throws IOException {
        String currentEvent = null;
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.write("\n");
            writer.flush();

            if (line.startsWith("event:")) {
                currentEvent = line.substring("event:".length()).trim();
            } else if ("done".equals(currentEvent) && line.startsWith("data:")) {
                AiChatResponse completed = objectMapper.readValue(
                        line.substring("data:".length()).trim(),
                        AiChatResponse.class);
                try {
                    completionHandler.accept(completed);
                } catch (Exception e) {
                    log.error("记录流式 AI 响应失败: {}", e.getMessage(), e);
                }
            }
        }
    }

    private void writeSseError(OutputStream outputStream, AiChatResponse fallback) throws IOException {
        String payload = objectMapper.writeValueAsString(fallback);
        String event = "event: error\n"
                + "data: {\"message\":\"AI Agent 服务暂不可用，已返回兜底回复。\"}\n\n"
                + "event: done\n"
                + "data: " + payload + "\n\n";
        outputStream.write(event.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private AiChatResponse fallbackResponse(Long conversationId, String message) {
        AiChatResponse response = new AiChatResponse();
        response.setConversationId(conversationId);
        response.setTraceId("fallback-" + UUID.randomUUID().toString().replace("-", ""));
        response.setAnswer(message);
        response.setIntent(AiAgentIntent.UNKNOWN.getCode());
        response.setRiskLevel(AiRiskLevel.MEDIUM.getCode());
        response.setHumanHandoffRequired(true);
        response.setSuggestions(List.of("稍后重试", "联系人工客服", "查看订单列表"));
        return response;
    }
}
