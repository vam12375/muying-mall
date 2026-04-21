package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.fixtures.PaymentFixtures;
import com.muyingmall.mapper.PaymentMapper;
import com.muyingmall.service.MessageProducerService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 支付服务 · 单元测试。
 * 覆盖：
 *   - createPayment：将 Payment 透传到 baseMapper.insert，并返回同一对象
 *   - getByPaymentNo：支付号不存在时返回 null
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("支付服务 · 单元测试")
class PaymentServiceImplTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private MessageProducerService messageProducerService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "baseMapper", paymentMapper);
    }

    @Nested
    @DisplayName("createPayment(payment)")
    class CreatePayment {

        @Test
        @DisplayName("应通过 baseMapper.insert 持久化并透传实体")
        void createPayment_shouldInsertAndReturnSameEntity() {
            // Given
            Payment input = PaymentFixtures.pending(1L, 1001, 9, new BigDecimal("100"));
            given(paymentMapper.insert(any(Payment.class))).willReturn(1);

            // When
            Payment result = paymentService.createPayment(input);

            // Then
            assertThat(result).isSameAs(input);

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentMapper).insert(captor.capture());
            Payment persisted = captor.getValue();
            assertThat(persisted.getId()).isEqualTo(1L);
            assertThat(persisted.getOrderId()).isEqualTo(1001);
            assertThat(persisted.getUserId()).isEqualTo(9);
            assertThat(persisted.getAmount()).isEqualByComparingTo("100");
            assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("getByPaymentNo(paymentNo)")
    class GetByPaymentNo {

        @Test
        @DisplayName("支付号存在时应返回记录")
        void getByPaymentNo_shouldReturnPayment_whenExists() {
            // Given
            Payment payment = PaymentFixtures.success(1L, 1001, 9, new BigDecimal("100"));
            given(paymentMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(payment);

            // When
            Payment result = paymentService.getByPaymentNo("PAY1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("支付号不存在时应返回 null")
        void getByPaymentNo_shouldReturnNull_whenNotFound() {
            // Given
            given(paymentMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(null);

            // When
            Payment result = paymentService.getByPaymentNo("NOT_EXIST");

            // Then
            assertThat(result).isNull();
        }
    }
}
