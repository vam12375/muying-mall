package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.service.UserCouponService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    private final RedisUtil redisUtil;

    @Override
    public UserCoupon getUserCoupon(Integer userId, Long couponId) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);

        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean markAsUsed(Long id, Long orderId) {
        UserCoupon userCoupon = getById(id);
        if (userCoupon == null) {
            return false;
        }

        userCoupon.setStatus("USED");
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        userCoupon.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(userCoupon);
        
        // 清除用户优惠券缓存
        if (result) {
            clearUserCouponCache(userCoupon.getUserId());
        }

        return result;
    }
    
    /**
     * 清除用户优惠券缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCouponCache(Integer userId) {
        if (userId == null) {
            return;
        }
        // 清除用户所有状态的优惠券缓存
        String pattern = CacheConstants.USER_COUPON_LIST_KEY + userId + ":*";
        Set<String> keys = redisUtil.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys);
        }
        log.debug("清除用户优惠券缓存: userId={}", userId);
    }
}