package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券Mapper接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * CAS方式安全递增已领取数量，防止并发超发
     * 仅当 received_quantity < total_quantity（或 total_quantity=0 表示不限量）时才更新
     *
     * @param couponId 优惠券ID
     * @return 影响行数，0 表示已被领完
     */
    @Update("UPDATE coupon SET received_quantity = received_quantity + 1, update_time = NOW() " +
            "WHERE id = #{couponId} AND (total_quantity = 0 OR received_quantity < total_quantity)")
    int incrementReceivedQuantity(@Param("couponId") Long couponId);
}