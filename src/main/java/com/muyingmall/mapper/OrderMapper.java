package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}