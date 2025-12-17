package com.muyingmall.consumer;

import com.muyingmall.common.constants.RabbitMQConstants;
import com.muyingmall.dto.OrderMessage;

import com.muyingmall.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单消息消费者
 * 处理订单相关的消息队列消息
 */
@Component
@Slf4j
public class OrderMessageConsumer {

    @Autowired
    private OrderService orderService;



    /**
     * 处理订单创建消息
     * 处理订单创建后的业务逻辑，如库存扣减、积分计算等
     * 
     * @param orderMessage 订单消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.ORDER_CREATE_QUEUE)
    public void handleOrderCreate(@Payload OrderMessage orderMessage, 
                                 Channel channel, 
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long startTime = System.currentTimeMillis();
        
        log.debug("接收到订单创建消息: orderId={}, orderNo={}, userId={}", 
                orderMessage.getOrderId(), orderMessage.getOrderNo(), orderMessage.getUserId());
        

        
        try {
            // 验证消息内容
            if (orderMessage.getOrderId() == null || orderMessage.getUserId() == null) {
                log.error("订单创建消息内容不完整: {}", orderMessage);

                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 处理订单创建后的业务逻辑
            processOrderCreation(orderMessage);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.debug("订单创建消息处理完成: orderId={}, 处理时间: {}ms", orderMessage.getOrderId(), processingTime);
            
        } catch (Exception e) {
            log.error("处理订单创建消息失败: orderId={}, error={}", 
                     orderMessage.getOrderId(), e.getMessage(), e);
            

            
            try {
                // 拒绝消息并重新入队，让其他消费者重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }

    /**
     * 处理订单状态变更消息
     * 处理订单状态变更的业务逻辑，如物流信息更新、用户通知等
     * 
     * @param orderMessage 订单消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConstants.ORDER_STATUS_QUEUE)
    public void handleOrderStatusChange(@Payload OrderMessage orderMessage, 
                                       Channel channel, 
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long startTime = System.currentTimeMillis();
        
        log.debug("接收到订单状态变更消息: orderId={}, oldStatus={}, newStatus={}", 
                orderMessage.getOrderId(), orderMessage.getOldStatus(), orderMessage.getNewStatus());
        

        
        try {
            // 验证消息内容
            if (orderMessage.getOrderId() == null || orderMessage.getNewStatus() == null) {
                log.error("订单状态变更消息内容不完整: {}", orderMessage);

                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 处理订单状态变更的业务逻辑
            processOrderStatusChange(orderMessage);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.debug("订单状态变更消息处理完成: orderId={}, newStatus={}, 处理时间: {}ms", 
                    orderMessage.getOrderId(), orderMessage.getNewStatus(), processingTime);
            
        } catch (Exception e) {
            log.error("处理订单状态变更消息失败: orderId={}, error={}", 
                     orderMessage.getOrderId(), e.getMessage(), e);
            

            
            try {
                // 拒绝消息并重新入队，让其他消费者重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }

    /**
     * 处理订单创建的业务逻辑
     * 
     * @param orderMessage 订单消息
     */
    private void processOrderCreation(OrderMessage orderMessage) {
        try {
            log.debug("开始处理订单创建业务逻辑: orderId={}", orderMessage.getOrderId());
            
            // 1. 库存扣减确认（如果需要）
            // 注意：库存扣减通常在订单创建时已经完成，这里可能是二次确认或者处理异步库存操作
            
            // 2. 积分计算和预分配
            if (orderMessage.getTotalAmount() != null) {
                // 根据订单金额计算积分（这里是示例逻辑）
                // 实际业务中可能需要调用积分服务
                log.debug("为订单计算积分: orderId={}, amount={}", 
                        orderMessage.getOrderId(), orderMessage.getTotalAmount());
            }
            
            // 3. 优惠券使用确认
            // 如果订单使用了优惠券，确认优惠券状态
            
            // 4. 发送订单创建通知
            // 可以发送邮件、短信或推送通知给用户
            log.debug("发送订单创建通知: userId={}, orderId={}", 
                    orderMessage.getUserId(), orderMessage.getOrderId());
            
            // 5. 更新相关统计数据
            // 更新用户订单统计、商品销量统计等
            
            log.debug("订单创建业务逻辑处理完成: orderId={}", orderMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理订单创建业务逻辑失败: orderId={}", orderMessage.getOrderId(), e);
            throw e; // 重新抛出异常，让上层处理消息确认
        }
    }

    /**
     * 处理订单状态变更的业务逻辑
     * 
     * @param orderMessage 订单消息
     */
    private void processOrderStatusChange(OrderMessage orderMessage) {
        try {
            log.debug("开始处理订单状态变更业务逻辑: orderId={}, oldStatus={}, newStatus={}", 
                    orderMessage.getOrderId(), orderMessage.getOldStatus(), orderMessage.getNewStatus());
            
            String newStatus = orderMessage.getNewStatus();
            
            // 根据不同的状态变更执行不同的业务逻辑
            switch (newStatus) {
                case "PAID":
                    // 订单已支付
                    handleOrderPaid(orderMessage);
                    break;
                case "SHIPPED":
                    // 订单已发货
                    handleOrderShipped(orderMessage);
                    break;
                case "DELIVERED":
                    // 订单已送达
                    handleOrderDelivered(orderMessage);
                    break;
                case "CANCELLED":
                    // 订单已取消
                    handleOrderCancelled(orderMessage);
                    break;
                case "COMPLETED":
                    // 订单已完成
                    handleOrderCompleted(orderMessage);
                    break;
                default:
                    log.debug("订单状态变更，无需特殊处理: orderId={}, status={}", 
                            orderMessage.getOrderId(), newStatus);
            }
            
            log.debug("订单状态变更业务逻辑处理完成: orderId={}, newStatus={}", 
                    orderMessage.getOrderId(), newStatus);
            
        } catch (Exception e) {
            log.error("处理订单状态变更业务逻辑失败: orderId={}, newStatus={}", 
                     orderMessage.getOrderId(), orderMessage.getNewStatus(), e);
            throw e; // 重新抛出异常，让上层处理消息确认
        }
    }

    /**
     * 处理订单支付完成
     */
    private void handleOrderPaid(OrderMessage orderMessage) {
        log.debug("处理订单支付完成: orderId={}", orderMessage.getOrderId());
        // 1. 通知仓库准备发货
        // 2. 发送支付成功通知给用户
        // 3. 更新库存状态（从预扣减转为实际扣减）
    }

    /**
     * 处理订单发货
     */
    private void handleOrderShipped(OrderMessage orderMessage) {
        log.debug("处理订单发货: orderId={}", orderMessage.getOrderId());
        // 1. 发送发货通知给用户
        // 2. 开始物流跟踪
        // 3. 更新预计送达时间
    }

    /**
     * 处理订单送达
     */
    private void handleOrderDelivered(OrderMessage orderMessage) {
        log.debug("处理订单送达: orderId={}", orderMessage.getOrderId());
        // 1. 发送送达确认通知
        // 2. 开始售后服务期计时
        // 3. 邀请用户评价
    }

    /**
     * 处理订单取消
     */
    private void handleOrderCancelled(OrderMessage orderMessage) {
        log.debug("处理订单取消: orderId={}", orderMessage.getOrderId());
        // 1. 恢复库存
        // 2. 处理退款（如果已支付）
        // 3. 恢复优惠券（如果使用了）
        // 4. 发送取消通知
    }

    /**
     * 处理订单完成
     */
    private void handleOrderCompleted(OrderMessage orderMessage) {
        log.debug("处理订单完成: orderId={}", orderMessage.getOrderId());
        // 1. 发放积分奖励
        // 2. 更新用户等级
        // 3. 生成推荐商品
        // 4. 发送完成通知和满意度调查
    }
}