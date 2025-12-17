package com.muyingmall.config;

import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存统一管理配置类
 * 负责缓存的统一管理、监控和维护
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CacheManagerConfig {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存命中统计
    private final Map<String, AtomicLong> cacheHitCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheMissCount = new ConcurrentHashMap<>();

    // 缓存前缀映射
    private final Map<String, Long> cachePrefixExpireTime = new HashMap<>();

    @Value("${cache.global.enabled:true}")
    private boolean cacheEnabled;

    @Value("${cache.global.default-ttl:3600}")
    private long defaultTtl;

    @Value("${redis.stats.enabled:false}")
    private boolean statsEnabled;

    /**
     * 初始化缓存配置
     */
    @PostConstruct
    public void init() {
        // 初始化缓存前缀与过期时间的映射关系
        initCachePrefixMap();

        log.debug("缓存管理器初始化完成，缓存状态: {}", cacheEnabled ? "启用" : "禁用");
        log.debug("缓存统计功能: {}", statsEnabled ? "启用" : "禁用");
        log.debug("默认缓存过期时间: {}秒", defaultTtl);
    }

    /**
     * 初始化缓存前缀与过期时间的映射关系
     */
    private void initCachePrefixMap() {
        // 商品相关
        cachePrefixExpireTime.put(CacheConstants.PRODUCT_KEY_PREFIX, CacheConstants.PRODUCT_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.PRODUCT_HOT_KEY, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.PRODUCT_NEW_KEY, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.PRODUCT_RECOMMEND_KEY, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);

        // 分类相关
        cachePrefixExpireTime.put(CacheConstants.CATEGORY_KEY_PREFIX, CacheConstants.CATEGORY_EXPIRE_TIME);

        // 品牌相关
        cachePrefixExpireTime.put(CacheConstants.BRAND_KEY_PREFIX, CacheConstants.BRAND_EXPIRE_TIME);

        // 用户相关
        cachePrefixExpireTime.put(CacheConstants.USER_KEY_PREFIX, CacheConstants.USER_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.USER_TOKEN_KEY, CacheConstants.USER_TOKEN_EXPIRE_TIME);

        // 订单相关
        cachePrefixExpireTime.put(CacheConstants.ORDER_KEY_PREFIX, CacheConstants.ORDER_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.USER_ORDER_LIST_KEY, CacheConstants.ORDER_LIST_EXPIRE_TIME);

        // 内容和分析相关
        cachePrefixExpireTime.put(CacheConstants.CONTENT_KEY_PREFIX, CacheConstants.MEDIUM_EXPIRE_TIME);
        cachePrefixExpireTime.put(CacheConstants.ANALYTICS_KEY_PREFIX, CacheConstants.ANALYTICS_EXPIRE_TIME);
    }

    /**
     * 根据key获取适合的过期时间
     * 
     * @param key 缓存键
     * @return 过期时间(秒)
     */
    public long getExpireTime(String key) {
        if (key == null || key.isEmpty()) {
            return defaultTtl;
        }

        // 根据前缀匹配过期时间
        for (Map.Entry<String, Long> entry : cachePrefixExpireTime.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 默认过期时间
        return defaultTtl;
    }

    /**
     * 缓存命中记录
     * 
     * @param cacheType 缓存类型前缀
     */
    public void recordHit(String cacheType) {
        if (!statsEnabled) {
            return;
        }

        cacheHitCount.computeIfAbsent(cacheType, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 缓存未命中记录
     * 
     * @param cacheType 缓存类型前缀
     */
    public void recordMiss(String cacheType) {
        if (!statsEnabled) {
            return;
        }

        cacheMissCount.computeIfAbsent(cacheType, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取缓存命中率
     * 
     * @param cacheType 缓存类型前缀
     * @return 命中率(0-100)
     */
    public double getHitRate(String cacheType) {
        if (!statsEnabled) {
            return 0;
        }

        AtomicLong hit = cacheHitCount.getOrDefault(cacheType, new AtomicLong(0));
        AtomicLong miss = cacheMissCount.getOrDefault(cacheType, new AtomicLong(0));

        long total = hit.get() + miss.get();
        if (total == 0) {
            return 0;
        }

        return (double) hit.get() / total * 100;
    }

    /**
     * 清除指定前缀的缓存
     * 
     * @param prefixPattern 前缀模式
     * @return 清除的键数量
     */
    public long clearCache(String prefixPattern) {
        try {
            return redisUtil.deleteByScan(prefixPattern);
        } catch (Exception e) {
            log.error("清除缓存失败: prefix={}, error={}", prefixPattern, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 定期打印缓存统计信息
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void printCacheStats() {
        if (!statsEnabled) {
            return;
        }

        log.debug("====== 缓存统计信息 ======");

        // 统计各类型缓存命中率
        for (String cacheType : cachePrefixExpireTime.keySet()) {
            double hitRate = getHitRate(cacheType);
            long hits = cacheHitCount.getOrDefault(cacheType, new AtomicLong(0)).get();
            long misses = cacheMissCount.getOrDefault(cacheType, new AtomicLong(0)).get();
            long total = hits + misses;

            if (total > 0) {
                log.debug("缓存类型: {}, 命中率: {:.2f}%, 总请求: {}, 命中: {}, 未命中: {}",
                        cacheType, hitRate, total, hits, misses);
            }
        }

        // 统计缓存键数量
        try {
            long totalKeys = redisTemplate.getConnectionFactory().getConnection().dbSize();
            log.debug("Redis总键数: {}", totalKeys);

            // 统计各类型缓存键数量
            for (String prefix : cachePrefixExpireTime.keySet()) {
                Set<String> keys = redisUtil.keys(prefix + "*");
                if (keys != null) {
                    log.debug("缓存类型: {}, 键数量: {}", prefix, keys.size());
                }
            }
        } catch (Exception e) {
            log.error("统计缓存键数量失败: {}", e.getMessage());
        }

        log.debug("====== 缓存统计信息结束 ======");
    }

    /**
     * 清理过期缓存
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredCache() {
        log.debug("开始执行过期缓存清理任务");

        // 记录清理前的键数量
        long beforeCount = 0;
        try {
            beforeCount = redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.error("获取清理前键数量失败: {}", e.getMessage());
        }

        // 清理会导致缓存雪崩的热点数据缓存
        // 这里只做示例，实际上Redis会自动清理过期键
        clearCache(CacheConstants.PRODUCT_HOT_KEY + "*");
        clearCache(CacheConstants.PRODUCT_NEW_KEY + "*");
        clearCache(CacheConstants.PRODUCT_RECOMMEND_KEY + "*");

        // 记录清理后的键数量
        long afterCount = 0;
        try {
            afterCount = redisTemplate.getConnectionFactory().getConnection().dbSize();
            log.debug("过期缓存清理完成，清理前: {}个键，清理后: {}个键，共清理: {}个键",
                    beforeCount, afterCount, beforeCount - afterCount);
        } catch (Exception e) {
            log.error("获取清理后键数量失败: {}", e.getMessage());
        }
    }
}