package com.muyingmall.service;

import com.muyingmall.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 获取所有配置
     */
    List<SystemConfig> getAllConfigs();

    /**
     * 根据分组获取配置
     */
    List<SystemConfig> getConfigsByGroup(String group);

    /**
     * 根据键获取配置值
     */
    String getConfigValue(String key);

    /**
     * 根据键获取配置值（带默认值）
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * 获取数字类型配置值
     */
    Integer getIntValue(String key, Integer defaultValue);

    /**
     * 获取布尔类型配置值
     */
    Boolean getBooleanValue(String key, Boolean defaultValue);

    /**
     * 批量更新配置
     */
    boolean updateConfigs(Map<String, Object> configs);

    /**
     * 更新单个配置
     */
    boolean updateConfig(String key, String value);

    /**
     * 重置为默认配置
     */
    boolean resetToDefault();

    /**
     * 初始化默认配置（如果不存在）
     */
    void initDefaultConfigs();
}
