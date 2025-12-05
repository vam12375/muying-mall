package com.muyingmall.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 登录限流器 - 基于IP和用户名的双重限流
 * 使用Guava RateLimiter实现令牌桶算法
 */
@Slf4j
@Component
public class LoginRateLimiter {

    // IP级别限流：每个IP每秒最多10次登录请求
    private final LoadingCache<String, RateLimiter> ipLimiters = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(10.0); // 每秒10个令牌
                }
            });

    // 用户名级别限流：每个用户名每秒最多5次登录请求
    private final LoadingCache<String, RateLimiter> userLimiters = CacheBuilder.newBuilder()
            .maximumSize(50000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(5.0); // 每秒5个令牌
                }
            });

    /**
     * 尝试获取登录许可
     * 
     * @param ip 客户端IP
     * @param username 用户名
     * @return true表示允许登录，false表示被限流
     */
    public boolean tryAcquire(String ip, String username) {
        try {
            // IP级别限流检查
            RateLimiter ipLimiter = ipLimiters.get(ip);
            if (!ipLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                log.warn("登录请求被IP限流拦截: ip={}", ip);
                return false;
            }

            // 用户名级别限流检查
            RateLimiter userLimiter = userLimiters.get(username);
            if (!userLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                log.warn("登录请求被用户名限流拦截: username={}", username);
                return false;
            }

            return true;
        } catch (ExecutionException e) {
            log.error("限流器获取失败", e);
            return true; // 限流器异常时放行，避免影响正常业务
        }
    }
}
