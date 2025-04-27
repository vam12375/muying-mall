package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Address;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface AddressService extends IService<Address> {

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getUserAddresses(Integer userId);

    /**
     * 添加地址
     *
     * @param address 地址对象
     * @return 是否成功
     */
    boolean addAddress(Address address);

    /**
     * 更新地址
     *
     * @param address 地址对象
     * @return 是否成功
     */
    boolean updateAddress(Address address);

    /**
     * 设置默认地址
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Integer userId, Integer addressId);
}