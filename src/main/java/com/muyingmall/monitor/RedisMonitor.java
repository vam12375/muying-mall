package com.muyingmall.monitor;

import com.muyingmall.dto.SystemMetricsDTO.RedisMetricsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Redis监控组件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMonitor {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取Redis监控指标
     */
    public RedisMetricsDTO getRedisMetrics() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info();
            Properties stats = connection.info("stats");
            Properties keyspace = connection.info("keyspace");

            // 计算命中率
            long hits = getLongValue(stats, "keyspace_hits");
            long misses = getLongValue(stats, "keyspace_misses");
            double hitRate = (hits + misses) > 0 ? (hits * 100.0 / (hits + misses)) : 0.0;

            return RedisMetricsDTO.builder()
                    .status("UP")
                    .version(info.getProperty("redis_version", "unknown"))
                    .connectedClients(getIntValue(info, "connected_clients"))
                    .usedMemory(getLongValue(info, "used_memory"))
                    .usedMemoryHuman(info.getProperty("used_memory_human", "0B"))
                    .memFragmentationRatio(getDoubleValue(info, "mem_fragmentation_ratio"))
                    .totalCommandsProcessed(getLongValue(stats, "total_commands_processed"))
                    .opsPerSec(getLongValue(stats, "instantaneous_ops_per_sec"))
                    .keyspaceHits(hits)
                    .keyspaceMisses(misses)
                    .hitRate(hitRate)
                    .totalKeys(getTotalKeys(keyspace))
                    .expiredKeys(getIntValue(stats, "expired_keys"))
                    .evictedKeys(getIntValue(stats, "evicted_keys"))
                    .rdbLastSaveTime(info.getProperty("rdb_last_save_time", "unknown"))
                    .aofEnabled(info.getProperty("aof_enabled", "0"))
                    .build();
        } catch (Exception e) {
            log.error("获取Redis监控指标失败", e);
            return RedisMetricsDTO.builder()
                    .status("DOWN")
                    .build();
        }
    }

    /**
     * 从Properties中获取Long值
     */
    private Long getLongValue(Properties props, String key) {
        try {
            String value = props.getProperty(key, "0");
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 从Properties中获取Integer值
     */
    private Integer getIntValue(Properties props, String key) {
        try {
            String value = props.getProperty(key, "0");
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从Properties中获取Double值
     */
    private Double getDoubleValue(Properties props, String key) {
        try {
            String value = props.getProperty(key, "0.0");
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取总键数
     */
    private Integer getTotalKeys(Properties keyspace) {
        int total = 0;
        try {
            for (String key : keyspace.stringPropertyNames()) {
                if (key.startsWith("db")) {
                    String value = keyspace.getProperty(key);
                    // 格式: keys=xxx,expires=xxx,avg_ttl=xxx
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        if (part.startsWith("keys=")) {
                            total += Integer.parseInt(part.substring(5));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取Redis总键数失败", e);
        }
        return total;
    }

    /**
     * 检查Redis连接状态
     */
    public boolean isRedisAvailable() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            return "PONG".equals(pong);
        } catch (Exception e) {
            log.error("Redis连接检查失败", e);
            return false;
        }
    }
}
