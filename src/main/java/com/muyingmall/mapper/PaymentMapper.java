package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}