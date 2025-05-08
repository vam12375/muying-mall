package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PaymentStateLog;
import com.muyingmall.statemachine.PaymentStateContext;

import java.util.List;

/**
 * 支付状态变更日志服务接口
 */
public interface PaymentStateLogService extends IService<PaymentStateLog> {
    
    /**
     * 记录支付状态变更
     *
     * @param context 支付状态上下文
     * @return 支付状态变更日志
     */
    PaymentStateLog recordStateChange(PaymentStateContext context);
    
    /**
     * 查询支付状态变更历史
     *
     * @param paymentId 支付ID
     * @return 支付状态变更日志列表
     */
    List<PaymentStateLog> getPaymentStateHistory(Long paymentId);
    
    /**
     * 查询订单关联的支付状态变更历史
     *
     * @param orderId 订单ID
     * @return 支付状态变更日志列表
     */
    List<PaymentStateLog> getPaymentStateHistoryByOrderId(Integer orderId);
} 