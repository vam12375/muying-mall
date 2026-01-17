package com.muyingmall.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 系统日志统计数据DTO
 * 来源：自定义实现
 */
@Data
public class SystemLogStatisticsDTO {
    
    /**
     * 总日志数
     */
    private Long totalLogs;
    
    /**
     * 成功数
     */
    private Long successCount;
    
    /**
     * 失败数
     */
    private Long failedCount;
    
    /**
     * 成功率（百分比）
     */
    private Double successRate;
    
    /**
     * 平均响应时间（毫秒）
     */
    private Double avgExecutionTime;
    
    /**
     * 最大响应时间（毫秒）
     */
    private Long maxExecutionTime;
    
    /**
     * 最小响应时间（毫秒）
     */
    private Long minExecutionTime;
    
    /**
     * 今日日志数
     */
    private Long todayLogs;
    
    /**
     * 操作类型分布
     */
    private Map<String, Long> operationTypeDistribution;
    
    /**
     * 模块操作统计
     */
    private List<Map<String, Object>> moduleStats;
    
    /**
     * 每日日志趋势（最近7天）
     */
    private List<Map<String, Object>> dailyTrend;
}
