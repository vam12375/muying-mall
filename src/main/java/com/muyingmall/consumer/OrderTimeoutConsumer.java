package com.muyingmall.consumer;

import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.service.OrderStateService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.statemachine.OrderEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.entity.Product;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单超时消息消费者
 *
 * 监听order.timeout.queue队列，处理超时未支付的订单
 * 实现论文中描述的"TTL + DLX死信队列"订单超时取消机制
 *
 * @author MuyingMall
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderMapper orderMapper;
    private final OrderStateService orderStateService;
    private final OrderProductMapper orderProductMapper;
    private final ProductService productService;
    private final ProductSkuService productSkuService;

    /**
     * 处理订单超时消息
     *
     * 当消息在order.delay.queue中等待30分钟后过期，
     * 会被转发到order.timeout.queue队列，由此方法处理
     *
     * @param message 消息内容（订单ID）
     * @param channel RabbitMQ通道
     * @param tag     消息标签
     */
    @RabbitListener(queues = "order.timeout.queue")
    public void handleOrderTimeout(Map<String, Object> message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        Integer orderId = null;
        try {
            orderId = (Integer) message.get("orderId");
            String orderNo = (String) message.get("orderNo");

            log.info("收到订单超时消息: orderId={}, orderNo={}", orderId, orderNo);

            // 查询订单当前状态
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.warn("订单不存在，忽略超时消息: orderId={}", orderId);
                channel.basicAck(tag, false);
                return;
            }

            // 只有处于"待支付"状态才执行取消
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                // 使用状态机发送超时事件
                orderStateService.sendEvent(
                        order,
                        OrderEvent.TIMEOUT,
                        "system",
                        "订单超过30分钟未支付，系统自动取消（消息队列触发）"
                );

                // 恢复库存
                restoreStock(orderId);

                log.info("订单超时取消成功: orderId={}, orderNo={}", orderId, orderNo);
            } else {
                log.info("订单状态已变更，无需取消: orderId={}, status={}", orderId, order.getStatus());
            }

            // 消息确认
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("处理订单超时消息失败: orderId={}, error={}", orderId, e.getMessage(), e);
            try {
                // 消息重新入队
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }

    /**
     * 恢复订单商品库存（支持SKU）
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Integer orderId) {
        try {
            // 查询订单商品
            LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderProduct::getOrderId, orderId);
            List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

            if (orderProducts.isEmpty()) {
                log.warn("订单 {} 没有关联的商品，无需恢复库存", orderId);
                return;
            }

            // 分别处理SKU商品和普通商品
            List<SkuStockDTO> skuStockList = new ArrayList<>();

            for (OrderProduct orderProduct : orderProducts) {
                Long skuId = orderProduct.getSkuId();
                Integer quantity = orderProduct.getQuantity();

                if (skuId != null && skuId > 0) {
                    // 有SKU，恢复SKU库存
                    SkuStockDTO stockDTO = new SkuStockDTO();
                    stockDTO.setSkuId(skuId);
                    stockDTO.setQuantity(quantity);
                    stockDTO.setOrderId(orderId);
                    stockDTO.setOperator("system");
                    stockDTO.setRemark("订单超时自动取消恢复库存（MQ触发）");
                    skuStockList.add(stockDTO);

                    log.debug("准备恢复SKU库存: skuId={}, quantity={}, orderId={}", skuId, quantity, orderId);
                } else {
                    // 无SKU，恢复商品库存
                    productService.update(
                            new LambdaUpdateWrapper<Product>()
                                    .eq(Product::getProductId, orderProduct.getProductId())
                                    .setSql("stock = stock + " + quantity));

                    log.debug("已恢复商品库存: productId={}, quantity={}", orderProduct.getProductId(), quantity);
                }
            }

            // 批量恢复SKU库存
            if (!skuStockList.isEmpty()) {
                productSkuService.batchRestoreStock(skuStockList);
                log.debug("订单 {} SKU库存恢复完成，共 {} 个SKU", orderId, skuStockList.size());
            }

            log.info("订单 {} 的所有商品库存已恢复", orderId);
        } catch (Exception e) {
            log.error("恢复订单 {} 商品库存失败: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
}
