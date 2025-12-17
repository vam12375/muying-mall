package com.muyingmall.monitor;

import com.muyingmall.dto.SystemMetricsDTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * API调用统计监控组件
 */
@Component
@Slf4j
public class ApiStatisticsMonitor {

    // API调用统计数据
    private final Map<String, ApiCallStats> apiStatsMap = new ConcurrentHashMap<>();
    
    // 时间序列数据（最近24小时，按小时统计）
    private final Map<String, Long> hourlyRequestCounts = new ConcurrentHashMap<>();
    
    // 全局统计
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    /**
     * API调用统计数据类
     */
    private static class ApiCallStats {
        String endpoint;
        String method;
        AtomicLong requestCount = new AtomicLong(0);
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failedCount = new AtomicLong(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        Map<Integer, AtomicLong> statusCodeCounts = new ConcurrentHashMap<>();

        ApiCallStats(String endpoint, String method) {
            this.endpoint = endpoint;
            this.method = method;
        }
    }

    /**
     * 记录API调用
     */
    public void recordApiCall(String endpoint, String method, int statusCode, long responseTime) {
        try {
            // 更新全局统计
            totalRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            
            if (statusCode >= 200 && statusCode < 300) {
                successRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            // 更新API统计
            String key = endpoint + ":" + method;
            ApiCallStats stats = apiStatsMap.computeIfAbsent(key, 
                k -> new ApiCallStats(endpoint, method));
            
            stats.requestCount.incrementAndGet();
            stats.totalResponseTime.addAndGet(responseTime);
            
            if (statusCode >= 200 && statusCode < 300) {
                stats.successCount.incrementAndGet();
            } else {
                stats.failedCount.incrementAndGet();
            }
            
            // 更新最大响应时间
            long currentMax = stats.maxResponseTime.get();
            while (responseTime > currentMax) {
                if (stats.maxResponseTime.compareAndSet(currentMax, responseTime)) {
                    break;
                }
                currentMax = stats.maxResponseTime.get();
            }
            
            // 更新状态码统计
            stats.statusCodeCounts.computeIfAbsent(statusCode, k -> new AtomicLong(0))
                    .incrementAndGet();

            // 更新时间序列数据
            String hourKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));
            hourlyRequestCounts.merge(hourKey, 1L, Long::sum);
            
            // 清理超过24小时的数据
            cleanOldTimeSeriesData();
            
        } catch (Exception e) {
            log.error("记录API调用统计失败", e);
        }
    }

    /**
     * 获取API统计信息
     */
    public ApiStatisticsDTO getApiStatistics() {
        try {
            long total = totalRequests.get();
            long success = successRequests.get();
            long failed = failedRequests.get();
            long totalTime = totalResponseTime.get();

            return ApiStatisticsDTO.builder()
                    .totalRequests(total)
                    .successRequests(success)
                    .failedRequests(failed)
                    .successRate(total > 0 ? (success * 100.0 / total) : 0.0)
                    .avgResponseTime(total > 0 ? (totalTime * 1.0 / total) : 0.0)
                    .requestsByEndpoint(getRequestsByEndpoint())
                    .requestsByMethod(getRequestsByMethod())
                    .requestsByStatus(getRequestsByStatus())
                    .slowestApis(getSlowestApis(10))
                    .mostFrequentApis(getMostFrequentApis(10))
                    .requestTimeSeries(getRequestTimeSeries())
                    .build();
        } catch (Exception e) {
            log.error("获取API统计信息失败", e);
            return ApiStatisticsDTO.builder().build();
        }
    }

    /**
     * 按端点统计请求数
     */
    private Map<String, Long> getRequestsByEndpoint() {
        Map<String, Long> result = new HashMap<>();
        apiStatsMap.values().forEach(stats -> {
            result.merge(stats.endpoint, stats.requestCount.get(), Long::sum);
        });
        return result;
    }

    /**
     * 按方法统计请求数
     */
    private Map<String, Long> getRequestsByMethod() {
        Map<String, Long> result = new HashMap<>();
        apiStatsMap.values().forEach(stats -> {
            result.merge(stats.method, stats.requestCount.get(), Long::sum);
        });
        return result;
    }

    /**
     * 按状态码统计请求数
     */
    private Map<Integer, Long> getRequestsByStatus() {
        Map<Integer, Long> result = new HashMap<>();
        apiStatsMap.values().forEach(stats -> {
            stats.statusCodeCounts.forEach((code, count) -> {
                result.merge(code, count.get(), Long::sum);
            });
        });
        return result;
    }

    /**
     * 获取最慢的API
     */
    private List<SlowApiDTO> getSlowestApis(int limit) {
        return apiStatsMap.values().stream()
                .map(stats -> {
                    long count = stats.requestCount.get();
                    long totalTime = stats.totalResponseTime.get();
                    return SlowApiDTO.builder()
                            .endpoint(stats.endpoint)
                            .method(stats.method)
                            .avgResponseTime(count > 0 ? totalTime / count : 0)
                            .maxResponseTime(stats.maxResponseTime.get())
                            .requestCount(count)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getAvgResponseTime(), a.getAvgResponseTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取最频繁的API
     */
    private List<FrequentApiDTO> getMostFrequentApis(int limit) {
        return apiStatsMap.values().stream()
                .map(stats -> {
                    long count = stats.requestCount.get();
                    long totalTime = stats.totalResponseTime.get();
                    return FrequentApiDTO.builder()
                            .endpoint(stats.endpoint)
                            .method(stats.method)
                            .requestCount(count)
                            .avgResponseTime(count > 0 ? (totalTime * 1.0 / count) : 0.0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getRequestCount(), a.getRequestCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取请求时间序列数据
     */
    private List<TimeSeriesDataDTO> getRequestTimeSeries() {
        return hourlyRequestCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> TimeSeriesDataDTO.builder()
                        .timestamp(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 清理超过24小时的时间序列数据
     */
    private void cleanOldTimeSeriesData() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            String cutoffKey = cutoff.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));
            
            hourlyRequestCounts.entrySet().removeIf(entry -> 
                entry.getKey().compareTo(cutoffKey) < 0
            );
        } catch (Exception e) {
            log.error("清理旧时间序列数据失败", e);
        }
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        apiStatsMap.clear();
        hourlyRequestCounts.clear();
        totalRequests.set(0);
        successRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        log.debug("API统计数据已重置");
    }
}
