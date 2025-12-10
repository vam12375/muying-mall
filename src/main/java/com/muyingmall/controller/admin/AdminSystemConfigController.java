package com.muyingmall.controller.admin;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.SystemConfig;
import com.muyingmall.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供系统配置的增删改查功能
 */
@RestController
@RequestMapping("/admin/system-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "后台-系统配置", description = "系统配置管理接口")
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping
    @Operation(summary = "获取所有配置", description = "获取所有启用的系统配置项")
    public Result<List<SystemConfig>> getAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据分组获取配置
     */
    @GetMapping("/group/{group}")
    @Operation(summary = "按分组获取配置", description = "根据配置分组获取配置列表")
    public Result<List<SystemConfig>> getConfigsByGroup(@PathVariable String group) {
        try {
            List<SystemConfig> configs = systemConfigService.getConfigsByGroup(group);
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取分组配置失败: {}", group, e);
            return Result.error("获取分组配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个配置项
     */
    @GetMapping("/key/{key}")
    @Operation(summary = "获取单个配置", description = "根据配置键获取配置值")
    public Result<String> getConfigByKey(@PathVariable String key) {
        try {
            String value = systemConfigService.getConfigValue(key);
            return Result.success(value);
        } catch (Exception e) {
            log.error("获取配置项失败: {}", key, e);
            return Result.error("获取配置项失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping
    @Operation(summary = "批量更新配置", description = "批量更新系统配置项")
    public Result<Boolean> updateConfigs(@RequestBody Map<String, Object> configs) {
        try {
            boolean success = systemConfigService.updateConfigs(configs);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("更新配置失败");
            }
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return Result.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新单个配置项
     */
    @PutMapping("/key/{key}")
    @Operation(summary = "更新单个配置", description = "更新指定的配置项")
    public Result<Boolean> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        try {
            String value = body.get("value");
            boolean success = systemConfigService.updateConfig(key, value);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("配置项不存在");
            }
        } catch (Exception e) {
            log.error("更新配置项失败: {}", key, e);
            return Result.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 重置为默认配置
     */
    @PostMapping("/reset")
    @Operation(summary = "重置默认配置", description = "将所有配置重置为系统默认值")
    public Result<Boolean> resetToDefault() {
        try {
            boolean success = systemConfigService.resetToDefault();
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("重置配置失败");
            }
        } catch (Exception e) {
            log.error("重置配置失败", e);
            return Result.error("重置配置失败: " + e.getMessage());
        }
    }
}
