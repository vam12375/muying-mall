package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Address;
import com.muyingmall.mapper.AddressMapper;
import com.muyingmall.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户地址服务实现类
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> getUserAddresses(Integer userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime);

        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(Address address) {
        // 检查是否是第一个地址，如果是则设为默认
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, address.getUserId());
        long count = count(queryWrapper);

        if (count == 0) {
            address.setIsDefault(1);
        } else if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }

        // 如果设置了默认地址，则将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            resetDefaultAddress(address.getUserId());
        }

        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        return save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Address address) {
        Address existAddress = getById(address.getAddressId());
        if (existAddress == null) {
            return false;
        }

        // 如果修改为默认地址，则重置其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1 &&
                (existAddress.getIsDefault() == null || existAddress.getIsDefault() != 1)) {
            resetDefaultAddress(address.getUserId());
        }

        address.setUpdateTime(LocalDateTime.now());

        return updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Integer userId, Integer addressId) {
        // 先将所有地址设为非默认
        resetDefaultAddress(userId);

        // 再将指定地址设为默认
        Address address = new Address();
        address.setAddressId(addressId);
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());

        return updateById(address);
    }

    /**
     * 重置用户所有地址为非默认
     *
     * @param userId 用户ID
     */
    private void resetDefaultAddress(Integer userId) {
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0);

        update(updateWrapper);
    }
}