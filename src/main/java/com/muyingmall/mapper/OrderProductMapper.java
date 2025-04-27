package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.OrderProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品 Mapper 接口
 */
@Mapper
public interface OrderProductMapper extends BaseMapper<OrderProduct> {
}