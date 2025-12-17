package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.entity.SystemConfig;
import com.muyingmall.mapper.SystemConfigMapper;
import com.muyingmall.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_KEY = "system:config";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @PostConstruct
    public void init() {
        // 应用启动时初始化默认配置
        initDefaultConfigs();
    }

    @Override
    public List<SystemConfig> getAllConfigs() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<SystemConfig> configs = (List<SystemConfig>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (configs != null) {
            return configs;
        }

        // 从数据库获取
        configs = systemConfigMapper.selectAllEnabled();
        
        // 存入缓存
        if (configs != null && !configs.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY, configs, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        
        return configs != null ? configs : new ArrayList<>();
    }

    @Override
    public List<SystemConfig> getConfigsByGroup(String group) {
        return systemConfigMapper.selectByGroup(group);
    }

    @Override
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        // 先从缓存的配置列表中查找
        List<SystemConfig> configs = getAllConfigs();
        for (SystemConfig config : configs) {
            if (config.getConfigKey().equals(key)) {
                return config.getConfigValue();
            }
        }
        return defaultValue;
    }

    @Override
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = getConfigValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 无法转换为整数", key, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigs(Map<String, Object> configs) {
        if (configs == null || configs.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String strValue = value != null ? String.valueOf(value) : "";
            
            // 更新数据库
            LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SystemConfig::getConfigKey, key)
                    .set(SystemConfig::getConfigValue, strValue)
                    .set(SystemConfig::getUpdateTime, LocalDateTime.now());
            
            int updated = systemConfigMapper.update(null, updateWrapper);
            if (updated == 0) {
                log.warn("配置项 {} 不存在，跳过更新", key);
            }
        }

        // 清除缓存
        clearCache();
        
        log.debug("批量更新系统配置成功，共 {} 项", configs.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(String key, String value) {
        LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemConfig::getConfigKey, key)
                .set(SystemConfig::getConfigValue, value)
                .set(SystemConfig::getUpdateTime, LocalDateTime.now());
        
        int updated = systemConfigMapper.update(null, updateWrapper);
        
        if (updated > 0) {
            clearCache();
            log.debug("更新系统配置成功: {} = {}", key, value);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetToDefault() {
        // 删除所有配置
        systemConfigMapper.delete(new LambdaQueryWrapper<>());
        
        // 重新初始化默认配置
        initDefaultConfigs();
        
        // 清除缓存
        clearCache();
        
        log.debug("系统配置已重置为默认值");
        return true;
    }

    /**
     * 清除配置缓存
     */
    private void clearCache() {
        redisTemplate.delete(CACHE_KEY);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultConfigs() {
        // 检查是否已有配置
        Long count = systemConfigMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            log.debug("系统配置已存在，跳过初始化");
            return;
        }

        log.debug("开始初始化系统默认配置...");
        
        List<SystemConfig> defaultConfigs = new ArrayList<>();
        int sortOrder = 0;

        // ========== 基础配置 ==========
        defaultConfigs.add(createConfig("siteName", "母婴商城", "网站名称", "basic", "string", "显示在浏览器标题和页面顶部", ++sortOrder));
        defaultConfigs.add(createConfig("siteLogo", "", "网站Logo", "basic", "image", "建议尺寸 200x60 像素", ++sortOrder));
        defaultConfigs.add(createConfig("copyright", "© 2024 母婴商城 版权所有", "版权信息", "basic", "string", "显示在页面底部", ++sortOrder));
        defaultConfigs.add(createConfig("contactPhone", "400-888-8888", "联系电话", "basic", "string", "客服热线电话", ++sortOrder));
        defaultConfigs.add(createConfig("contactEmail", "service@muying.com", "联系邮箱", "basic", "string", "官方联系邮箱", ++sortOrder));
        defaultConfigs.add(createConfig("contactAddress", "", "联系地址", "basic", "string", "公司详细地址", ++sortOrder));

        // ========== 业务配置 ==========
        sortOrder = 0;
        defaultConfigs.add(createConfig("orderAutoCancelMinutes", "30", "订单自动取消时间", "business", "number", "未支付订单超过此时间自动取消（分钟）", ++sortOrder));
        defaultConfigs.add(createConfig("orderAutoConfirmDays", "15", "自动确认收货时间", "business", "number", "发货后超过此时间自动确认收货（天）", ++sortOrder));
        defaultConfigs.add(createConfig("orderAutoCompleteDays", "7", "订单自动完成时间", "business", "number", "确认收货后超过此时间订单自动完成（天）", ++sortOrder));
        defaultConfigs.add(createConfig("refundApplyDays", "7", "退款申请时限", "business", "number", "确认收货后可申请退款的天数", ++sortOrder));
        defaultConfigs.add(createConfig("stockWarningThreshold", "10", "库存预警阈值", "business", "number", "库存低于此数量时发出预警", ++sortOrder));

        // ========== 积分配置 ==========
        sortOrder = 0;
        defaultConfigs.add(createConfig("pointsDeductionRate", "100", "积分抵扣比例", "points", "number", "多少积分可抵扣1元", ++sortOrder));
        defaultConfigs.add(createConfig("consumePointsRate", "1", "消费积分比例", "points", "number", "消费1元获得多少积分", ++sortOrder));
        defaultConfigs.add(createConfig("signInPoints", "10", "签到积分", "points", "number", "每日签到获得的积分", ++sortOrder));
        defaultConfigs.add(createConfig("registerPoints", "100", "注册赠送积分", "points", "number", "新用户注册赠送的积分", ++sortOrder));

        // ========== 支付配置 ==========
        sortOrder = 0;
        defaultConfigs.add(createConfig("alipayEnabled", "true", "支付宝支付", "payment", "boolean", "启用后用户可使用支付宝付款", ++sortOrder));
        defaultConfigs.add(createConfig("wechatPayEnabled", "true", "微信支付", "payment", "boolean", "启用后用户可使用微信付款", ++sortOrder));
        defaultConfigs.add(createConfig("balancePayEnabled", "true", "余额支付", "payment", "boolean", "启用后用户可使用账户余额付款", ++sortOrder));

        // ========== 物流配置 ==========
        sortOrder = 0;
        defaultConfigs.add(createConfig("defaultShippingFee", "10", "默认运费", "logistics", "number", "订单默认运费（元）", ++sortOrder));
        defaultConfigs.add(createConfig("freeShippingAmount", "99", "满额包邮", "logistics", "number", "订单金额满此数免运费，0表示不启用", ++sortOrder));

        // ========== 上传配置 ==========
        sortOrder = 0;
        defaultConfigs.add(createConfig("maxImageSize", "5", "图片大小限制", "upload", "number", "单张图片最大上传大小（MB）", ++sortOrder));
        defaultConfigs.add(createConfig("allowedImageTypes", "jpg,jpeg,png,gif,webp", "允许的图片类型", "upload", "string", "多个类型用逗号分隔", ++sortOrder));

        // 批量插入
        for (SystemConfig config : defaultConfigs) {
            systemConfigMapper.insert(config);
        }

        log.debug("系统默认配置初始化完成，共 {} 项", defaultConfigs.size());
    }

    /**
     * 创建配置项
     */
    private SystemConfig createConfig(String key, String value, String name, String group, 
                                       String valueType, String description, int sortOrder) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigName(name);
        config.setConfigGroup(group);
        config.setValueType(valueType);
        config.setDescription(description);
        config.setSortOrder(sortOrder);
        config.setStatus(1);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        return config;
    }
}
