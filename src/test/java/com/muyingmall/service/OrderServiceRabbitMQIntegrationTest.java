package com.muyingmall.service;

import com.muyingmall.dto.OrderMessage;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAddress;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderService RabbitMQ集成测试
 * 验证订单服务与RabbitMQ消息队列的集成是否正常工作
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceRabbitMQIntegrationTest {

    @Mock
    private MessageProducerService messageProducerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testSendOrderStatusChangeNotification_Cancel() {
        // 准备测试数据
        Order order = new Order();
        order.setOrderId(12345);
        order.setOrderNo("ORD20250918001");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("299.99"));
        order.setStatus(OrderStatus.CANCELLED);
        order.setCreateTime(LocalDateTime.now());

        String oldStatus = "pending_payment";
        String newStatus = "cancelled";

        // 使用反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = OrderServiceImpl.class.getDeclaredMethod(
                    "sendOrderStatusChangeNotification", Order.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(orderService, order, oldStatus, newStatus);

            // 验证MessageProducerService.sendOrderMessage被调用
            ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
            verify(messageProducerService, times(1)).sendOrderMessage(messageCaptor.capture());

            // 验证消息内容
            OrderMessage capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);
            assertEquals(order.getOrderId(), capturedMessage.getOrderId());
            assertEquals(order.getOrderNo(), capturedMessage.getOrderNo());
            assertEquals(order.getUserId(), capturedMessage.getUserId());
            assertEquals("CANCEL", capturedMessage.getEventType());
            assertEquals(newStatus, capturedMessage.getNewStatus());
            assertEquals(order.getTotalAmount(), capturedMessage.getTotalAmount());

            // 验证Spring事件也被发布
            verify(eventPublisher, times(1)).publishEvent(any());

        } catch (Exception e) {
            fail("测试执行失败: " + e.getMessage());
        }
    }

    @Test
    void testSendOrderStatusChangeNotification_Complete() {
        // 准备测试数据
        Order order = new Order();
        order.setOrderId(12346);
        order.setOrderNo("ORD20250918002");
        order.setUserId(1002);
        order.setTotalAmount(new BigDecimal("599.99"));
        order.setStatus(OrderStatus.COMPLETED);
        order.setCreateTime(LocalDateTime.now());

        String oldStatus = "shipped";
        String newStatus = "completed";

        // 使用反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = OrderServiceImpl.class.getDeclaredMethod(
                    "sendOrderStatusChangeNotification", Order.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(orderService, order, oldStatus, newStatus);

            // 验证MessageProducerService.sendOrderMessage被调用
            ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
            verify(messageProducerService, times(1)).sendOrderMessage(messageCaptor.capture());

            // 验证消息内容
            OrderMessage capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);
            assertEquals(order.getOrderId(), capturedMessage.getOrderId());
            assertEquals(order.getOrderNo(), capturedMessage.getOrderNo());
            assertEquals(order.getUserId(), capturedMessage.getUserId());
            assertEquals("COMPLETE", capturedMessage.getEventType());
            assertEquals(newStatus, capturedMessage.getNewStatus());
            assertEquals(order.getTotalAmount(), capturedMessage.getTotalAmount());

            // 验证Spring事件也被发布
            verify(eventPublisher, times(1)).publishEvent(any());

        } catch (Exception e) {
            fail("测试执行失败: " + e.getMessage());
        }
    }

    @Test
    void testSendOrderStatusChangeNotification_StatusChange() {
        // 准备测试数据
        Order order = new Order();
        order.setOrderId(12347);
        order.setOrderNo("ORD20250918003");
        order.setUserId(1003);
        order.setTotalAmount(new BigDecimal("199.99"));
        order.setStatus(OrderStatus.SHIPPED);
        order.setCreateTime(LocalDateTime.now());

        String oldStatus = "pending_shipment";
        String newStatus = "shipped";

        // 使用反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = OrderServiceImpl.class.getDeclaredMethod(
                    "sendOrderStatusChangeNotification", Order.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(orderService, order, oldStatus, newStatus);

            // 验证MessageProducerService.sendOrderMessage被调用
            ArgumentCaptor<OrderMessage> messageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
            verify(messageProducerService, times(1)).sendOrderMessage(messageCaptor.capture());

            // 验证消息内容
            OrderMessage capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);
            assertEquals(order.getOrderId(), capturedMessage.getOrderId());
            assertEquals(order.getOrderNo(), capturedMessage.getOrderNo());
            assertEquals(order.getUserId(), capturedMessage.getUserId());
            assertEquals("STATUS_CHANGE", capturedMessage.getEventType());
            assertEquals(oldStatus, capturedMessage.getOldStatus());
            assertEquals(newStatus, capturedMessage.getNewStatus());
            assertEquals(order.getTotalAmount(), capturedMessage.getTotalAmount());

            // 验证Spring事件也被发布
            verify(eventPublisher, times(1)).publishEvent(any());

        } catch (Exception e) {
            fail("测试执行失败: " + e.getMessage());
        }
    }

    @Test
    void testSendOrderStatusChangeNotification_MessageProducerException() {
        // 准备测试数据
        Order order = new Order();
        order.setOrderId(12348);
        order.setOrderNo("ORD20250918004");
        order.setUserId(1004);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.CANCELLED);
        order.setCreateTime(LocalDateTime.now());

        String oldStatus = "pending_payment";
        String newStatus = "cancelled";

        // 模拟MessageProducerService抛出异常
        doThrow(new RuntimeException("RabbitMQ连接失败")).when(messageProducerService).sendOrderMessage(any());

        // 使用反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = OrderServiceImpl.class.getDeclaredMethod(
                    "sendOrderStatusChangeNotification", Order.class, String.class, String.class);
            method.setAccessible(true);
            
            // 方法应该不抛出异常，即使RabbitMQ发送失败
            assertDoesNotThrow(() -> {
                try {
                    method.invoke(orderService, order, oldStatus, newStatus);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // 验证MessageProducerService.sendOrderMessage被调用
            verify(messageProducerService, times(1)).sendOrderMessage(any());

            // 验证Spring事件仍然被发布（不受RabbitMQ异常影响）
            verify(eventPublisher, times(1)).publishEvent(any());

        } catch (Exception e) {
            fail("测试执行失败: " + e.getMessage());
        }
    }
}