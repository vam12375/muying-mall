package com.muyingmall.service.impl;

import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.fixtures.PaymentFixtures;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.PaymentStateLogService;
import com.muyingmall.statemachine.PaymentEvent;
import com.muyingmall.statemachine.PaymentStateContext;
import com.muyingmall.statemachine.PaymentStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 支付状态机 · 单元测试。
 * 覆盖：
 *   - 合法事件流转（PENDING + SUCCESS → SUCCESS）
 *   - 非法事件流转（SUCCESS + CREATE → 抛 BusinessException）
 *   - 支付记录不存在
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("支付状态机 · 单元测试")
class PaymentStateServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentStateMachine paymentStateMachine;

    @Mock
    private PaymentStateLogService paymentStateLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentStateServiceImpl stateService;

    @Nested
    @DisplayName("sendEvent(paymentId, event, operator, reason) · 根据支付ID流转状态")
    class SendEventByPaymentId {

        @Test
        @DisplayName("合法事件 SUCCESS 应将 PENDING 流转到 SUCCESS 并记录日志、发事件")
        void sendEvent_shouldTransitionAndLog_whenEventAllowed() {
            // Given
            Payment payment = PaymentFixtures.pending(500L, 1001, 9, new BigDecimal("100"));
            given(paymentService.getById(500L)).willReturn(payment);
            given(paymentStateMachine.sendEvent(eq(PaymentStatus.PENDING), eq(PaymentEvent.SUCCESS), any(PaymentStateContext.class)))
                    .willAnswer(invocation -> {
                        PaymentStateContext ctx = invocation.getArgument(2);
                        ctx.setOldStatus(PaymentStatus.PENDING);
                        ctx.setNewStatus(PaymentStatus.SUCCESS);
                        ctx.setEvent(PaymentEvent.SUCCESS);
                        return PaymentStatus.SUCCESS;
                    });
            given(paymentService.updateById(any(Payment.class))).willReturn(true);

            // When
            Payment result = stateService.sendEvent(500L, PaymentEvent.SUCCESS, "SYSTEM", "支付回调成功");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.getPaymentTime()).isNotNull();

            ArgumentCaptor<PaymentStateContext> logCaptor = ArgumentCaptor.forClass(PaymentStateContext.class);
            verify(paymentStateLogService).recordStateChange(logCaptor.capture());
            PaymentStateContext logCtx = logCaptor.getValue();
            assertThat(logCtx.getOldStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(logCtx.getNewStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(logCtx.getOperator()).isEqualTo("SYSTEM");
            assertThat(logCtx.getReason()).isEqualTo("支付回调成功");

            verify(eventPublisher).publishEvent(any(PaymentStateServiceImpl.PaymentStateChangedEvent.class));
        }

        @Test
        @DisplayName("非法事件（SUCCESS 下再发 CREATE）应抛 BusinessException 且不记录日志")
        void sendEvent_shouldThrow_whenEventNotAllowed() {
            // Given
            Payment payment = PaymentFixtures.success(500L, 1001, 9, new BigDecimal("100"));
            given(paymentService.getById(500L)).willReturn(payment);
            given(paymentStateMachine.sendEvent(eq(PaymentStatus.SUCCESS), eq(PaymentEvent.CREATE), any(PaymentStateContext.class)))
                    .willThrow(new BusinessException("不支持的状态转换：从[成功]状态触发[CREATE]事件"));

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(500L, PaymentEvent.CREATE, "SYSTEM", "异常操作"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("支付状态转换失败");

            verify(paymentStateLogService, never()).recordStateChange(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("支付记录不存在时应抛 BusinessException")
        void sendEvent_shouldThrow_whenPaymentNotFound() {
            // Given
            given(paymentService.getById(9999L)).willReturn(null);

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(9999L, PaymentEvent.SUCCESS, "SYSTEM", "支付"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("支付记录不存在");

            verify(paymentStateMachine, never()).sendEvent(any(), any(), any());
        }
    }
}
