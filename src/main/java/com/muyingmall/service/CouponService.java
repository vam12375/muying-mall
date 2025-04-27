package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.UserCoupon;

import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取可用优惠券列表
     *
     * @param userId 用户ID，可为空
     * @return 优惠券列表
     */
    List<Coupon> getAvailableCoupons(Integer userId);

    /**
     * 获取用户优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态：all-全部, UNUSED-未使用, USED-已使用, EXPIRED-已过期
     * @return 用户优惠券列表
     */
    List<UserCoupon> getUserCoupons(Integer userId, String status);

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean receiveCoupon(Integer userId, Long couponId);

    /**
     * 获取订单可用优惠券
     *
     * @param userId     用户ID
     * @param amount     订单金额
     * @param productIds 商品ID列表
     * @return 可用优惠券列表
     */
    List<UserCoupon> getOrderCoupons(Integer userId, Double amount, List<Integer> productIds);
}