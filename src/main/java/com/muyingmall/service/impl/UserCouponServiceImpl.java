package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

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

        return updateById(userCoupon);
    }
}