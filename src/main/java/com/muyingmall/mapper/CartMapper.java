package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}