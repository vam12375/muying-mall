package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.fixtures.OrderFixtures;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.RefundMapper;
import com.muyingmall.service.AlipayRefundService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.service.RefundLogService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.service.UserService;
import com.muyingmall.statemachine.RefundEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 退款服务 · 单元测试。
 * 被测方法：{@link RefundServiceImpl#applyRefund}
 * 覆盖：
 *   - 黄金路径：订单状态允许退款 + 金额合法 + 无进行中申请 → 落库并返回退款ID
 *   - 边界：退款金额超过订单实付金额 → 抛 BusinessException
 *   - 边界：订单不存在 → 抛 BusinessException
 *   - 边界：订单归属不匹配 → 抛 BusinessException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("退款服务 · 单元测试")
class RefundServiceImplTest {

    @Mock
    private RefundMapper refundMapper;

    @Mock
    private OrderService orderService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserService userService;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private RefundStateService refundStateService;
    @Mock
    private RefundLogService refundLogService;
    @Mock
    private AlipayRefundService alipayRefundService;
    @Mock
    private OrderProductMapper orderProductMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private RefundServiceImpl refundService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refundService, "baseMapper", refundMapper);
    }

    @Nested
    @DisplayName("applyRefund(orderId, userId, amount, reason, reasonDetail, evidenceImages)")
    class ApplyRefund {

        @Test
        @DisplayName("订单已支付且金额合法时应落库退款并返回 refundId")
        void applyRefund_shouldPersistAndReturnId_whenGoldenPath() {
            // Given
            Order order = OrderFixtures.paid(1001, 9, new BigDecimal("100"));
            given(orderService.getById(1001)).willReturn(order);
            given(refundMapper.selectCount(any(Wrapper.class))).willReturn(0L);
            // 模拟 insert 时为实体分配主键
            given(refundMapper.insert(any(Refund.class))).willAnswer(invocation -> {
                Refund entity = invocation.getArgument(0);
                entity.setId(555L);
                return 1;
            });
            given(refundStateService.sendEvent(anyLong(), eq(RefundEvent.SUBMIT),
                    anyString(), anyString(), anyInt(), anyString())).willReturn(true);
            given(orderService.updateById(any(Order.class))).willReturn(true);

            // When
            Long refundId = refundService.applyRefund(
                    1001, 9, new BigDecimal("50"), "质量问题", "包装破损", "img1.jpg");

            // Then
            assertThat(refundId).isEqualTo(555L);

            ArgumentCaptor<Refund> captor = ArgumentCaptor.forClass(Refund.class);
            verify(refundMapper).insert(captor.capture());
            Refund persisted = captor.getValue();
            assertThat(persisted.getOrderId()).isEqualTo(1001);
            assertThat(persisted.getUserId()).isEqualTo(9);
            assertThat(persisted.getAmount()).isEqualByComparingTo("50");
            assertThat(persisted.getRefundReason()).isEqualTo("质量问题");
            assertThat(persisted.getRefundReasonDetail()).isEqualTo("包装破损");
            assertThat(persisted.getStatus()).isEqualTo(RefundStatus.PENDING.getCode());
            assertThat(persisted.getRefundNo()).startsWith("R");

            verify(refundStateService).sendEvent(eq(555L), eq(RefundEvent.SUBMIT),
                    eq("USER"), anyString(), eq(9), anyString());
        }

        @Test
        @DisplayName("退款金额超过实付金额时应抛 BusinessException 且不落库")
        void applyRefund_shouldThrow_whenAmountExceedsActual() {
            // Given
            Order order = OrderFixtures.paid(1001, 9, new BigDecimal("100"));
            given(orderService.getById(1001)).willReturn(order);

            // When / Then
            assertThatThrownBy(() -> refundService.applyRefund(
                    1001, 9, new BigDecimal("200"), "质量问题", "破损", "img.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("退款金额不能超过实付金额");

            verify(refundMapper, never()).insert(any(Refund.class));
            verify(refundStateService, never()).sendEvent(anyLong(), any(), anyString(), anyString(), anyInt(), anyString());
        }

        @Test
        @DisplayName("订单不存在时应抛 BusinessException")
        void applyRefund_shouldThrow_whenOrderNotFound() {
            // Given
            given(orderService.getById(9999)).willReturn(null);

            // When / Then
            assertThatThrownBy(() -> refundService.applyRefund(
                    9999, 9, new BigDecimal("50"), "质量问题", "破损", "img.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单不存在");

            verify(refundMapper, never()).insert(any(Refund.class));
        }

        @Test
        @DisplayName("订单归属不匹配时应抛 BusinessException")
        void applyRefund_shouldThrow_whenUserIdMismatch() {
            // Given
            Order order = OrderFixtures.paid(1001, 9, new BigDecimal("100"));
            given(orderService.getById(1001)).willReturn(order);

            // When / Then
            assertThatThrownBy(() -> refundService.applyRefund(
                    1001, 777, new BigDecimal("50"), "质量问题", "破损", "img.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权限");

            verify(refundMapper, never()).insert(any(Refund.class));
        }

        @Test
        @DisplayName("订单处于待支付/已取消状态时应抛 BusinessException")
        void applyRefund_shouldThrow_whenOrderStatusDisallowed() {
            // Given
            Order order = OrderFixtures.withStatus(1001, 9, new BigDecimal("100"), OrderStatus.PENDING_PAYMENT);
            given(orderService.getById(1001)).willReturn(order);

            // When / Then
            assertThatThrownBy(() -> refundService.applyRefund(
                    1001, 9, new BigDecimal("50"), "质量问题", "破损", "img.jpg"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("当前订单状态不允许退款");

            verify(refundMapper, never()).insert(any(Refund.class));
        }
    }
}
