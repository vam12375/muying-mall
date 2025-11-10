package com.muyingmall.service;

import com.muyingmall.dto.SystemMetricsDTO;
import com.muyingmall.dto.SystemMetricsDTO.*;
import com.muyingmall.monitor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 系统监控服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemMonitorService {

    private final ServerMonitor serverMonitor;
    private final DatabaseMonitor databaseMonitor;
    private final RedisMonitor redisMonitor;
    private final ApiStatisticsMonitor apiStatisticsMonitor;

    /**
     * 获取完整的系统监控指标
     */
    public SystemMetricsDTO getSystemMetrics() {
        return SystemMetricsDTO.builder()
                .health(getSystemHealth())
                .serverPerformance(serverMonitor.getServerPerformance())
                .databaseMetrics(databaseMonitor.getDatabaseMetrics())
                .redisMetrics(redisMonitor.getRedisMetrics())
                .apiStatistics(apiStatisticsMonitor.getApiStatistics())
                .errorLogStatistics(getErrorLogStatistics())
                .build();
    }

    /**
     * 获取系统健康状态
     */
    public SystemHealthDTO getSystemHealth() {
        Map<String, ComponentHealthDTO> components = new HashMap<>();
        
        // 检查数据库
        components.put("database", checkDatabaseHealth());
        
        // 检查Redis
        components.put("redis", checkRedisHealth());
        
        // 检查磁盘空间
        components.put("diskSpace", checkDiskSpaceHealth());
        
        // 检查内存
        components.put("memory", checkMemoryHealth());

        // 确定整体状态
        String overallStatus = determineOverallStatus(components);

        return SystemHealthDTO.builder()
                .status(overallStatus)
                .timestamp(System.currentTimeMillis())
                .components(components)
                .build();
    }


    /**
     * 检查数据库健康状态
     */
    private ComponentHealthDTO checkDatabaseHealth() {
        try {
            DatabaseMetricsDTO metrics = databaseMonitor.getDatabaseMetrics();
            String status = "UP".equals(metrics.getStatus()) ? "UP" : "DOWN";
            
            Map<String, Object> details = new HashMap<>();
            details.put("activeConnections", metrics.getActiveConnections());
            details.put("connectionUsage", metrics.getConnectionUsage() + "%");
            
            return ComponentHealthDTO.builder()
                    .status(status)
                    .message(status.equals("UP") ? "数据库运行正常" : "数据库连接失败")
                    .details(details)
                    .build();
        } catch (Exception e) {
            return ComponentHealthDTO.builder()
                    .status("DOWN")
                    .message("数据库检查失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 检查Redis健康状态
     */
    private ComponentHealthDTO checkRedisHealth() {
        try {
            boolean available = redisMonitor.isRedisAvailable();
            String status = available ? "UP" : "DOWN";
            
            Map<String, Object> details = new HashMap<>();
            if (available) {
                RedisMetricsDTO metrics = redisMonitor.getRedisMetrics();
                details.put("version", metrics.getVersion());
                details.put("connectedClients", metrics.getConnectedClients());
                details.put("usedMemory", metrics.getUsedMemoryHuman());
            }
            
            return ComponentHealthDTO.builder()
                    .status(status)
                    .message(status.equals("UP") ? "Redis运行正常" : "Redis连接失败")
                    .details(details)
                    .build();
        } catch (Exception e) {
            return ComponentHealthDTO.builder()
                    .status("DOWN")
                    .message("Redis检查失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 检查磁盘空间健康状态
     */
    private ComponentHealthDTO checkDiskSpaceHealth() {
        try {
            ServerPerformanceDTO performance = serverMonitor.getServerPerformance();
            Double diskUsage = performance.getDiskUsage();
            
            String status;
            String message;
            if (diskUsage < 80) {
                status = "UP";
                message = "磁盘空间充足";
            } else if (diskUsage < 90) {
                status = "WARNING";
                message = "磁盘空间不足，建议清理";
            } else {
                status = "DOWN";
                message = "磁盘空间严重不足";
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("usage", diskUsage + "%");
            details.put("free", formatBytes(performance.getFreeDiskSpace()));
            details.put("total", formatBytes(performance.getTotalDiskSpace()));
            
            return ComponentHealthDTO.builder()
                    .status(status)
                    .message(message)
                    .details(details)
                    .build();
        } catch (Exception e) {
            return ComponentHealthDTO.builder()
                    .status("UNKNOWN")
                    .message("磁盘检查失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 检查内存健康状态
     */
    private ComponentHealthDTO checkMemoryHealth() {
        try {
            ServerPerformanceDTO performance = serverMonitor.getServerPerformance();
            Double memoryUsage = performance.getMemoryUsage();
            
            String status;
            String message;
            if (memoryUsage < 80) {
                status = "UP";
                message = "内存使用正常";
            } else if (memoryUsage < 90) {
                status = "WARNING";
                message = "内存使用率较高";
            } else {
                status = "DOWN";
                message = "内存使用率过高";
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("usage", memoryUsage + "%");
            details.put("used", formatBytes(performance.getUsedMemory()));
            details.put("total", formatBytes(performance.getTotalMemory()));
            
            return ComponentHealthDTO.builder()
                    .status(status)
                    .message(message)
                    .details(details)
                    .build();
        } catch (Exception e) {
            return ComponentHealthDTO.builder()
                    .status("UNKNOWN")
                    .message("内存检查失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 确定整体健康状态
     */
    private String determineOverallStatus(Map<String, ComponentHealthDTO> components) {
        boolean hasDown = components.values().stream()
                .anyMatch(c -> "DOWN".equals(c.getStatus()));
        boolean hasWarning = components.values().stream()
                .anyMatch(c -> "WARNING".equals(c.getStatus()));
        
        if (hasDown) {
            return "DOWN";
        } else if (hasWarning) {
            return "WARNING";
        } else {
            return "UP";
        }
    }

    /**
     * 获取错误日志统计（模拟数据）
     */
    private ErrorLogStatisticsDTO getErrorLogStatistics() {
        // 这里返回模拟数据，实际应该从日志系统获取
        Map<String, Long> errorsByType = new HashMap<>();
        errorsByType.put("NullPointerException", 5L);
        errorsByType.put("SQLException", 3L);
        errorsByType.put("TimeoutException", 2L);
        
        Map<String, Long> errorsByLevel = new HashMap<>();
        errorsByLevel.put("ERROR", 8L);
        errorsByLevel.put("WARN", 15L);
        
        return ErrorLogStatisticsDTO.builder()
                .totalErrors(23L)
                .last24hErrors(10L)
                .lastHourErrors(2L)
                .errorsByType(errorsByType)
                .errorsByLevel(errorsByLevel)
                .recentErrors(new ArrayList<>())
                .errorTrend(new ArrayList<>())
                .build();
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
