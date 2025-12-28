package com.muyingmall.service;

import java.math.BigDecimal;

/**
 * 运费计算服务接口
 */
public interface ShippingService {

    /**
     * 【场景2：智能运费计算】根据地址和订单金额计算运费
     *
     * @param addressId   收货地址ID
     * @param orderAmount 订单金额
     * @return 运费（元）
     */
    BigDecimal calculateShippingFee(Integer addressId, BigDecimal orderAmount);

    /**
     * 【场景2：智能运费计算】根据距离和订单金额计算运费
     *
     * @param distance    配送距离（公里）
     * @param orderAmount 订单金额
     * @return 运费（元）
     */
    BigDecimal calculateShippingFeeByDistance(Double distance, BigDecimal orderAmount);

    /**
     * 判断地址是否在配送范围内
     *
     * @param addressId 收货地址ID
     * @return 是否可配送
     */
    boolean isDeliverable(Integer addressId);
}
