package com.muyingmall.aspect;

import com.muyingmall.annotation.CacheEvict;
import com.muyingmall.annotation.Cacheable;
import com.muyingmall.cache.CacheEvictPublisher;
import com.muyingmall.cache.LocalCache;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存切面
 * 处理 @Cacheable 和 @CacheEvict 注解
 * 支持：
 *   - Redis (L2) 单层缓存（默认）
 *   - Caffeine (L1) + Redis (L2) 二级缓存（useLocalCache=true）
 * 二级缓存失效通过 Redis Pub/Sub 广播保证多节点 L1 一致性
 *
 * 来源：性能优化 - Redis + Caffeine 二级缓存增强
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {

    private final RedisUtil redisUtil;
    private final LocalCache localCache;
    private final CacheEvictPublisher cacheEvictPublisher;

    /**
     * 处理 @Cacheable 注解
     * 读流程：
     *   useLocalCache=true 时：L1 -> L2 -> DB -> 回填 L1+L2
     *   useLocalCache=false 时：L2 -> DB -> 回填 L2
     */
    @Around("@annotation(com.muyingmall.annotation.Cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        if (cacheable == null) {
            return joinPoint.proceed();
        }

        String cacheKey = generateCacheKey(cacheable.keyPrefix(), joinPoint.getArgs(), cacheable.useParams());
        boolean useLocal = cacheable.useLocalCache();
        long localTtl = cacheable.localExpireSeconds();

        // L1 查询
        if (useLocal) {
            Object l1Value = localCache.get(localTtl, cacheKey);
            if (l1Value != null) {
                log.debug("L1 命中: {}", cacheKey);
                return l1Value;
            }
        }

        // L2 查询
        Object l2Value = redisUtil.get(cacheKey);
        if (l2Value != null) {
            log.debug("L2 命中: {}", cacheKey);
            if (useLocal) {
                localCache.put(localTtl, cacheKey, l2Value);
            }
            return l2Value;
        }

        // 缓存全部未命中，执行方法
        log.debug("缓存未命中，执行方法: {}", cacheKey);
        Object result = joinPoint.proceed();
        if (result != null) {
            redisUtil.set(cacheKey, result, cacheable.expireTime());
            if (useLocal) {
                localCache.put(localTtl, cacheKey, result);
            }
            log.debug("结果已缓存: key={}, L2 TTL={}s, L1 TTL={}s",
                    cacheKey, cacheable.expireTime(), useLocal ? localTtl : 0);
        }
        return result;
    }

    /**
     * 处理 @CacheEvict 注解
     * 清除 L2；useLocalCache=true 时同步清除本节点 L1 并发布 Pub/Sub 广播
     */
    @Around("@annotation(com.muyingmall.annotation.CacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        Object result = joinPoint.proceed();
        if (cacheEvict == null) {
            return result;
        }

        boolean useLocal = cacheEvict.useLocalCache();
        for (String keyPrefix : cacheEvict.keyPrefixes()) {
            if (cacheEvict.allEntries()) {
                // 前缀批量清 L2
                Set<String> keys = redisUtil.scan(keyPrefix + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisUtil.del(keys.toArray(new String[0]));
                    log.debug("L2 按前缀清除: {} ({} keys)", keyPrefix, keys.size());
                }
                // L1 本地 + 广播
                if (useLocal) {
                    localCache.evictByPrefix(keyPrefix);
                    cacheEvictPublisher.publishPrefix(keyPrefix);
                }
            } else {
                // 精确清 L2
                redisUtil.del(keyPrefix);
                log.debug("L2 精确清除: {}", keyPrefix);
                // L1 本地 + 广播
                if (useLocal) {
                    localCache.evict(keyPrefix);
                    cacheEvictPublisher.publishKey(keyPrefix);
                }
            }
        }
        return result;
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String prefix, Object[] args, boolean useParams) {
        if (!useParams || args == null || args.length == 0) {
            return prefix;
        }
        String paramStr = Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.toString())
                .collect(Collectors.joining(":"));
        return prefix + paramStr;
    }
}
