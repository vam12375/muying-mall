package com.muyingmall.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.config.RabbitMQSeckillConfig;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.service.SeckillOrderService;
import com.muyingmall.websocket.SeckillWebSocket;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消费者 - 异步处理秒杀订单，实现削峰填谷
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final SeckillOrderService seckillOrderService;
    private final ObjectMapper objectMapper;

    /**
     * 消费秒杀订单消息
     * 使用手动ACK模式，保证消息可靠性
     */
    @RabbitListener(queues = RabbitMQSeckillConfig.SECKILL_QUEUE, ackMode = "MANUAL")
    public void handleSeckillOrder(String messageBody, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到秒杀订单消息: {}", messageBody);

            // 解析消息
            SeckillOrderMessage orderMessage = objectMapper.readValue(messageBody, SeckillOrderMessage.class);

            // 构建秒杀请求
            SeckillRequestDTO request = new SeckillRequestDTO();
            request.setSeckillProductId(orderMessage.getSeckillProductId());
            request.setQuantity(orderMessage.getQuantity());
            request.setAddressId(orderMessage.getAddressId());

            // 执行秒杀
            Long orderId = seckillOrderService.executeSeckill(orderMessage.getUserId(), request);

            log.info("✅ 秒杀订单处理成功: userId={}, orderId={}", orderMessage.getUserId(), orderId);

            // 通过WebSocket通知用户秒杀成功
            SeckillWebSocket.sendSeckillSuccess(orderMessage.getUserId(), orderId);

            // 手动ACK
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("❌ 秒杀订单处理失败: message={}", messageBody, e);

            // 通过WebSocket通知用户秒杀失败
            try {
                SeckillOrderMessage orderMessage = objectMapper.readValue(messageBody, SeckillOrderMessage.class);
                SeckillWebSocket.sendSeckillFailure(orderMessage.getUserId(), e.getMessage());
            } catch (Exception parseEx) {
                log.error("解析消息失败，无法发送WebSocket通知", parseEx);
            }

            try {
                // 拒绝消息，不重新入队（避免死循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);
            }
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
