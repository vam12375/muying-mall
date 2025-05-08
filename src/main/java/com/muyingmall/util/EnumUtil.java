package com.muyingmall.util;

import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.PaymentStatus;

/**
 * 枚举工具类
 */
public class EnumUtil {

    /**
     * 根据状态编码获取订单状态枚举
     *
     * @param code 状态编码
     * @return 订单状态枚举
     */
    public static OrderStatus getOrderStatusByCode(String code) {
        if (code == null) {
            return OrderStatus.PENDING_PAYMENT; // 默认为待支付
        }
        return OrderStatus.getByCode(code);
    }

    /**
     * 获取订单状态编码
     *
     * @param status 订单状态枚举
     * @return 状态编码
     */
    public static String getOrderStatusCode(OrderStatus status) {
        return status != null ? status.getCode() : null;
    }

    /**
     * 根据状态字符串获取订单状态枚举
     *
     * @param statusStr 状态字符串
     * @return 订单状态枚举
     */
    public static OrderStatus getOrderStatusByString(String statusStr) {
        if (statusStr == null) {
            return null;
        }
        return OrderStatus.getByCode(statusStr);
    }

    /**
     * 获取订单状态字符串
     *
     * @param status 订单状态枚举
     * @return 状态字符串
     */
    public static String getOrderStatusString(OrderStatus status) {
        return status != null ? status.getCode() : null;
    }

    /**
     * 根据状态编码获取支付状态枚举
     *
     * @param code 状态编码
     * @return 支付状态枚举
     */
    public static PaymentStatus getPaymentStatusByCode(int code) {
        return PaymentStatus.getByCode(code);
    }

    /**
     * 获取支付状态编码
     *
     * @param status 支付状态枚举
     * @return 状态编码
     */
    public static int getPaymentStatusCode(PaymentStatus status) {
        return status != null ? status.getCode() : 0;
    }
}