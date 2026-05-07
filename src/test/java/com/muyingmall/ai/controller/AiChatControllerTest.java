package com.muyingmall.ai.controller;

import com.muyingmall.ai.dto.AiChatRequest;
import com.muyingmall.ai.dto.AiChatResponse;
import com.muyingmall.ai.entity.AiConversation;
import com.muyingmall.ai.service.AiAgentGatewayService;
import com.muyingmall.ai.service.AiConversationService;
import com.muyingmall.ai.service.AiMessageService;
import com.muyingmall.common.api.Result;
import com.muyingmall.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("用户 AI 聊天控制器")
class AiChatControllerTest {

    @Mock
    private UserContext userContext;
    @Mock
    private AiConversationService aiConversationService;
    @Mock
    private AiMessageService aiMessageService;
    @Mock
    private AiAgentGatewayService aiAgentGatewayService;
    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private AiChatController controller;

    @Test
    @DisplayName("默认聊天入口应返回 SSE 并在 done 后记录助手消息")
    void chat_shouldStreamAndPersistAssistantMessageOnDone() throws Exception {
        AiChatRequest request = request();
        AiConversation conversation = conversation();
        given(userContext.getCurrentUserId()).willReturn(9);
        given(aiConversationService.getOrCreateConversation(9, null, "WEB", "你好")).willReturn(conversation);
        given(servletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer token");
        given(aiAgentGatewayService.streamChat(eq(9), eq("Bearer token"), same(request), any()))
                .willAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Consumer<AiChatResponse> completionHandler = invocation.getArgument(3, Consumer.class);
                    return (StreamingResponseBody) outputStream -> {
                        AiChatResponse completed = completedResponse();
                        completionHandler.accept(completed);
                        outputStream.write("""
                                event: done
                                data: {"answer":"流式回答"}

                                """.getBytes(StandardCharsets.UTF_8));
                    };
                });

        ResponseEntity<StreamingResponseBody> response = controller.chat(request, servletRequest);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getBody().writeTo(outputStream);

        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_EVENT_STREAM);
        assertThat(outputStream.toString(StandardCharsets.UTF_8)).contains("event: done");
        verify(aiMessageService).recordMessage(7L, 9, "USER", "你好", null, "LOW");
        verify(aiMessageService).recordMessage(7L, 9, "ASSISTANT", "流式回答", "UNKNOWN", "LOW", null);
        verify(aiConversationService).refreshConversation(7L, "UNKNOWN", "LOW", "你好", "OPEN");
    }

    @Test
    @DisplayName("JSON 兼容入口应保持原 Result 响应结构")
    void chatJson_shouldKeepOriginalResultShape() {
        AiChatRequest request = request();
        AiConversation conversation = conversation();
        AiChatResponse completed = completedResponse();
        given(userContext.getCurrentUserId()).willReturn(9);
        given(aiConversationService.getOrCreateConversation(9, null, "WEB", "你好")).willReturn(conversation);
        given(servletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer token");
        given(aiAgentGatewayService.chat(9, "Bearer token", request)).willReturn(completed);

        Result<AiChatResponse> result = controller.chatJson(request, servletRequest);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getAnswer()).isEqualTo("流式回答");
        verify(aiMessageService).recordMessage(7L, 9, "ASSISTANT", "流式回答", "UNKNOWN", "LOW", null);
    }

    private AiChatRequest request() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("你好");
        request.setChannel("WEB");
        return request;
    }

    private AiConversation conversation() {
        AiConversation conversation = new AiConversation();
        conversation.setId(7L);
        return conversation;
    }

    private AiChatResponse completedResponse() {
        AiChatResponse response = new AiChatResponse();
        response.setConversationId(7L);
        response.setTraceId("trace-1");
        response.setAnswer("流式回答");
        response.setIntent("UNKNOWN");
        response.setRiskLevel("LOW");
        response.setHumanHandoffRequired(false);
        return response;
    }
}
