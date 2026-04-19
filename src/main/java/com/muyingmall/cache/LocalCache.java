package com.muyingmall.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地缓存 (L1) - 基于 Caffeine
 * 配合 Redis (L2) 组成二级缓存架构
 *
 * 为什么按 TTL 粒度维护多实例：
 * Caffeine 的 expireAfterWrite 是全局策略，不同 key 需要不同 TTL 时必须使用不同的 Cache 实例
 * 本实现按 localExpireSeconds 作为分片维度，复用相同 TTL 的 Cache 实例
 */
@Slf4j
@Component
public class LocalCache {

    @Value("${cache.local.enabled:true}")
    private boolean enabled;

    @Value("${cache.local.max-size:10000}")
    private long maxSize;

    @Value("${cache.local.stats-enabled:true}")
    private boolean statsEnabled;

    /**
     * 按 TTL 分片的 Caffeine Cache 实例
     * key: TTL 秒数；value: 对应 TTL 的 Cache 实例
     */
    @Getter
    private final ConcurrentMap<Long, Cache<String, Object>> caches = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("本地缓存 Caffeine 初始化 - enabled={}, maxSize={}, statsEnabled={}",
                enabled, maxSize, statsEnabled);
    }

    /**
     * 读取本地缓存
     *
     * @param ttlSeconds TTL 分片
     * @param key        缓存 key
     * @return 值（null 表示未命中）
     */
    public Object get(long ttlSeconds, String key) {
        if (!enabled) {
            return null;
        }
        Cache<String, Object> cache = caches.get(ttlSeconds);
        if (cache == null) {
            return null;
        }
        return cache.getIfPresent(key);
    }

    /**
     * 写入本地缓存
     */
    public void put(long ttlSeconds, String key, Object value) {
        if (!enabled || value == null) {
            return;
        }
        Cache<String, Object> cache = caches.computeIfAbsent(ttlSeconds, this::createCache);
        cache.put(key, value);
    }

    /**
     * 移除指定 key（精确）
     */
    public void evict(String key) {
        if (!enabled) {
            return;
        }
        // 所有分片都清一下，成本极低
        caches.values().forEach(cache -> cache.invalidate(key));
    }

    /**
     * 按前缀移除（前缀匹配扫描本地 keyset）
     */
    public void evictByPrefix(String prefix) {
        if (!enabled || prefix == null || prefix.isEmpty()) {
            return;
        }
        caches.values().forEach(cache -> {
            Set<String> matched = cache.asMap().keySet().stream()
                    .filter(k -> k.startsWith(prefix))
                    .collect(java.util.stream.Collectors.toSet());
            if (!matched.isEmpty()) {
                cache.invalidateAll(matched);
                log.debug("本地缓存按前缀清除: prefix={}, 数量={}", prefix, matched.size());
            }
        });
    }

    /**
     * 清空所有本地缓存
     */
    public void evictAll() {
        caches.values().forEach(Cache::invalidateAll);
    }

    /**
     * 获取指定 TTL 分片的命中统计（用于监控）
     */
    public CacheStats stats(long ttlSeconds) {
        Cache<String, Object> cache = caches.get(ttlSeconds);
        return cache != null ? cache.stats() : CacheStats.empty();
    }

    private Cache<String, Object> createCache(long ttlSeconds) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds));
        if (statsEnabled) {
            builder.recordStats();
        }
        log.info("创建本地缓存实例 - ttl={}s, maxSize={}", ttlSeconds, maxSize);
        return builder.build();
    }
}
