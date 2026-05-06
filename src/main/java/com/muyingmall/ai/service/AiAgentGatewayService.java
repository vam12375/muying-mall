package com.muyingmall.ai.service;

import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;

/**
 * Spring Boot 到 FastAPI Agent 的网关服务。
 */
public interface AiAgentGatewayService {

    AiChatResponse chat(Integer userId, String authorizationHeader, AiChatRequest request);
}
