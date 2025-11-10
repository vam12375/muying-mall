package com.muyingmall.controller;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.SystemMetricsDTO;
import com.muyingmall.dto.SystemMetricsDTO.*;
import com.muyingmall.monitor.*;
import com.muyingmall.service.SystemMonitorService;
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
    public CommonResult<SystemMetricsDTO> getSystemMetrics() {
        try {
            SystemMetricsDTO metrics = systemMonitorService.getSystemMetrics();
            return CommonResult.success(metrics);
        } catch (Exception e) {
            log.error("获取系统监控指标失败", e);
            return CommonResult.failed("获取系统监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看系统健康状态", module = "系统监控", operationType = "READ")
    public CommonResult<SystemHealthDTO> getSystemHealth() {
        try {
            SystemHealthDTO health = systemMonitorService.getSystemHealth();
            return CommonResult.success(health);
        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            return CommonResult.failed("获取系统健康状态失败: " + e.getMessage());
        }
    }


    /**
     * 获取服务器性能指标
     */
    @GetMapping("/server")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看服务器性能", module = "系统监控", operationType = "READ")
    public CommonResult<ServerPerformanceDTO> getServerPerformance() {
        try {
            ServerPerformanceDTO performance = serverMonitor.getServerPerformance();
            return CommonResult.success(performance);
        } catch (Exception e) {
            log.error("获取服务器性能指标失败", e);
            return CommonResult.failed("获取服务器性能指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库监控指标
     */
    @GetMapping("/database")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看数据库监控", module = "系统监控", operationType = "READ")
    public CommonResult<DatabaseMetricsDTO> getDatabaseMetrics() {
        try {
            DatabaseMetricsDTO metrics = databaseMonitor.getDatabaseMetrics();
            return CommonResult.success(metrics);
        } catch (Exception e) {
            log.error("获取数据库监控指标失败", e);
            return CommonResult.failed("获取数据库监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取Redis监控指标
     */
    @GetMapping("/redis")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看Redis监控", module = "系统监控", operationType = "READ")
    public CommonResult<RedisMetricsDTO> getRedisMetrics() {
        try {
            RedisMetricsDTO metrics = redisMonitor.getRedisMetrics();
            return CommonResult.success(metrics);
        } catch (Exception e) {
            log.error("获取Redis监控指标失败", e);
            return CommonResult.failed("获取Redis监控指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取API调用统计
     */
    @GetMapping("/api-statistics")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "查看API统计", module = "系统监控", operationType = "READ")
    public CommonResult<ApiStatisticsDTO> getApiStatistics() {
        try {
            ApiStatisticsDTO statistics = apiStatisticsMonitor.getApiStatistics();
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取API统计失败", e);
            return CommonResult.failed("获取API统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置API统计数据
     */
    @PostMapping("/api-statistics/reset")
    @PreAuthorize("hasAuthority('admin')")
    @AdminOperationLog(operation = "重置API统计", module = "系统监控", operationType = "UPDATE")
    public CommonResult<String> resetApiStatistics() {
        try {
            apiStatisticsMonitor.resetStatistics();
            return CommonResult.success("API统计数据已重置");
        } catch (Exception e) {
            log.error("重置API统计失败", e);
            return CommonResult.failed("重置API统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库版本信息
     */
    @GetMapping("/database/version")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult<String> getDatabaseVersion() {
        try {
            String version = databaseMonitor.getDatabaseVersion();
            return CommonResult.success(version);
        } catch (Exception e) {
            log.error("获取数据库版本失败", e);
            return CommonResult.failed("获取数据库版本失败: " + e.getMessage());
        }
    }

    /**
     * 检查Redis连接状态
     */
    @GetMapping("/redis/ping")
    @PreAuthorize("hasAuthority('admin')")
    public CommonResult<Boolean> pingRedis() {
        try {
            boolean available = redisMonitor.isRedisAvailable();
            return CommonResult.success(available);
        } catch (Exception e) {
            log.error("检查Redis连接失败", e);
            return CommonResult.failed("检查Redis连接失败: " + e.getMessage());
        }
    }
}
