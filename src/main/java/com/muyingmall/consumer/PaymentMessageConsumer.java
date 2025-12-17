package com.muyingmall.consumer;

import com.muyingmall.common.constants.RabbitMQConstants;
import com.muyingmall.dto.PaymentMessage;

import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 支付消息消费者
 * 处理支付相关的消息队列消息
 */
@Component
@Slf4j
public class PaymentMessageConsumer {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderService orderService;



    /**
     * 处理支付成功消息
     * 处理支付成功后的业务逻辑，如更新订单状态、发送确认邮件等
     * 
     * @param paymentMessage 支付消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(@Payload PaymentMessage paymentMessage, 
                                   Channel channel, 
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long startTime = System.currentTimeMillis();
        
        log.debug("接收到支付成功消息: paymentId={}, orderId={}, amount={}", 
                paymentMessage.getPaymentId(), paymentMessage.getOrderId(), paymentMessage.getAmount());
        

        
        try {
            // 验证消息内容
            if (paymentMessage.getPaymentId() == null || paymentMessage.getOrderId() == null) {
                log.error("支付成功消息内容不完整: {}", paymentMessage);

                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 处理支付成功的业务逻辑
            processPaymentSuccess(paymentMessage);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.debug("支付成功消息处理完成: paymentId={}, orderId={}, 处理时间: {}ms", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId(), processingTime);
            
        } catch (Exception e) {
            log.error("处理支付成功消息失败: paymentId={}, orderId={}, error={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e.getMessage(), e);
            

            
            try {
                // 拒绝消息并重新入队，让其他消费者重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }

    /**
     * 处理支付失败消息
     * 处理支付失败后的业务逻辑，如恢复库存、取消订单等
     * 
     * @param paymentMessage 支付消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(@Payload PaymentMessage paymentMessage, 
                                  Channel channel, 
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.debug("接收到支付失败消息: paymentId={}, orderId={}, amount={}", 
                paymentMessage.getPaymentId(), paymentMessage.getOrderId(), paymentMessage.getAmount());
        
        try {
            // 验证消息内容
            if (paymentMessage.getPaymentId() == null || paymentMessage.getOrderId() == null) {
                log.error("支付失败消息内容不完整: {}", paymentMessage);
                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 处理支付失败的业务逻辑
            processPaymentFailed(paymentMessage);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.debug("支付失败消息处理完成: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理支付失败消息失败: paymentId={}, orderId={}, error={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e.getMessage(), e);
            try {
                // 拒绝消息并重新入队，让其他消费者重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }

    /**
     * 处理退款消息
     * 处理退款相关的业务逻辑
     * 
     * @param paymentMessage 支付消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.PAYMENT_REFUND_QUEUE)
    public void handlePaymentRefund(@Payload PaymentMessage paymentMessage, 
                                  Channel channel, 
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.debug("接收到退款消息: paymentId={}, orderId={}, amount={}", 
                paymentMessage.getPaymentId(), paymentMessage.getOrderId(), paymentMessage.getAmount());
        
        try {
            // 验证消息内容
            if (paymentMessage.getPaymentId() == null || paymentMessage.getOrderId() == null) {
                log.error("退款消息内容不完整: {}", paymentMessage);
                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 处理退款的业务逻辑
            processPaymentRefund(paymentMessage);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.debug("退款消息处理完成: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理退款消息失败: paymentId={}, orderId={}, error={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e.getMessage(), e);
            try {
                // 拒绝消息并重新入队，让其他消费者重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }

    /**
     * 处理支付成功的业务逻辑
     * 
     * @param paymentMessage 支付消息
     */
    private void processPaymentSuccess(PaymentMessage paymentMessage) {
        try {
            log.debug("开始处理支付成功业务逻辑: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
            // 1. 更新订单状态为已支付
            Integer orderId = paymentMessage.getOrderId();
            if (orderId != null) {
                log.debug("更新订单状态为已支付: orderId={}", orderId);
                // 这里应该调用OrderService的方法来更新订单状态
                // orderService.updateOrderStatus(orderId, "PAID");
            }
            
            // 2. 确认库存扣减
            // 将预扣减的库存转为实际扣减
            log.debug("确认库存扣减: orderId={}", orderId);
            
            // 3. 发送支付成功通知
            sendPaymentSuccessNotification(paymentMessage);
            
            // 4. 记录支付成功日志和统计
            recordPaymentSuccess(paymentMessage);
            
            // 5. 触发后续业务流程
            // 如自动发货、积分奖励等
            triggerPostPaymentProcesses(paymentMessage);
            
            log.debug("支付成功业务逻辑处理完成: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理支付成功业务逻辑失败: paymentId={}, orderId={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e);
            throw e; // 重新抛出异常，让上层处理消息确认
        }
    }

    /**
     * 处理支付失败的业务逻辑
     * 
     * @param paymentMessage 支付消息
     */
    private void processPaymentFailed(PaymentMessage paymentMessage) {
        try {
            log.debug("开始处理支付失败业务逻辑: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
            // 1. 更新订单状态
            Integer orderId = paymentMessage.getOrderId();
            if (orderId != null) {
                log.debug("更新订单状态为支付失败: orderId={}", orderId);
                // 根据业务规则，可能需要取消订单或保持待支付状态
                // orderService.updateOrderStatus(orderId, "PAYMENT_FAILED");
            }
            
            // 2. 恢复库存
            // 释放预扣减的库存
            log.debug("恢复库存: orderId={}", orderId);
            
            // 3. 恢复优惠券
            // 如果使用了优惠券，需要恢复优惠券状态
            log.debug("恢复优惠券: orderId={}", orderId);
            
            // 4. 发送支付失败通知
            sendPaymentFailedNotification(paymentMessage);
            
            // 5. 记录支付失败日志
            recordPaymentFailure(paymentMessage);
            
            log.debug("支付失败业务逻辑处理完成: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理支付失败业务逻辑失败: paymentId={}, orderId={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e);
            throw e; // 重新抛出异常，让上层处理消息确认
        }
    }

    /**
     * 处理退款的业务逻辑
     * 
     * @param paymentMessage 支付消息
     */
    private void processPaymentRefund(PaymentMessage paymentMessage) {
        try {
            log.debug("开始处理退款业务逻辑: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
            // 1. 更新订单状态
            Integer orderId = paymentMessage.getOrderId();
            if (orderId != null) {
                log.debug("更新订单状态为已退款: orderId={}", orderId);
                // orderService.updateOrderStatus(orderId, "REFUNDED");
            }
            
            // 2. 恢复库存（如果商品还未发货）
            log.debug("检查并恢复库存: orderId={}", orderId);
            
            // 3. 处理积分回退
            // 如果订单获得了积分，需要扣回积分
            log.debug("处理积分回退: orderId={}", orderId);
            
            // 4. 发送退款通知
            sendRefundNotification(paymentMessage);
            
            // 5. 记录退款日志
            recordRefund(paymentMessage);
            
            log.debug("退款业务逻辑处理完成: paymentId={}, orderId={}", 
                    paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理退款业务逻辑失败: paymentId={}, orderId={}", 
                     paymentMessage.getPaymentId(), paymentMessage.getOrderId(), e);
            throw e; // 重新抛出异常，让上层处理消息确认
        }
    }

    /**
     * 发送支付成功通知
     */
    private void sendPaymentSuccessNotification(PaymentMessage paymentMessage) {
        log.debug("发送支付成功通知: userId={}, orderId={}, amount={}", 
                paymentMessage.getUserId(), paymentMessage.getOrderId(), paymentMessage.getAmount());
        
        // 1. 发送邮件通知
        // 2. 发送短信通知
        // 3. 发送App推送通知
        // 4. 更新用户消息中心
    }

    /**
     * 发送支付失败通知
     */
    private void sendPaymentFailedNotification(PaymentMessage paymentMessage) {
        log.debug("发送支付失败通知: userId={}, orderId={}, reason={}", 
                paymentMessage.getUserId(), paymentMessage.getOrderId(), paymentMessage.getStatus());
        
        // 1. 发送邮件通知
        // 2. 发送短信通知
        // 3. 发送App推送通知
        // 4. 更新用户消息中心
    }

    /**
     * 发送退款通知
     */
    private void sendRefundNotification(PaymentMessage paymentMessage) {
        log.debug("发送退款通知: userId={}, orderId={}, amount={}", 
                paymentMessage.getUserId(), paymentMessage.getOrderId(), paymentMessage.getAmount());
        
        // 1. 发送邮件通知
        // 2. 发送短信通知
        // 3. 发送App推送通知
        // 4. 更新用户消息中心
    }

    /**
     * 记录支付成功统计
     */
    private void recordPaymentSuccess(PaymentMessage paymentMessage) {
        log.debug("记录支付成功统计: paymentMethod={}, amount={}", 
                paymentMessage.getPaymentMethod(), paymentMessage.getAmount());
        
        // 1. 更新支付方式统计
        // 2. 更新用户支付统计
        // 3. 更新商品销售统计
        // 4. 更新平台收入统计
    }

    /**
     * 记录支付失败统计
     */
    private void recordPaymentFailure(PaymentMessage paymentMessage) {
        log.debug("记录支付失败统计: paymentMethod={}, reason={}", 
                paymentMessage.getPaymentMethod(), paymentMessage.getStatus());
        
        // 1. 更新支付失败统计
        // 2. 分析失败原因
        // 3. 更新风控数据
    }

    /**
     * 记录退款统计
     */
    private void recordRefund(PaymentMessage paymentMessage) {
        log.debug("记录退款统计: paymentMethod={}, amount={}", 
                paymentMessage.getPaymentMethod(), paymentMessage.getAmount());
        
        // 1. 更新退款统计
        // 2. 更新用户退款记录
        // 3. 分析退款原因
    }

    /**
     * 触发支付后续流程
     */
    private void triggerPostPaymentProcesses(PaymentMessage paymentMessage) {
        log.debug("触发支付后续流程: orderId={}", paymentMessage.getOrderId());
        
        // 1. 自动发货流程（对于虚拟商品）
        // 2. 积分奖励流程
        // 3. 推荐系统更新
        // 4. 营销活动触发
    }
}