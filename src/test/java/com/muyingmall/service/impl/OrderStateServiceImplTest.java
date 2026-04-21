package com.muyingmall.service.impl;

import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.fixtures.OrderFixtures;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.OrderStateLogService;
import com.muyingmall.statemachine.OrderEvent;
import com.muyingmall.statemachine.OrderStateContext;
import com.muyingmall.statemachine.OrderStateMachine;
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
 * 订单状态机 · 单元测试。
 * 覆盖：
 *   - 合法事件流转（PENDING_PAYMENT + PAID → PENDING_SHIPMENT）
 *   - 非法事件流转（SHIPPED + PAID → 抛 BusinessException）
 *   - 订单不存在
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("订单状态机 · 单元测试")
class OrderStateServiceImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStateMachine orderStateMachine;

    @Mock
    private OrderStateLogService orderStateLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderStateServiceImpl stateService;

    @Nested
    @DisplayName("sendEvent(orderId, event, operator, reason) · 根据订单ID流转状态")
    class SendEventByOrderId {

        @Test
        @DisplayName("合法事件 PAID 应将 PENDING_PAYMENT 流转到 PENDING_SHIPMENT 并记录日志、发布事件")
        void sendEvent_shouldTransitionAndLog_whenEventAllowed() {
            // Given
            Order order = OrderFixtures.pendingPayment(1001, 9, new BigDecimal("100"));
            given(orderService.getById(1001)).willReturn(order);
            given(orderStateMachine.sendEvent(eq(OrderStatus.PENDING_PAYMENT), eq(OrderEvent.PAID), any(OrderStateContext.class)))
                    .willAnswer(invocation -> {
                        OrderStateContext ctx = invocation.getArgument(2);
                        ctx.setOldStatus(OrderStatus.PENDING_PAYMENT);
                        ctx.setNewStatus(OrderStatus.PENDING_SHIPMENT);
                        ctx.setEvent(OrderEvent.PAID);
                        return OrderStatus.PENDING_SHIPMENT;
                    });
            given(orderService.updateById(any(Order.class))).willReturn(true);

            // When
            Order result = stateService.sendEvent(1001, OrderEvent.PAID, "USER:9", "支付完成");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_SHIPMENT);
            assertThat(result.getPayTime()).isNotNull();

            ArgumentCaptor<OrderStateContext> logCaptor = ArgumentCaptor.forClass(OrderStateContext.class);
            verify(orderStateLogService).recordStateChange(logCaptor.capture());
            OrderStateContext logCtx = logCaptor.getValue();
            assertThat(logCtx.getOldStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(logCtx.getNewStatus()).isEqualTo(OrderStatus.PENDING_SHIPMENT);
            assertThat(logCtx.getOperator()).isEqualTo("USER:9");
            assertThat(logCtx.getReason()).isEqualTo("支付完成");

            verify(eventPublisher).publishEvent(any(OrderStateServiceImpl.OrderStateChangedEvent.class));
        }

        @Test
        @DisplayName("非法事件（已发货状态再次 PAID）应抛 BusinessException 且不记录日志不发事件")
        void sendEvent_shouldThrow_whenEventNotAllowed() {
            // Given
            Order order = OrderFixtures.withStatus(1001, 9, new BigDecimal("100"), OrderStatus.SHIPPED);
            given(orderService.getById(1001)).willReturn(order);
            given(orderStateMachine.sendEvent(eq(OrderStatus.SHIPPED), eq(OrderEvent.PAID), any(OrderStateContext.class)))
                    .willThrow(new BusinessException("不支持的状态转换：从[已发货]状态触发[PAID]事件"));

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(1001, OrderEvent.PAID, "USER:9", "重复支付"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单状态转换失败");

            verify(orderStateLogService, never()).recordStateChange(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("订单不存在时应抛 BusinessException")
        void sendEvent_shouldThrow_whenOrderNotFound() {
            // Given
            given(orderService.getById(9999)).willReturn(null);

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(9999, OrderEvent.PAID, "USER:9", "支付"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单不存在");

            verify(orderStateMachine, never()).sendEvent(any(), any(), any());
            verify(orderStateLogService, never()).recordStateChange(any());
        }
    }
}
