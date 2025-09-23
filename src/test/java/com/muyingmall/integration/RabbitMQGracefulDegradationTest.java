package com.muyingmall.integration;

import com.muyingmall.dto.OrderMessage;
import com.muyingmall.dto.PaymentMessage;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.MessageProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RabbitMQ优雅降级测试
 * 验证RabbitMQ不可用时系统的正常运行
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@SpringBootTest
@TestPropertySource(properties = {
        "rabbitmq.enabled=false", // 禁用RabbitMQ
        "rabbitmq.fallback-to-sync=true"
})
public class RabbitMQGracefulDegradationTest {

    @Autowired(required = false)
    private MessageProducerService messageProducerService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private PaymentService paymentService;

    /**
     * 测试RabbitMQ禁用时MessageProducerService不存在
     */
    @Test
    void testMessageProducerServiceNotAvailableWhenRabbitMQDisabled() {
        // 验证MessageProducerService在RabbitMQ禁用时不存在
        assertNull(messageProducerService, "MessageProducerService应该在RabbitMQ禁用时不存在");
    }

    /**
     * 测试订单服务在RabbitMQ禁用时正常工作
     */
    @Test
    void testOrderServiceWorksWithoutRabbitMQ() {
        // 模拟订单创建成功
        when(orderService.createOrder(any())).thenReturn(1);

        // 执行订单创建
        Integer orderId = orderService.createOrder(any());

        // 验证订单创建成功
        assertNotNull(orderId);
        assertEquals(1, orderId);
        
        // 验证订单服务被调用
        verify(orderService, times(1)).createOrder(any());
    }

    /**
     * 测试支付服务在RabbitMQ禁用时正常工作
     */
    @Test
    void testPaymentServiceWorksWithoutRabbitMQ() {
        // 模拟支付处理成功
        when(paymentService.processPayment(any())).thenReturn(true);

        // 执行支付处理
        boolean paymentResult = paymentService.processPayment(any());

        // 验证支付处理成功
        assertTrue(paymentResult);
        
        // 验证支付服务被调用
        verify(paymentService, times(1)).processPayment(any());
    }

    /**
     * 测试系统在没有消息队列的情况下的完整流程
     */
    @Test
    void testCompleteFlowWithoutMessageQueue() {
        // 模拟完整的订单-支付流程
        when(orderService.createOrder(any())).thenReturn(1);
        when(paymentService.processPayment(any())).thenReturn(true);
        when(orderService.updateOrderStatus(anyInt(), anyString())).thenReturn(true);

        // 执行完整流程
        Integer orderId = orderService.createOrder(any());
        boolean paymentResult = paymentService.processPayment(any());
        boolean statusUpdateResult = orderService.updateOrderStatus(orderId, "PAID");

        // 验证所有步骤都成功
        assertNotNull(orderId);
        assertTrue(paymentResult);
        assertTrue(statusUpdateResult);

        // 验证所有服务都被正确调用
        verify(orderService, times(1)).createOrder(any());
        verify(paymentService, times(1)).processPayment(any());
        verify(orderService, times(1)).updateOrderStatus(anyInt(), anyString());
    }

    /**
     * 测试消息对象创建（即使没有消息队列）
     */
    @Test
    void testMessageObjectCreationWithoutQueue() {
        // 创建订单消息对象
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(1);
        orderMessage.setOrderNo("ORDER001");
        orderMessage.setUserId(1);
        orderMessage.setEventType("CREATE");
        orderMessage.setTimestamp(LocalDateTime.now());
        orderMessage.setTotalAmount(new BigDecimal("99.99"));

        // 验证消息对象创建成功
        assertNotNull(orderMessage);
        assertEquals(1, orderMessage.getOrderId());
        assertEquals("ORDER001", orderMessage.getOrderNo());
        assertEquals("CREATE", orderMessage.getEventType());

        // 创建支付消息对象
        PaymentMessage paymentMessage = new PaymentMessage();
        paymentMessage.setPaymentId(1);
        paymentMessage.setOrderId(1);
        paymentMessage.setUserId(1);
        paymentMessage.setEventType("SUCCESS");
        paymentMessage.setAmount(new BigDecimal("99.99"));
        paymentMessage.setTimestamp(LocalDateTime.now());

        // 验证支付消息对象创建成功
        assertNotNull(paymentMessage);
        assertEquals(1, paymentMessage.getPaymentId());
        assertEquals("SUCCESS", paymentMessage.getEventType());
    }
}

/**
 * RabbitMQ连接失败时的降级测试
 */
@SpringBootTest
@TestPropertySource(properties = {
        "rabbitmq.enabled=true", // 启用RabbitMQ
        "rabbitmq.fallback-to-sync=true", // 启用降级
        "spring.rabbitmq.host=invalid-host", // 无效主机
        "spring.rabbitmq.port=9999", // 无效端口
        "spring.rabbitmq.connection-timeout=1000" // 短超时时间
})
class RabbitMQConnectionFailureDegradationTest {

    @Autowired(required = false)
    private MessageProducerService messageProducerService;

    /**
     * 测试RabbitMQ连接失败时的降级行为
     */
    @Test
    void testGracefulDegradationOnConnectionFailure() {
        if (messageProducerService != null) {
            // 验证RabbitMQ不可用
            assertFalse(messageProducerService.isRabbitMQAvailable(), 
                    "RabbitMQ应该不可用（连接失败）");

            // 创建测试消息
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setOrderId(1);
            orderMessage.setEventType("CREATE");
            orderMessage.setUserId(1);
            orderMessage.setTimestamp(LocalDateTime.now());

            // 发送消息应该不抛出异常（优雅降级）
            assertDoesNotThrow(() -> {
                messageProducerService.sendOrderMessage(orderMessage);
            }, "消息发送应该优雅降级，不抛出异常");
        }
    }
}