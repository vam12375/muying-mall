package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券Mapper接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}