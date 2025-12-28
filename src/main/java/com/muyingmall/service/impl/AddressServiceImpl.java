package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.dto.amap.GeoCodeResponse;
import com.muyingmall.entity.Address;
import com.muyingmall.mapper.AddressMapper;
import com.muyingmall.service.AddressService;
import com.muyingmall.service.AMapService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户地址服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    private final RedisUtil redisUtil;
    private final AMapService amapService;

    @Override
    @SuppressWarnings("unchecked")
    public List<Address> getUserAddresses(Integer userId) {
        // 构建缓存键
        String cacheKey = CacheConstants.USER_ADDRESS_LIST_KEY + userId;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取用户地址列表: userId={}", userId);
            return (List<Address>) cached;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户地址列表: userId={}", userId);
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime);

        List<Address> addresses = list(queryWrapper);
        
        // 缓存结果
        if (addresses != null) {
            redisUtil.set(cacheKey, addresses, CacheConstants.ADDRESS_EXPIRE_TIME);
            log.debug("将用户地址列表缓存到Redis: userId={}, 过期时间={}秒", userId, CacheConstants.ADDRESS_EXPIRE_TIME);
        }
        
        return addresses;
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

        // 【场景1：地址智能输入】调用高德地图API进行地理编码
        enrichAddressWithGeoCode(address);

        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        boolean result = save(address);
        
        // 清除用户地址列表缓存
        if (result) {
            clearUserAddressCache(address.getUserId());
        }
        
        return result;
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

        // 【场景1：地址智能输入】如果地址信息有变更，重新进行地理编码
        if (isAddressChanged(address, existAddress)) {
            enrichAddressWithGeoCode(address);
        }

        address.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(address);
        
        // 清除用户地址列表缓存
        if (result) {
            clearUserAddressCache(address.getUserId() != null ? address.getUserId() : existAddress.getUserId());
        }
        
        return result;
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

        boolean result = updateById(address);
        
        // 清除用户地址列表缓存
        if (result) {
            clearUserAddressCache(userId);
        }
        
        return result;
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
    
    /**
     * 清除用户地址缓存
     *
     * @param userId 用户ID
     */
    private void clearUserAddressCache(Integer userId) {
        if (userId == null) {
            return;
        }
        String cacheKey = CacheConstants.USER_ADDRESS_LIST_KEY + userId;
        redisUtil.del(cacheKey);
        log.debug("清除用户地址列表缓存: userId={}", userId);
    }

    /**
     * 【场景1：地址智能输入】使用高德地图API丰富地址信息
     * 将地址转换为经纬度坐标，并获取标准化地址
     *
     * @param address 地址对象
     */
    private void enrichAddressWithGeoCode(Address address) {
        try {
            // 拼接完整地址
            String fullAddress = buildFullAddress(address);
            if (fullAddress == null || fullAddress.trim().isEmpty()) {
                log.warn("地址信息不完整，跳过地理编码");
                return;
            }

            // 调用高德地图API进行地理编码
            GeoCodeResponse response = amapService.geoCode(fullAddress);
            if (response == null || response.getGeoCodes() == null || response.getGeoCodes().isEmpty()) {
                log.warn("地理编码失败或无结果: address={}", fullAddress);
                return;
            }

            // 获取第一个结果
            GeoCodeResponse.GeoCodes geoCode = response.getGeoCodes().get(0);
            
            // 解析经纬度
            String location = geoCode.getLocation();
            if (location != null && location.contains(",")) {
                String[] coords = location.split(",");
                address.setLongitude(Double.parseDouble(coords[0]));
                address.setLatitude(Double.parseDouble(coords[1]));
            }

            // 设置标准化地址和区域编码
            address.setFormattedAddress(geoCode.getFormattedAddress());
            address.setAdcode(geoCode.getAdCode());

            log.info("地址地理编码成功: address={}, location={}", fullAddress, location);
        } catch (Exception e) {
            log.error("地址地理编码异常", e);
            // 不影响主流程，继续保存地址
        }
    }

    /**
     * 构建完整地址字符串
     *
     * @param address 地址对象
     * @return 完整地址
     */
    private String buildFullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) {
            sb.append(address.getProvince());
        }
        if (address.getCity() != null) {
            sb.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            sb.append(address.getDistrict());
        }
        if (address.getDetail() != null) {
            sb.append(address.getDetail());
        }
        return sb.toString();
    }

    /**
     * 判断地址信息是否发生变更
     *
     * @param newAddress 新地址
     * @param oldAddress 旧地址
     * @return 是否变更
     */
    private boolean isAddressChanged(Address newAddress, Address oldAddress) {
        return !equals(newAddress.getProvince(), oldAddress.getProvince()) ||
               !equals(newAddress.getCity(), oldAddress.getCity()) ||
               !equals(newAddress.getDistrict(), oldAddress.getDistrict()) ||
               !equals(newAddress.getDetail(), oldAddress.getDetail());
    }

    /**
     * 安全比较两个对象是否相等
     */
    private boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    /**
     * 批量补充历史地址的坐标（管理员接口）
     * 用于修复历史数据中坐标为空的地址
     *
     * @return 补充成功的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchEnrichCoordinates() {
        log.info("【管理员操作】开始批量补充地址坐标");
        
        // 查询所有坐标为空的地址
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(Address::getLongitude)
                .or()
                .isNull(Address::getLatitude));
        
        List<Address> emptyCoordAddresses = list(queryWrapper);
        
        if (emptyCoordAddresses == null || emptyCoordAddresses.isEmpty()) {
            log.info("没有需要补充坐标的地址");
            return 0;
        }
        
        log.info("找到{}个坐标为空的地址，开始补充", emptyCoordAddresses.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Address address : emptyCoordAddresses) {
            try {
                // 调用地理编码
                enrichAddressWithGeoCode(address);
                
                // 如果成功获取到坐标，更新数据库
                if (address.getLongitude() != null && address.getLatitude() != null) {
                    address.setUpdateTime(LocalDateTime.now());
                    boolean updated = updateById(address);
                    
                    if (updated) {
                        successCount++;
                        log.info("地址坐标补充成功: addressId={}, location={},{}", 
                                address.getAddressId(), address.getLongitude(), address.getLatitude());
                        
                        // 清除缓存
                        clearUserAddressCache(address.getUserId());
                    } else {
                        failCount++;
                        log.warn("地址坐标更新失败: addressId={}", address.getAddressId());
                    }
                } else {
                    failCount++;
                    log.warn("地址地理编码失败: addressId={}, address={}", 
                            address.getAddressId(), buildFullAddress(address));
                }
                
                // 避免API调用过快，休眠100ms
                Thread.sleep(100);
                
            } catch (Exception e) {
                failCount++;
                log.error("补充地址坐标异常: addressId={}", address.getAddressId(), e);
            }
        }
        
        log.info("【管理员操作】批量补充地址坐标完成: 成功={}, 失败={}, 总数={}", 
                successCount, failCount, emptyCoordAddresses.size());
        
        return successCount;
    }
}