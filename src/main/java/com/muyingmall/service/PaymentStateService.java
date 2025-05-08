package com.muyingmall.service;

import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.statemachine.PaymentEvent;
import com.muyingmall.statemachine.PaymentStateContext;

/**
 * 支付状态服务接口
 */
public interface PaymentStateService {

    /**
     * 支付状态转换
     *
     * @param paymentId 支付ID
     * @param event     事件
     * @param operator  操作者
     * @param reason    原因
     * @return 支付
     */
    Payment sendEvent(Long paymentId, PaymentEvent event, String operator, String reason);

    /**
     * 支付状态转换
     *
     * @param payment  支付
     * @param event    事件
     * @param operator 操作者
     * @param reason   原因
     * @return 支付
     */
    Payment sendEvent(Payment payment, PaymentEvent event, String operator, String reason);

    /**
     * 支付状态转换
     *
     * @param context 状态上下文
     * @return 支付
     */
    Payment sendEvent(PaymentStateContext context);

    /**
     * 判断支付状态是否可以转换
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否可以转换
     */
    boolean canTransit(PaymentStatus currentStatus, PaymentStatus targetStatus);

    /**
     * 获取可能的下一个状态
     *
     * @param currentStatus 当前状态
     * @return 可能的下一个状态数组
     */
    PaymentStatus[] getPossibleNextStates(PaymentStatus currentStatus);
}