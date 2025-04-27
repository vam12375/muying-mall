package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址 Mapper 接口
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}