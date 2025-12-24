package com.muyingmall.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消费者（异步处理秒杀订单）
 * 
 * 注意：当前实现采用同步处理方式，此消费者作为扩展预留
 * 如需启用异步秒杀，需要：
 * 1. 在RabbitMQConfig中配置秒杀队列
 * 2. 修改SeckillController使用消息队列
 * 3. 启用此消费者的@RabbitListener注解
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {
    
    private final SeckillOrderService seckillOrderService;
    private final ObjectMapper objectMapper;
    
    /**
     * 消费秒杀订单消息
     * 
     * 消息格式：
     * {
     *   "userId": 1,
     *   "seckillProductId": 1,
     *   "quantity": 1,
     *   "addressId": 1
     * }
     */
    // @RabbitListener(queues = "seckill.order.queue")
    public void handleSeckillOrder(String message) {
        try {
            log.info("收到秒杀订单消息: {}", message);
            
            // 解析消息
            SeckillOrderMessage orderMessage = objectMapper.readValue(message, SeckillOrderMessage.class);
            
            // 构建秒杀请求
            SeckillRequestDTO request = new SeckillRequestDTO();
            request.setSeckillProductId(orderMessage.getSeckillProductId());
            request.setQuantity(orderMessage.getQuantity());
            request.setAddressId(orderMessage.getAddressId());
            
            // 执行秒杀
            Long orderId = seckillOrderService.executeSeckill(orderMessage.getUserId(), request);
            
            log.info("秒杀订单处理成功: userId={}, orderId={}", orderMessage.getUserId(), orderId);
            
        } catch (Exception e) {
            log.error("秒杀订单处理失败: message={}", message, e);
            // 这里可以实现重试逻辑或将失败消息发送到死信队列
        }
    }
    
    /**
     * 秒杀订单消息实体
     */
    @lombok.Data
    public static class SeckillOrderMessage {
        private Integer userId;
        private Long seckillProductId;
        private Integer quantity;
        private Long addressId;
    }
}
