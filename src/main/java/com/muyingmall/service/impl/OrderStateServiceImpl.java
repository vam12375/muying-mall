package com.muyingmall.service.impl;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.OrderStateLogService;
import com.muyingmall.service.OrderStateService;
import com.muyingmall.statemachine.OrderEvent;
import com.muyingmall.statemachine.OrderStateContext;
import com.muyingmall.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单状态服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStateServiceImpl implements OrderStateService {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderStateMachine orderStateMachine;
    private final OrderStateLogService orderStateLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order sendEvent(Integer orderId, OrderEvent event, String operator, String reason) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在：" + orderId);
        }
        return sendEvent(order, event, operator, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order sendEvent(Order order, OrderEvent event, String operator, String reason) {
        OrderStateContext context = OrderStateContext.of(order);
        context.setEvent(event);
        context.setOperator(operator);
        context.setReason(reason);
        return sendEvent(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order sendEvent(OrderStateContext context) {
        Order order = context.getOrder();
        OrderStatus currentStatus = order.getStatus();
        OrderEvent event = context.getEvent();

        try {
            // 执行状态转换
            OrderStatus nextStatus = orderStateMachine.sendEvent(currentStatus, event, context);

            // 更新订单状态
            updateOrderStatus(order, nextStatus, context);

            // 记录状态变更日志
            recordStateLog(context);

            // 发布状态变更事件
            publishStateChangedEvent(context);

            return order;
        } catch (Exception e) {
            log.error("订单状态转换失败：orderId={}, event={}, error={}",
                    order.getOrderId(), event, e.getMessage(), e);
            throw new BusinessException("订单状态转换失败：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean canTransit(OrderStatus currentStatus, OrderStatus targetStatus) {
        return orderStateMachine.canTransit(currentStatus, targetStatus);
    }

    @Override
    public OrderStatus[] getPossibleNextStates(OrderStatus currentStatus) {
        return orderStateMachine.getPossibleNextStates(currentStatus);
    }

    /**
     * 更新订单状态
     *
     * @param order      订单
     * @param nextStatus 下一个状态
     * @param context    状态上下文
     */
    private void updateOrderStatus(Order order, OrderStatus nextStatus, OrderStateContext context) {
        // 设置新状态
        order.setStatus(nextStatus);
        order.setUpdateTime(LocalDateTime.now());

        // 根据事件类型设置特定的字段
        switch (context.getEvent()) {
            case PAID:
                // 支付成功，设置支付时间
                if (order.getPayTime() == null) {
                    order.setPayTime(LocalDateTime.now());
                }
                break;
            case SHIP:
                // 发货，设置发货时间和物流信息
                order.setShippingTime(LocalDateTime.now());
                break;
            case RECEIVE:
                // 收货，设置完成时间
                order.setCompletionTime(LocalDateTime.now());
                break;
            case CANCEL:
                // 取消，设置取消时间和原因
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(context.getReason());
                break;
            case TIMEOUT:
                // 超时取消，设置取消时间和原因
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时未支付，系统自动取消");
                order.setRemark("未支付已自动取消");
                break;
            default:
                break;
        }

        // 使用乐观锁更新订单，可防止并发更新问题
        boolean updateResult = orderService.updateById(order);
        if (!updateResult) {
            throw new BusinessException("订单状态更新失败，可能已被其他操作修改");
        }
    }

    /**
     * 记录订单状态变更日志
     *
     * @param context 状态上下文
     */
    private void recordStateLog(OrderStateContext context) {
        // 记录日志到控制台
        log.debug("订单状态变更：orderId={}, oldStatus={}, newStatus={}, event={}, operator={}, reason={}",
                context.getOrder().getOrderId(),
                context.getOldStatus(),
                context.getNewStatus(),
                context.getEvent(),
                context.getOperator(),
                context.getReason());

        // 记录日志到数据库
        orderStateLogService.recordStateChange(context);
    }

    /**
     * 发布订单状态变更事件
     *
     * @param context 状态上下文
     */
    private void publishStateChangedEvent(OrderStateContext context) {
        // 发布订单状态变更事件，供其他模块监听处理
        if (eventPublisher != null) {
            OrderStateChangedEvent event = new OrderStateChangedEvent(
                    context.getOrder(),
                    context.getOldStatus(),
                    context.getNewStatus(),
                    context.getEvent(),
                    context.getOperator(),
                    context.getReason());
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * 订单状态变更事件
     */
    public static class OrderStateChangedEvent {
        private final Order order;
        private final OrderStatus oldStatus;
        private final OrderStatus newStatus;
        private final OrderEvent event;
        private final String operator;
        private final String reason;

        public OrderStateChangedEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus,
                OrderEvent event, String operator, String reason) {
            this.order = order;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.event = event;
            this.operator = operator;
            this.reason = reason;
        }

        public Order getOrder() {
            return order;
        }

        public OrderStatus getOldStatus() {
            return oldStatus;
        }

        public OrderStatus getNewStatus() {
            return newStatus;
        }

        public OrderEvent getEvent() {
            return event;
        }

        public String getOperator() {
            return operator;
        }

        public String getReason() {
            return reason;
        }
    }
}