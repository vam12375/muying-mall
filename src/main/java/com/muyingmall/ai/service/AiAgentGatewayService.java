package com.muyingmall.ai.service;

import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.function.Consumer;

/**
 * Spring Boot 到 FastAPI Agent 的网关服务。
 */
public interface AiAgentGatewayService {

    AiChatResponse chat(Integer userId, String authorizationHeader, AiChatRequest request);

    StreamingResponseBody streamChat(
            Integer userId,
            String authorizationHeader,
            AiChatRequest request,
            Consumer<AiChatResponse> completionHandler);
}
