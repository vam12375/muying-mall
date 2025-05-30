package com.muyingmall.statemachine;

import com.muyingmall.enums.OrderStatus;
import com.muyingmall.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态机
 */
@Component
public class OrderStateMachine implements StateMachine<OrderStatus, OrderEvent, OrderStateContext> {

    /**
     * 订单状态转换规则
     */
    private static final Map<OrderStatus, Map<OrderEvent, OrderStatus>> STATE_MACHINE_MAP = new HashMap<>();

    static {
        // 初始化状态机规则

        // 待支付状态下的转换规则
        Map<OrderEvent, OrderStatus> pendingPaymentMap = new HashMap<>();
        pendingPaymentMap.put(OrderEvent.PAID, OrderStatus.PENDING_SHIPMENT);
        pendingPaymentMap.put(OrderEvent.CANCEL, OrderStatus.CANCELLED);
        pendingPaymentMap.put(OrderEvent.TIMEOUT, OrderStatus.CANCELLED);
        STATE_MACHINE_MAP.put(OrderStatus.PENDING_PAYMENT, pendingPaymentMap);

        // 待发货状态下的转换规则
        Map<OrderEvent, OrderStatus> pendingShipmentMap = new HashMap<>();
        pendingShipmentMap.put(OrderEvent.SHIP, OrderStatus.SHIPPED);
        pendingShipmentMap.put(OrderEvent.CANCEL, OrderStatus.CANCELLED);
        pendingShipmentMap.put(OrderEvent.REFUND_APPLY, OrderStatus.REFUNDING);
        STATE_MACHINE_MAP.put(OrderStatus.PENDING_SHIPMENT, pendingShipmentMap);

        // 已发货状态下的转换规则
        Map<OrderEvent, OrderStatus> shippedMap = new HashMap<>();
        shippedMap.put(OrderEvent.RECEIVE, OrderStatus.COMPLETED);
        shippedMap.put(OrderEvent.REFUND_APPLY, OrderStatus.REFUNDING);
        STATE_MACHINE_MAP.put(OrderStatus.SHIPPED, shippedMap);

        // 已完成状态下的转换规则
        Map<OrderEvent, OrderStatus> completedMap = new HashMap<>();
        completedMap.put(OrderEvent.REFUND_APPLY, OrderStatus.REFUNDING);
        STATE_MACHINE_MAP.put(OrderStatus.COMPLETED, completedMap);

        // 退款中状态下的转换规则
        Map<OrderEvent, OrderStatus> refundingMap = new HashMap<>();
        refundingMap.put(OrderEvent.REFUND_COMPLETE, OrderStatus.REFUNDED);
        refundingMap.put(OrderEvent.REFUND_FAIL, OrderStatus.COMPLETED);
        STATE_MACHINE_MAP.put(OrderStatus.REFUNDING, refundingMap);

        // 已退款状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(OrderStatus.REFUNDED, new HashMap<>());

        // 已取消状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(OrderStatus.CANCELLED, new HashMap<>());
    }

    @Override
    public OrderStatus sendEvent(OrderStatus currentState, OrderEvent event, OrderStateContext context) {
        Map<OrderEvent, OrderStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null || !eventMap.containsKey(event)) {
            throw new BusinessException("不支持的状态转换：从[" + currentState.getDesc() + "]状态触发[" + event + "]事件");
        }

        OrderStatus nextState = eventMap.get(event);

        // 记录状态变更
        context.setOldStatus(currentState);
        context.setNewStatus(nextState);
        context.setEvent(event);

        return nextState;
    }

    @Override
    public boolean canTransit(OrderStatus currentState, OrderStatus targetState) {
        if (currentState == targetState) {
            return true;
        }

        Map<OrderEvent, OrderStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null) {
            return false;
        }

        return eventMap.values().contains(targetState);
    }

    @Override
    public OrderStatus[] getPossibleNextStates(OrderStatus currentState) {
        Map<OrderEvent, OrderStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null) {
            return new OrderStatus[0];
        }

        return eventMap.values().toArray(new OrderStatus[0]);
    }
}