package com.muyingmall.service;

import com.muyingmall.common.constants.RabbitMQConstants;
import com.muyingmall.config.properties.RabbitMQProperties;
import com.muyingmall.dto.OrderMessage;
import com.muyingmall.dto.PaymentMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息发送服务
 * 封装RabbitMQ消息发送逻辑，支持订单和支付消息的发送
 * 实现混合模式：同时发送RabbitMQ消息和Redis通知
 * 提供优雅降级机制：RabbitMQ不可用时回退到Redis通知
 * 
 * @author MuyingMall
 * @since 2025-09-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderNotificationService orderNotificationService;
    private final RabbitMQProperties rabbitMQProperties;
    private final ConnectionFactory connectionFactory;

    /**
     * RabbitMQ连接状态监控
     */
    private final AtomicBoolean isRabbitMQAvailable = new AtomicBoolean(true);
    
    /**
     * 最后一次连接检查时间
     */
    private final AtomicLong lastConnectionCheckTime = new AtomicLong(0);
    
    /**
     * 连接检查间隔（毫秒）
     */
    private static final long CONNECTION_CHECK_INTERVAL = 30000L; // 30秒

    /**
     * 检查RabbitMQ连接状态
     * 
     * @return true如果连接可用，false如果连接不可用
     */
    private boolean checkRabbitMQConnection() {
        long currentTime = System.currentTimeMillis();
        
        // 如果距离上次检查时间不足间隔，直接返回缓存的状态
        if (currentTime - lastConnectionCheckTime.get() < CONNECTION_CHECK_INTERVAL) {
            return isRabbitMQAvailable.get();
        }
        
        try {
            // 尝试获取连接来测试连接状态
            connectionFactory.createConnection().close();
            isRabbitMQAvailable.set(true);
            lastConnectionCheckTime.set(currentTime);
            
            if (log.isDebugEnabled()) {
                log.debug("RabbitMQ连接状态检查成功");
            }
            return true;
        } catch (Exception e) {
            isRabbitMQAvailable.set(false);
            lastConnectionCheckTime.set(currentTime);
            log.warn("RabbitMQ连接状态检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前RabbitMQ可用状态
     * 
     * @return true如果RabbitMQ可用，false如果不可用
     */
    public boolean isRabbitMQAvailable() {
        return rabbitMQProperties.isEnabled() && checkRabbitMQConnection();
    }

    /**
     * 发送订单消息
     * 支持订单消息发送和Redis通知的混合模式
     * 增强的优雅降级机制
     * 
     * @param message 订单消息
     */
    public void sendOrderMessage(OrderMessage message) {
        if (message == null) {
            log.warn("订单消息为空，跳过发送");
            return;
        }

        log.debug("开始发送订单消息: orderId={}, eventType={}, userId={}", 
                message.getOrderId(), message.getEventType(), message.getUserId());

        boolean rabbitmqSuccess = false;
        boolean shouldUseRabbitMQ = isRabbitMQAvailable();

        // 尝试发送RabbitMQ消息
        if (shouldUseRabbitMQ) {
            rabbitmqSuccess = sendOrderMessageToRabbitMQ(message);
        } else {
            log.debug("RabbitMQ不可用，直接使用Redis通知: orderId={}", message.getOrderId());
        }

        // 发送Redis通知（保留现有机制）
        sendOrderNotificationToRedis(message);

        // 记录发送结果
        logOrderMessageResult(message, shouldUseRabbitMQ, rabbitmqSuccess);
    }

    /**
     * 发送支付消息
     * 支持支付消息发送和Redis通知的混合模式
     * 增强的优雅降级机制
     * 
     * @param message 支付消息
     */
    public void sendPaymentMessage(PaymentMessage message) {
        if (message == null) {
            log.warn("支付消息为空，跳过发送");
            return;
        }

        log.debug("开始发送支付消息: paymentId={}, eventType={}, orderId={}, userId={}", 
                message.getPaymentId(), message.getEventType(), message.getOrderId(), message.getUserId());

        boolean rabbitmqSuccess = false;
        boolean shouldUseRabbitMQ = isRabbitMQAvailable();

        // 尝试发送RabbitMQ消息
        if (shouldUseRabbitMQ) {
            rabbitmqSuccess = sendPaymentMessageToRabbitMQ(message);
        } else {
            log.debug("RabbitMQ不可用，直接使用Redis通知: paymentId={}", message.getPaymentId());
        }

        // 发送Redis通知（保留现有机制）
        sendPaymentNotificationToRedis(message);

        // 记录发送结果
        logPaymentMessageResult(message, shouldUseRabbitMQ, rabbitmqSuccess);
    }

    /**
     * 记录订单消息发送结果
     */
    private void logOrderMessageResult(OrderMessage message, boolean shouldUseRabbitMQ, boolean rabbitmqSuccess) {
        if (shouldUseRabbitMQ && rabbitmqSuccess) {
            log.debug("订单消息发送成功: orderId={}, 使用RabbitMQ+Redis混合模式", message.getOrderId());
        } else if (shouldUseRabbitMQ && !rabbitmqSuccess && rabbitMQProperties.isFallbackToSync()) {
            log.warn("订单消息RabbitMQ发送失败，已回退到Redis通知: orderId={}", message.getOrderId());
        } else if (!shouldUseRabbitMQ) {
            log.debug("订单消息发送成功: orderId={}, 使用Redis通知模式", message.getOrderId());
        }
    }

    /**
     * 记录支付消息发送结果
     */
    private void logPaymentMessageResult(PaymentMessage message, boolean shouldUseRabbitMQ, boolean rabbitmqSuccess) {
        if (shouldUseRabbitMQ && rabbitmqSuccess) {
            log.debug("支付消息发送成功: paymentId={}, 使用RabbitMQ+Redis混合模式", message.getPaymentId());
        } else if (shouldUseRabbitMQ && !rabbitmqSuccess && rabbitMQProperties.isFallbackToSync()) {
            log.warn("支付消息RabbitMQ发送失败，已回退到Redis通知: paymentId={}", message.getPaymentId());
        } else if (!shouldUseRabbitMQ) {
            log.debug("支付消息发送成功: paymentId={}, 使用Redis通知模式", message.getPaymentId());
        }
    }

    /**
     * 发送订单消息到RabbitMQ
     * 
     * @param message 订单消息
     * @return 发送是否成功
     */
    private boolean sendOrderMessageToRabbitMQ(OrderMessage message) {
        int retryCount = 0;
        Exception lastException = null;
        int maxRetryCount = rabbitMQProperties.getErrorHandling().getMaxRetryAttempts();

        while (retryCount < maxRetryCount) {
            try {
                String routingKey = generateOrderRoutingKey(message);
                
                log.debug("发送订单消息到RabbitMQ: exchange={}, routingKey={}, orderId={}", 
                        RabbitMQConstants.ORDER_EXCHANGE, routingKey, message.getOrderId());

                rabbitTemplate.convertAndSend(
                        RabbitMQConstants.ORDER_EXCHANGE, 
                        routingKey, 
                        message
                );



                log.debug("订单消息RabbitMQ发送成功: orderId={}, routingKey={}", 
                        message.getOrderId(), routingKey);
                return true;

            } catch (AmqpException e) {
                lastException = e;
                retryCount++;
                log.warn("订单消息RabbitMQ发送失败，第{}次重试: orderId={}, error={}", 
                        retryCount, message.getOrderId(), e.getMessage());

                if (retryCount < maxRetryCount) {
                    try {
                        long retryInterval = rabbitMQProperties.getErrorHandling().getRetryInterval();
                        Thread.sleep(retryInterval * retryCount); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("重试等待被中断: orderId={}", message.getOrderId());
                        break;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.error("订单消息RabbitMQ发送出现未预期异常: orderId={}, error={}", 
                        message.getOrderId(), e.getMessage(), e);
                break; // 非AMQP异常，不重试
            }
        }

        log.error("订单消息RabbitMQ发送最终失败: orderId={}, 重试次数={}, 最后异常={}", 
                message.getOrderId(), retryCount, lastException != null ? lastException.getMessage() : "未知");
        return false;
    }

    /**
     * 发送支付消息到RabbitMQ
     * 
     * @param message 支付消息
     * @return 发送是否成功
     */
    private boolean sendPaymentMessageToRabbitMQ(PaymentMessage message) {
        int retryCount = 0;
        Exception lastException = null;
        int maxRetryCount = rabbitMQProperties.getErrorHandling().getMaxRetryAttempts();

        while (retryCount < maxRetryCount) {
            try {
                String routingKey = generatePaymentRoutingKey(message);
                
                log.debug("发送支付消息到RabbitMQ: exchange={}, routingKey={}, paymentId={}", 
                        RabbitMQConstants.PAYMENT_EXCHANGE, routingKey, message.getPaymentId());

                rabbitTemplate.convertAndSend(
                        RabbitMQConstants.PAYMENT_EXCHANGE, 
                        routingKey, 
                        message
                );



                log.debug("支付消息RabbitMQ发送成功: paymentId={}, routingKey={}", 
                        message.getPaymentId(), routingKey);
                return true;

            } catch (AmqpException e) {
                lastException = e;
                retryCount++;
                log.warn("支付消息RabbitMQ发送失败，第{}次重试: paymentId={}, error={}", 
                        retryCount, message.getPaymentId(), e.getMessage());

                if (retryCount < maxRetryCount) {
                    try {
                        long retryInterval = rabbitMQProperties.getErrorHandling().getRetryInterval();
                        Thread.sleep(retryInterval * retryCount); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("重试等待被中断: paymentId={}", message.getPaymentId());
                        break;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.error("支付消息RabbitMQ发送出现未预期异常: paymentId={}, error={}", 
                        message.getPaymentId(), e.getMessage(), e);
                break; // 非AMQP异常，不重试
            }
        }

        log.error("支付消息RabbitMQ发送最终失败: paymentId={}, 重试次数={}, 最后异常={}", 
                message.getPaymentId(), retryCount, lastException != null ? lastException.getMessage() : "未知");
        return false;
    }

    /**
     * 发送订单通知到Redis
     * 
     * @param message 订单消息
     */
    private void sendOrderNotificationToRedis(OrderMessage message) {
        try {
            String reason = getOrderChangeReason(message.getEventType());
            
            orderNotificationService.notifyOrderStatusChange(
                    message.getOrderId(),
                    message.getUserId(),
                    message.getOldStatus(),
                    message.getNewStatus(),
                    reason
            );

            log.debug("订单Redis通知发送成功: orderId={}", message.getOrderId());

        } catch (Exception e) {
            log.error("订单Redis通知发送失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 发送支付通知到Redis
     * 
     * @param message 支付消息
     */
    private void sendPaymentNotificationToRedis(PaymentMessage message) {
        try {
            // 根据事件类型发送不同的通知
            if ("SUCCESS".equals(message.getEventType())) {
                orderNotificationService.notifyPaymentSuccess(
                        message.getOrderId(),
                        message.getUserId(),
                        message.getPaymentMethod(),
                        message.getAmount().doubleValue()
                );
            } else {
                // 对于失败和退款事件，发送订单状态变更通知
                String newStatus = getPaymentEventStatus(message.getEventType());
                orderNotificationService.notifyOrderStatusChange(
                        message.getOrderId(),
                        message.getUserId(),
                        null, // 旧状态可能不明确
                        newStatus,
                        getPaymentChangeReason(message.getEventType())
                );
            }

            log.debug("支付Redis通知发送成功: paymentId={}", message.getPaymentId());

        } catch (Exception e) {
            log.error("支付Redis通知发送失败: paymentId={}, error={}", 
                    message.getPaymentId(), e.getMessage(), e);
        }
    }

    /**
     * 生成订单路由键
     * 
     * @param message 订单消息
     * @return 路由键
     */
    private String generateOrderRoutingKey(OrderMessage message) {
        String eventType = message.getEventType();
        
        switch (eventType) {
            case "CREATE":
                return RabbitMQConstants.ORDER_CREATE_KEY;
            case "CANCEL":
                return RabbitMQConstants.ORDER_CANCEL_KEY;
            case "COMPLETE":
                return RabbitMQConstants.ORDER_COMPLETE_KEY;
            case "STATUS_CHANGE":
                // 使用状态变更的具体路由键
                return RabbitMQConstants.getOrderStatusRoutingKey(
                        message.getOldStatus(), 
                        message.getNewStatus()
                );
            default:
                log.warn("未知的订单事件类型: {}, 使用默认路由键", eventType);
                return RabbitMQConstants.ORDER_STATUS_PREFIX + ".unknown";
        }
    }

    /**
     * 生成支付路由键
     * 
     * @param message 支付消息
     * @return 路由键
     */
    private String generatePaymentRoutingKey(PaymentMessage message) {
        String eventType = message.getEventType();
        
        switch (eventType) {
            case "SUCCESS":
                return RabbitMQConstants.PAYMENT_SUCCESS_KEY;
            case "FAILED":
                return RabbitMQConstants.PAYMENT_FAILED_KEY;
            case "REFUND":
                return RabbitMQConstants.PAYMENT_REFUND_KEY;
            default:
                log.warn("未知的支付事件类型: {}, 使用默认路由键", eventType);
                return "payment.unknown";
        }
    }

    /**
     * 获取订单变更原因
     * 
     * @param eventType 事件类型
     * @return 变更原因
     */
    private String getOrderChangeReason(String eventType) {
        switch (eventType) {
            case "CREATE":
                return "订单创建";
            case "STATUS_CHANGE":
                return "订单状态变更";
            case "CANCEL":
                return "订单取消";
            case "COMPLETE":
                return "订单完成";
            default:
                return "系统处理";
        }
    }

    /**
     * 获取支付事件对应的订单状态
     * 
     * @param eventType 支付事件类型
     * @return 订单状态
     */
    private String getPaymentEventStatus(String eventType) {
        switch (eventType) {
            case "SUCCESS":
                return "paid";
            case "FAILED":
                return "payment_failed";
            case "REFUND":
                return "refunded";
            default:
                return "unknown";
        }
    }

    /**
     * 获取支付变更原因
     * 
     * @param eventType 支付事件类型
     * @return 变更原因
     */
    private String getPaymentChangeReason(String eventType) {
        switch (eventType) {
            case "SUCCESS":
                return "支付成功";
            case "FAILED":
                return "支付失败";
            case "REFUND":
                return "退款处理";
            default:
                return "支付处理";
        }
    }



    /**
     * 根据路由键获取队列名称
     * 
     * @param routingKey 路由键
     * @param type 消息类型（order/payment）
     * @return 队列名称
     */
    private String getQueueNameByRoutingKey(String routingKey, String type) {
        if ("order".equals(type)) {
            if (RabbitMQConstants.ORDER_CREATE_KEY.equals(routingKey)) {
                return RabbitMQConstants.ORDER_CREATE_QUEUE;
            } else if (RabbitMQConstants.ORDER_CANCEL_KEY.equals(routingKey)) {
                return RabbitMQConstants.ORDER_CANCEL_QUEUE;
            } else if (RabbitMQConstants.ORDER_COMPLETE_KEY.equals(routingKey)) {
                return RabbitMQConstants.ORDER_COMPLETE_QUEUE;
            } else if (routingKey.startsWith(RabbitMQConstants.ORDER_STATUS_PREFIX)) {
                return RabbitMQConstants.ORDER_STATUS_QUEUE;
            }
        } else if ("payment".equals(type)) {
            if (RabbitMQConstants.PAYMENT_SUCCESS_KEY.equals(routingKey)) {
                return RabbitMQConstants.PAYMENT_SUCCESS_QUEUE;
            } else if (RabbitMQConstants.PAYMENT_FAILED_KEY.equals(routingKey)) {
                return RabbitMQConstants.PAYMENT_FAILED_QUEUE;
            } else if (RabbitMQConstants.PAYMENT_REFUND_KEY.equals(routingKey)) {
                return RabbitMQConstants.PAYMENT_REFUND_QUEUE;
            }
        }
        return "unknown";
    }

    /**
     * 获取服务状态信息
     * 
     * @return 服务状态
     */
    public java.util.Map<String, Object> getServiceStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("rabbitmqEnabled", rabbitMQProperties.isEnabled());
        status.put("fallbackToSync", rabbitMQProperties.isFallbackToSync());
        status.put("rabbitmqAvailable", isRabbitMQAvailable());
        status.put("maxRetryCount", rabbitMQProperties.getErrorHandling().getMaxRetryAttempts());
        status.put("retryInterval", rabbitMQProperties.getErrorHandling().getRetryInterval());
        
        return status;
    }
}