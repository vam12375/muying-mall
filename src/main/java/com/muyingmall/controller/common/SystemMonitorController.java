package com.muyingmall.controller.common;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.Result;
import com.muyingmall.dto.SystemMetricsDTO;
import com.muyingmall.dto.SystemMetricsDTO.*;
import com.muyingmall.monitor.*;
import com.muyingmall.service.SystemMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/system/monitor")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "后台-系统设置", description = "系统监控、性能指标、健康检查等功能")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;
    private final ServerMonitor serverMonitor;
    private final DatabaseMonitor databaseMonitor;
    private final RedisMonitor redisMonitor;
    private final ApiStatisticsMonitor apiStatisticsMonitor;

    /**
     * 获取完整的系统监控指标
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看系统监控指标", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取系统监控指标")
    public Result<SystemMetricsDTO> getSystemMetrics() {
        try {
            SystemMetricsDTO metrics = systemMonitorService.getSystemMetrics();
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取系统监控指标失败", e);
            return Result.error("获取系统监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看系统健康状态", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取系统健康状态")
    public Result<SystemHealthDTO> getSystemHealth() {
        try {
            SystemHealthDTO health = systemMonitorService.getSystemHealth();
            return Result.success(health);
        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            return Result.error("获取系统健康状态失败: " + e.getMessage());
        }
    }


    /**
     * 获取服务器性能指标
     */
    @GetMapping("/server")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看服务器性能", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取服务器性能指标")
    public Result<ServerPerformanceDTO> getServerPerformance() {
        try {
            ServerPerformanceDTO performance = serverMonitor.getServerPerformance();
            return Result.success(performance);
        } catch (Exception e) {
            log.error("获取服务器性能指标失败", e);
            return Result.error("获取服务器性能指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库监控指标
     */
    @GetMapping("/database")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看数据库监控", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取数据库监控指标")
    public Result<DatabaseMetricsDTO> getDatabaseMetrics() {
        try {
            DatabaseMetricsDTO metrics = databaseMonitor.getDatabaseMetrics();
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取数据库监控指标失败", e);
            return Result.error("获取数据库监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取Redis监控指标
     */
    @GetMapping("/redis")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看Redis监控", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取Redis监控指标")
    public Result<RedisMetricsDTO> getRedisMetrics() {
        try {
            RedisMetricsDTO metrics = redisMonitor.getRedisMetrics();
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取Redis监控指标失败", e);
            return Result.error("获取Redis监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取API调用统计
     */
    @GetMapping("/api-statistics")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看API统计", module = "系统监控", operationType = "READ")
    @Operation(summary = "获取API调用统计")
    public Result<ApiStatisticsDTO> getApiStatistics() {
        try {
            ApiStatisticsDTO statistics = apiStatisticsMonitor.getApiStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取API统计失败", e);
            return Result.error("获取API统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置API统计数据
     */
    @PostMapping("/api-statistics/reset")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "重置API统计", module = "系统监控", operationType = "UPDATE")
    @Operation(summary = "重置API统计数据")
    public Result<String> resetApiStatistics() {
        try {
            apiStatisticsMonitor.resetStatistics();
            return Result.success("API统计数据已重置");
        } catch (Exception e) {
            log.error("重置API统计失败", e);
            return Result.error("重置API统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库版本信息
     */
    @GetMapping("/database/version")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "获取数据库版本")
    public Result<String> getDatabaseVersion() {
        try {
            String version = databaseMonitor.getDatabaseVersion();
            return Result.success(version);
        } catch (Exception e) {
            log.error("获取数据库版本失败", e);
            return Result.error("获取数据库版本失败: " + e.getMessage());
        }
    }

    /**
     * 检查Redis连接状态
     */
    @GetMapping("/redis/ping")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "检查Redis连接状态")
    public Result<Boolean> pingRedis() {
        try {
            boolean available = redisMonitor.isRedisAvailable();
            return Result.success(available);
        } catch (Exception e) {
            log.error("检查Redis连接失败", e);
            return Result.error("检查Redis连接失败: " + e.getMessage());
        }
    }
}
