package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.OrderStateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单状态变更日志Mapper接口
 */
@Mapper
public interface OrderStateLogMapper extends BaseMapper<OrderStateLog> {
}