package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.UserCoupon;

/**
 * 用户优惠券服务接口
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 获取用户优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券对象
     */
    UserCoupon getUserCoupon(Integer userId, Long couponId);

    /**
     * 标记优惠券为已使用
     *
     * @param id      用户优惠券ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean markAsUsed(Long id, Long orderId);
}