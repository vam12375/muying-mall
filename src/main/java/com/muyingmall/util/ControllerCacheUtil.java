package com.muyingmall.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Controller层缓存工具类
 * 用于统一管理Controller层的响应缓存
 * 
 * 优化目标：减少高延迟接口的响应时间
 * 核心策略：缓存完整的响应结果，避免重复查询
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ControllerCacheUtil {

    private final RedisUtil redisUtil;

    /**
     * 带缓存的查询方法
     * 
     * @param cacheKey 缓存键
     * @param expireSeconds 过期时间（秒）
     * @param supplier 数据查询函数
     * @param <T> 返回类型
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getWithCache(String cacheKey, long expireSeconds, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            long cacheTime = System.currentTimeMillis() - startTime;
            log.debug("Controller缓存命中: key={}, 耗时={}ms", cacheKey, cacheTime);
            return (T) cached;
        }
        
        // 缓存未命中，执行查询
        log.debug("Controller缓存未命中，执行查询: key={}", cacheKey);
        long queryStartTime = System.currentTimeMillis();
        T result = supplier.get();
        long queryTime = System.currentTimeMillis() - queryStartTime;
        
        // 缓存结果
        if (result != null) {
            redisUtil.set(cacheKey, result, expireSeconds);
            log.debug("Controller缓存已更新: key={}, 查询耗时={}ms, 缓存时间={}秒", 
                    cacheKey, queryTime, expireSeconds);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        log.debug("Controller查询完成: key={}, 总耗时={}ms, 缓存命中=false", cacheKey, totalTime);
        
        return result;
    }

    /**
     * 清除缓存
     * 
     * @param cacheKeys 缓存键列表
     */
    public void clearCache(String... cacheKeys) {
        if (cacheKeys == null || cacheKeys.length == 0) {
            return;
        }
        
        for (String cacheKey : cacheKeys) {
            redisUtil.del(cacheKey);
            log.debug("Controller缓存已清除: key={}", cacheKey);
        }
    }

    /**
     * 使用模式匹配清除缓存
     * 用于清除带有通配符的缓存键（如分页缓存）
     * 
     * @param pattern 缓存键模式（支持*通配符）
     */
    public void clearCacheByPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }
        
        java.util.Set<String> keys = redisUtil.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys);
            log.debug("Controller缓存已清除（模式匹配）: pattern={}, 清除数量={}", pattern, keys.size());
        }
    }

    /**
     * 清除用户相关的所有缓存
     * 
     * @param userId 用户ID
     * @param username 用户名
     */
    public void clearUserRelatedCache(Integer userId, String username) {
        if (userId == null && username == null) {
            return;
        }
        
        log.debug("开始清除用户相关缓存: userId={}, username={}", userId, username);
        
        // 清除用户信息缓存
        if (username != null) {
            clearCache("user:info:" + username);
        }
        
        // 清除用户统计相关缓存
        if (userId != null) {
            clearCache(
                "order:stats:" + userId,
                "order:list:" + userId,
                "user:favorites:" + userId,
                "user:addresses:" + userId,
                "user:coupons:" + userId,
                "cart:list:" + userId,
                "cart:total:" + userId,
                "points:info:" + userId
            );
        }
        
        log.debug("用户相关缓存清除完成: userId={}, username={}", userId, username);
    }
}
