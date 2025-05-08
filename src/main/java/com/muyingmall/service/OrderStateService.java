package com.muyingmall.service;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.statemachine.OrderEvent;
import com.muyingmall.statemachine.OrderStateContext;

/**
 * 订单状态服务接口
 */
public interface OrderStateService {

    /**
     * 订单状态转换
     *
     * @param orderId  订单ID
     * @param event    事件
     * @param operator 操作者
     * @param reason   原因
     * @return 订单
     */
    Order sendEvent(Integer orderId, OrderEvent event, String operator, String reason);

    /**
     * 订单状态转换
     *
     * @param order    订单
     * @param event    事件
     * @param operator 操作者
     * @param reason   原因
     * @return 订单
     */
    Order sendEvent(Order order, OrderEvent event, String operator, String reason);

    /**
     * 订单状态转换
     *
     * @param context 状态上下文
     * @return 订单
     */
    Order sendEvent(OrderStateContext context);

    /**
     * 判断订单状态是否可以转换
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否可以转换
     */
    boolean canTransit(OrderStatus currentStatus, OrderStatus targetStatus);

    /**
     * 获取可能的下一个状态
     *
     * @param currentStatus 当前状态
     * @return 可能的下一个状态数组
     */
    OrderStatus[] getPossibleNextStates(OrderStatus currentStatus);
}