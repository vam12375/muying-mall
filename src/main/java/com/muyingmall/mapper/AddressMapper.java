package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址Mapper接口
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}