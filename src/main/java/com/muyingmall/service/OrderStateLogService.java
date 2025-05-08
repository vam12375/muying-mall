package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.OrderStateLog;
import com.muyingmall.statemachine.OrderStateContext;

import java.util.List;

/**
 * 订单状态变更日志服务接口
 */
public interface OrderStateLogService extends IService<OrderStateLog> {

    /**
     * 记录订单状态变更
     *
     * @param context 订单状态上下文
     * @return 订单状态变更日志
     */
    OrderStateLog recordStateChange(OrderStateContext context);

    /**
     * 查询订单状态变更历史
     *
     * @param orderId 订单ID
     * @return 订单状态变更日志列表
     */
    List<OrderStateLog> getOrderStateHistory(Integer orderId);
}