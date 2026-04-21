package com.muyingmall.fixtures;

import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.UserCoupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券测试夹具。
 */
public final class CouponFixtures {

    private CouponFixtures() {
    }

    /**
     * 构造一个处于有效期内的固定金额券。
     */
    public static Coupon activeFixedCoupon(Long id, BigDecimal value) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setName("满减券-" + id);
        coupon.setCode("CPN" + id);
        coupon.setType("FIXED");
        coupon.setValue(value);
        coupon.setMinSpend(value.multiply(new BigDecimal("2")));
        coupon.setMaxDiscount(value);
        coupon.setStatus("ACTIVE");
        coupon.setIsStackable(0);
        coupon.setTotalQuantity(1000);
        coupon.setUsedQuantity(0);
        coupon.setReceivedQuantity(0);
        coupon.setUserLimit(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(7));
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        return coupon;
    }

    /**
     * 构造一张已过期的券。
     */
    public static Coupon expiredCoupon(Long id, BigDecimal value) {
        Coupon coupon = activeFixedCoupon(id, value);
        coupon.setStartTime(LocalDateTime.now().minusDays(30));
        coupon.setEndTime(LocalDateTime.now().minusDays(1));
        return coupon;
    }

    /**
     * 构造一张未使用状态的用户优惠券。
     */
    public static UserCoupon unusedUserCoupon(Long id, Integer userId, Long couponId) {
        UserCoupon uc = new UserCoupon();
        uc.setId(id);
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("UNUSED");
        uc.setReceiveTime(LocalDateTime.now().minusHours(1));
        uc.setExpireTime(LocalDateTime.now().plusDays(7));
        uc.setCreateTime(LocalDateTime.now());
        uc.setUpdateTime(LocalDateTime.now());
        return uc;
    }

    /**
     * 构造一张已使用状态的用户优惠券。
     */
    public static UserCoupon usedUserCoupon(Long id, Integer userId, Long couponId, Long orderId) {
        UserCoupon uc = unusedUserCoupon(id, userId, couponId);
        uc.setStatus("USED");
        uc.setUseTime(LocalDateTime.now());
        uc.setOrderId(orderId);
        return uc;
    }
}
