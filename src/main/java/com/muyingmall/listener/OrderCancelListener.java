package com.muyingmall.listener;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.impl.OrderStateServiceImpl.OrderStateChangedEvent;
import com.muyingmall.statemachine.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单取消事件监听器
 * 处理订单取消或超时时的积分退还
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelListener {

    private final PointsService pointsService;

    /**
     * 监听OrderStateChangedEvent事件，处理订单取消和超时
     * 当订单取消且使用了积分时，退还积分
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCancelled(OrderStateChangedEvent event) {
        Order order = event.getOrder();
        OrderEvent orderEvent = event.getEvent();
        OrderStatus newStatus = event.getNewStatus();

        // 只处理订单取消或超时事件，且订单状态为已取消
        if ((OrderEvent.CANCEL.equals(orderEvent) || OrderEvent.TIMEOUT.equals(orderEvent))
                && OrderStatus.CANCELLED.equals(newStatus)) {

            log.info("接收到订单取消事件: orderId={}, event={}, operator={}",
                    order.getOrderId(), orderEvent, event.getOperator());

            // 检查订单是否使用了积分
            Integer pointsUsed = order.getPointsUsed();
            if (pointsUsed != null && pointsUsed > 0) {
                Integer userId = order.getUserId();
                String orderNo = order.getOrderNo();

                log.info("订单 {} 取消，开始退还积分 {}", orderNo, pointsUsed);

                try {
                    // 调用积分服务，退还积分
                    boolean success = pointsService.addPoints(
                            userId,
                            pointsUsed,
                            "order_cancel",
                            orderNo,
                            "订单取消返还积分");

                    if (success) {
                        log.info("订单 {} 取消，成功退还积分 {} 给用户 {}", orderNo, pointsUsed, userId);
                    } else {
                        log.error("订单 {} 取消，退还积分 {} 失败", orderNo, pointsUsed);
                    }
                } catch (Exception e) {
                    log.error("订单 {} 取消退还积分异常: {}", orderNo, e.getMessage(), e);
                }
            } else {
                log.info("订单 {} 取消，未使用积分，无需退还", order.getOrderNo());
            }
        }
    }
}