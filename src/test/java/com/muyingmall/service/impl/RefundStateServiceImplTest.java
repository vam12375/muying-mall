package com.muyingmall.service.impl;

import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.fixtures.OrderFixtures;
import com.muyingmall.fixtures.RefundFixtures;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.RefundLogService;
import com.muyingmall.service.RefundService;
import com.muyingmall.statemachine.OrderStateMachine;
import com.muyingmall.statemachine.RefundEvent;
import com.muyingmall.statemachine.RefundStateContext;
import com.muyingmall.statemachine.RefundStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 退款状态机 · 单元测试。
 * 覆盖：
 *   - 合法事件（PENDING + APPROVE → APPROVED）
 *   - 非法事件（REJECTED + APPROVE → 状态机抛异常）
 *   - 退款申请不存在
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("退款状态机 · 单元测试")
class RefundStateServiceImplTest {

    @Mock
    private RefundService refundService;

    @Mock
    private RefundLogService refundLogService;

    @Mock
    private OrderService orderService;

    @Mock
    private RefundStateMachine refundStateMachine;

    @Mock
    private OrderStateMachine orderStateMachine;

    @InjectMocks
    private RefundStateServiceImpl stateService;

    @Nested
    @DisplayName("sendEvent(refundId, event, operatorType, operatorName, operatorId, reason)")
    class SendEventByRefundId {

        @Test
        @DisplayName("合法事件 APPROVE 应将 PENDING 流转到 APPROVED 并记录日志")
        void sendEvent_shouldTransitionAndLog_whenEventAllowed() {
            // Given
            Refund refund = RefundFixtures.pending(100L, 1001, 9, new BigDecimal("50"));
            given(refundService.getById(100L)).willReturn(refund);
            given(refundStateMachine.sendEvent(eq(RefundStatus.PENDING), eq(RefundEvent.APPROVE), any(RefundStateContext.class)))
                    .willReturn(RefundStatus.APPROVED);
            given(refundService.updateById(any(Refund.class))).willReturn(true);
            // APPROVE 不触发 handleSpecialEvents 中的订单状态变更分支（只处理 SUBMIT/COMPLETE/FAIL），
            // 但仍会查询订单，返回已发货订单即可平静通过。
            Order order = OrderFixtures.withStatus(1001, 9, new BigDecimal("100"), OrderStatus.SHIPPED);
            given(orderService.getById(1001)).willReturn(order);

            // When
            boolean success = stateService.sendEvent(100L, RefundEvent.APPROVE, "ADMIN", "admin01", 1, "审核通过");

            // Then
            assertThat(success).isTrue();
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED.getCode());

            verify(refundLogService).logStatusChange(
                    eq(100L),
                    eq(refund.getRefundNo()),
                    eq(RefundStatus.PENDING.getCode()),
                    eq(RefundStatus.APPROVED.getCode()),
                    eq("ADMIN"),
                    eq(1),
                    eq("admin01"),
                    eq("审核通过"));

            // APPROVE 不会触发订单状态机更新
            verify(orderStateMachine, never()).sendEvent(any(), any(), any());
        }

        @Test
        @DisplayName("非法事件（状态机抛异常）应包装为 BusinessException 且不记录状态变更日志")
        void sendEvent_shouldThrow_whenEventNotAllowed() {
            // Given: 已拒绝状态下再次 APPROVE
            Refund refund = RefundFixtures.withStatus(100L, 1001, 9, new BigDecimal("50"),
                    RefundStatus.REJECTED.getCode());
            given(refundService.getById(100L)).willReturn(refund);
            given(refundStateMachine.getPossibleNextStates(RefundStatus.REJECTED))
                    .willReturn(new RefundStatus[0]);
            given(refundStateMachine.sendEvent(eq(RefundStatus.REJECTED), eq(RefundEvent.APPROVE), any(RefundStateContext.class)))
                    .willThrow(new BusinessException("不支持的状态转换：从[已拒绝]状态触发[APPROVE]事件"));

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(100L, RefundEvent.APPROVE, "ADMIN", "admin01", 1, "误操作"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("退款状态变更失败");

            // 状态变更日志不应记录（因为转换失败）
            verify(refundLogService, never()).logStatusChange(any(), any(), any(), any(), any(), any(), any(), any());
            verify(refundService, never()).updateById(any(Refund.class));
        }

        @Test
        @DisplayName("退款申请不存在时应抛 BusinessException")
        void sendEvent_shouldThrow_whenRefundNotFound() {
            // Given
            given(refundService.getById(9999L)).willReturn(null);

            // When / Then
            assertThatThrownBy(() -> stateService.sendEvent(9999L, RefundEvent.APPROVE, "ADMIN", "admin01", 1, "审核"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("退款申请不存在");

            verify(refundStateMachine, never()).sendEvent(any(), any(), any());
            verify(refundLogService, never()).logStatusChange(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }
}
