package com.muyingmall.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@Tag("unit")
@DisplayName("FastAPI Agent 网关")
class AiAgentGatewayServiceImplTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private AiAgentGatewayServiceImpl service;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new AiAgentGatewayServiceImpl(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(service, "agentBaseUrl", "http://localhost:8001");
    }

    @Test
    @DisplayName("非流式调用应走 FastAPI JSON 兼容入口")
    void chat_shouldUseJsonCompatibilityEndpoint() {
        AiChatRequest request = new AiChatRequest();
        request.setConversationId(7L);
        request.setMessage("你好");
        request.setChannel("WEB");

        server.expect(once(), requestTo("http://localhost:8001/api/v1/chat/json"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("\"message\":\"你好\"")))
                .andRespond(withSuccess("""
                        {
                          "conversationId": 7,
                          "traceId": "trace-1",
                          "answer": "你好",
                          "intent": "UNKNOWN",
                          "riskLevel": "LOW",
                          "humanHandoffRequired": false,
                          "suggestions": [],
                          "toolResults": {}
                        }
                        """, MediaType.APPLICATION_JSON));

        AiChatResponse response = service.chat(1, "Bearer token", request);

        assertThat(response.getAnswer()).isEqualTo("你好");
        assertThat(response.getTraceId()).isEqualTo("trace-1");
        server.verify();
    }

    @Test
    @DisplayName("流式调用应转发 SSE 并解析 done 事件")
    void streamChat_shouldProxySseAndPublishDoneResponse() throws Exception {
        AiChatRequest request = new AiChatRequest();
        request.setConversationId(7L);
        request.setMessage("你好");
        request.setChannel("WEB");
        AtomicReference<AiChatResponse> completed = new AtomicReference<>();

        server.expect(once(), requestTo("http://localhost:8001/api/v1/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(content().string(containsString("\"message\":\"你好\"")))
                .andRespond(withSuccess("""
                        event: delta
                        data: {"content":"你"}

                        event: delta
                        data: {"content":"好"}

                        event: done
                        data: {"conversationId":7,"traceId":"trace-2","answer":"你好","intent":"UNKNOWN","riskLevel":"LOW","humanHandoffRequired":false,"suggestions":[],"toolResults":{}}

                        """, MediaType.TEXT_EVENT_STREAM));

        StreamingResponseBody body = service.streamChat(1, "Bearer token", request, completed::set);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        body.writeTo(outputStream);

        String forwarded = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(forwarded).contains("event: delta");
        assertThat(forwarded).contains("data: {\"content\":\"你\"}");
        assertThat(forwarded).contains("event: done");
        assertThat(completed.get()).isNotNull();
        assertThat(completed.get().getAnswer()).isEqualTo("你好");
        assertThat(completed.get().getTraceId()).isEqualTo("trace-2");
        server.verify();
    }

    @Test
    @DisplayName("流式调用失败时应生成可落库的兜底响应")
    void streamChat_shouldPublishFallbackResponseWhenAgentFails() throws Exception {
        AiChatRequest request = new AiChatRequest();
        request.setConversationId(7L);
        request.setMessage("你好");
        request.setChannel("WEB");
        AtomicReference<AiChatResponse> completed = new AtomicReference<>();

        server.expect(once(), requestTo("http://localhost:8001/api/v1/chat"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        StreamingResponseBody body = service.streamChat(1, "Bearer token", request, completed::set);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        body.writeTo(outputStream);

        String forwarded = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(forwarded).contains("event: error");
        assertThat(forwarded).contains("event: done");
        assertThat(completed.get()).isNotNull();
        assertThat(completed.get().getConversationId()).isEqualTo(7L);
        assertThat(completed.get().getHumanHandoffRequired()).isTrue();
        server.verify();
    }
}
