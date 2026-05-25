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
 * 秒杀入口限流器。
 * 只在控制器入口做轻量削峰，真正的库存一致性仍由 Redis Lua 和数据库条件扣减兜底。
 */
@Slf4j
@Component
public class SeckillRateLimiter {

    private final LoadingCache<String, RateLimiter> ipLimiters = CacheBuilder.newBuilder()
            .maximumSize(20000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(3.0);
                }
            });

    private final LoadingCache<String, RateLimiter> userProductLimiters = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(1.0);
                }
            });

    public boolean tryAcquire(String clientIp, Integer userId, Long seckillProductId) {
        String safeIp = (clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp;
        String userProductKey = userId + ":" + seckillProductId;

        try {
            boolean ipAllowed = ipLimiters.get(safeIp).tryAcquire();
            boolean userAllowed = userProductLimiters.get(userProductKey).tryAcquire();

            if (!ipAllowed || !userAllowed) {
                log.warn("秒杀请求被限流: ip={}, userId={}, seckillProductId={}, ipAllowed={}, userAllowed={}",
                        safeIp, userId, seckillProductId, ipAllowed, userAllowed);
                return false;
            }
            return true;
        } catch (ExecutionException e) {
            log.error("秒杀限流器获取失败: ip={}, userId={}, seckillProductId={}", safeIp, userId, seckillProductId, e);
            return false;
        }
    }
}
