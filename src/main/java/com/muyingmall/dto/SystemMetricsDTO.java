package com.muyingmall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 系统监控指标数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsDTO {

    /**
     * 系统健康状态
     */
    private SystemHealthDTO health;

    /**
     * 服务器性能指标
     */
    private ServerPerformanceDTO serverPerformance;

    /**
     * 数据库监控指标
     */
    private DatabaseMetricsDTO databaseMetrics;

    /**
     * Redis监控指标
     */
    private RedisMetricsDTO redisMetrics;

    /**
     * API调用统计
     */
    private ApiStatisticsDTO apiStatistics;

    /**
     * 错误日志统计
     */
    private ErrorLogStatisticsDTO errorLogStatistics;

    /**
     * 系统健康状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthDTO {
        private String status; // UP, DOWN, WARNING
        private Long timestamp;
        private Map<String, ComponentHealthDTO> components;
    }

    /**
     * 组件健康状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealthDTO {
        private String status;
        private String message;
        private Map<String, Object> details;
    }

    /**
     * 服务器性能指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerPerformanceDTO {
        // CPU相关
        private Double cpuUsage; // CPU使用率 (%)
        private Integer cpuCores; // CPU核心数
        private Double systemLoadAverage; // 系统负载

        // 内存相关
        private Long totalMemory; // 总内存 (bytes)
        private Long usedMemory; // 已用内存 (bytes)
        private Long freeMemory; // 空闲内存 (bytes)
        private Double memoryUsage; // 内存使用率 (%)

        // JVM相关
        private Long jvmTotalMemory; // JVM总内存 (bytes)
        private Long jvmUsedMemory; // JVM已用内存 (bytes)
        private Long jvmFreeMemory; // JVM空闲内存 (bytes)
        private Double jvmMemoryUsage; // JVM内存使用率 (%)
        private Integer threadCount; // 线程数
        private Integer peakThreadCount; // 峰值线程数

        // 磁盘相关
        private Long totalDiskSpace; // 总磁盘空间 (bytes)
        private Long usedDiskSpace; // 已用磁盘空间 (bytes)
        private Long freeDiskSpace; // 空闲磁盘空间 (bytes)
        private Double diskUsage; // 磁盘使用率 (%)

        // 运行时间
        private Long uptime; // 运行时间 (ms)
        private String uptimeFormatted; // 格式化的运行时间
    }

    /**
     * 数据库监控指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseMetricsDTO {
        private String status; // 数据库状态
        private Integer activeConnections; // 活跃连接数
        private Integer idleConnections; // 空闲连接数
        private Integer maxConnections; // 最大连接数
        private Double connectionUsage; // 连接使用率 (%)
        
        // 查询性能
        private Long totalQueries; // 总查询数
        private Long slowQueries; // 慢查询数
        private Double avgQueryTime; // 平均查询时间 (ms)
        
        // 数据库大小
        private Long databaseSize; // 数据库大小 (bytes)
        private Map<String, Long> tableSizes; // 各表大小
        
        // 最近慢查询
        private List<SlowQueryDTO> recentSlowQueries;
    }

    /**
     * 慢查询信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlowQueryDTO {
        private String sql;
        private Long executionTime; // 执行时间 (ms)
        private String timestamp;
    }

    /**
     * Redis监控指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisMetricsDTO {
        private String status;
        private String version;
        private Integer connectedClients; // 连接客户端数
        private Long usedMemory; // 已用内存 (bytes)
        private String usedMemoryHuman; // 人类可读的内存使用
        private Double memFragmentationRatio; // 内存碎片率
        
        // 性能指标
        private Long totalCommandsProcessed; // 总命令处理数
        private Long opsPerSec; // 每秒操作数
        private Long keyspaceHits; // 缓存命中数
        private Long keyspaceMisses; // 缓存未命中数
        private Double hitRate; // 命中率 (%)
        
        // 键空间信息
        private Integer totalKeys; // 总键数
        private Integer expiredKeys; // 过期键数
        private Integer evictedKeys; // 驱逐键数
        
        // 持久化
        private String rdbLastSaveTime;
        private String aofEnabled;
    }

    /**
     * API调用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiStatisticsDTO {
        private Long totalRequests; // 总请求数
        private Long successRequests; // 成功请求数
        private Long failedRequests; // 失败请求数
        private Double successRate; // 成功率 (%)
        private Double avgResponseTime; // 平均响应时间 (ms)
        
        // 请求分布
        private Map<String, Long> requestsByEndpoint; // 按端点统计
        private Map<String, Long> requestsByMethod; // 按方法统计
        private Map<Integer, Long> requestsByStatus; // 按状态码统计
        
        // 最慢的API
        private List<SlowApiDTO> slowestApis;
        
        // 最频繁的API
        private List<FrequentApiDTO> mostFrequentApis;
        
        // 时间序列数据（最近24小时）
        private List<TimeSeriesDataDTO> requestTimeSeries;
    }

    /**
     * 慢API信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlowApiDTO {
        private String endpoint;
        private String method;
        private Long avgResponseTime; // 平均响应时间 (ms)
        private Long maxResponseTime; // 最大响应时间 (ms)
        private Long requestCount; // 请求次数
    }

    /**
     * 频繁API信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrequentApiDTO {
        private String endpoint;
        private String method;
        private Long requestCount;
        private Double avgResponseTime;
    }

    /**
     * 时间序列数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesDataDTO {
        private String timestamp;
        private Long value;
    }

    /**
     * 错误日志统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorLogStatisticsDTO {
        private Long totalErrors; // 总错误数
        private Long last24hErrors; // 最近24小时错误数
        private Long lastHourErrors; // 最近1小时错误数
        
        // 错误类型分布
        private Map<String, Long> errorsByType;
        
        // 错误级别分布
        private Map<String, Long> errorsByLevel;
        
        // 最近错误
        private List<RecentErrorDTO> recentErrors;
        
        // 错误趋势（最近24小时）
        private List<TimeSeriesDataDTO> errorTrend;
    }

    /**
     * 最近错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentErrorDTO {
        private String timestamp;
        private String level;
        private String message;
        private String exception;
        private String endpoint;
    }
}
