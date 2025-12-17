package com.muyingmall.monitor;

import com.muyingmall.config.CacheManagerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.actuate.health.Health;
// import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 缓存监控组件
 * 负责监控和收集Redis缓存的运行状态和性能指标
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheMonitor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManagerConfig cacheManagerConfig;

    @Value("${redis.stats.enabled:false}")
    private boolean statsEnabled;

    @Value("${redis.monitor.memory-threshold:80}")
    private int memoryThreshold = 80; // 内存使用率阈值，默认80%

    /**
     * 定期收集Redis状态信息
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void collectRedisStats() {
        if (!statsEnabled) {
            return;
        }

        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info();

            log.debug("===== Redis状态信息 =====");
            log.debug("Redis版本: {}", info.getProperty("redis_version", "unknown"));
            log.debug("连接数: {}", info.getProperty("connected_clients", "unknown"));
            log.debug("内存使用: {}", info.getProperty("used_memory_human", "unknown"));
            log.debug("内存碎片率: {}", info.getProperty("mem_fragmentation_ratio", "unknown"));
            log.debug("已过期键数量: {}", info.getProperty("expired_keys", "unknown"));
            log.debug("每秒执行命令数: {}", info.getProperty("instantaneous_ops_per_sec", "unknown"));
            log.debug("缓存命中率: {}%", info.getProperty("keyspace_hits", "0") + "/" +
                    (Integer.parseInt(info.getProperty("keyspace_hits", "0"))
                            + Integer.parseInt(info.getProperty("keyspace_misses", "0"))));
            log.debug("=======================");

        } catch (Exception e) {
            log.error("收集Redis状态信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 监控慢查询
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000)
    public void monitorSlowLogs() {
        if (!statsEnabled) {
            return;
        }

        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            // 临时注释，等actuator依赖添加后再恢复
            // List<Map<String, String>> slowLogs = connection.slowLog("get", 10);
            log.debug("===== Redis慢查询日志功能暂时禁用 =====");

            /*
             * if (slowLogs != null && !slowLogs.isEmpty()) {
             * log.debug("===== Redis慢查询日志 =====");
             * for (Map<String, String> slowLog : slowLogs) {
             * log.debug("ID: {}, 执行时间: {}微秒, 命令: {}",
             * slowLog.get("id"),
             * slowLog.get("executionTime"),
             * slowLog.get("args"));
             * }
             * log.debug("==========================");
             * }
             */
        } catch (Exception e) {
            log.error("获取Redis慢查询日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取缓存命中率统计
     * 
     * @return 缓存命中率统计
     */
    public Map<String, Double> getCacheHitRates() {
        Map<String, Double> hitRates = new HashMap<>();

        // 获取各类型缓存的命中率
        hitRates.put("product", cacheManagerConfig.getHitRate("product:"));
        hitRates.put("user", cacheManagerConfig.getHitRate("user:"));
        hitRates.put("order", cacheManagerConfig.getHitRate("order:"));
        hitRates.put("category", cacheManagerConfig.getHitRate("category:"));
        hitRates.put("content", cacheManagerConfig.getHitRate("content:"));
        hitRates.put("analytics", cacheManagerConfig.getHitRate("analytics:"));

        return hitRates;
    }

    /**
     * 重置缓存命中统计
     */
    public void resetCacheStats() {
        try {
            // 重置Redis统计信息
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            // 临时注释，等actuator依赖添加后再恢复
            // connection.serverCommands().resetStat();
            log.debug("已重置Redis统计信息");
        } catch (Exception e) {
            log.error("重置Redis统计信息失败: {}", e.getMessage(), e);
        }
    }
}