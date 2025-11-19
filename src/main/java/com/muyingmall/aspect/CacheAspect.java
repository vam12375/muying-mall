package com.muyingmall.aspect;

import com.muyingmall.annotation.CacheEvict;
import com.muyingmall.annotation.Cacheable;
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
 * 处理@Cacheable和@CacheEvict注解
 * 
 * Source: 性能优化 - Redis缓存增强
 * 遵循协议: AURA-X-KYS (KISS/YAGNI/SOLID)
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {

    private final RedisUtil redisUtil;

    /**
     * 处理@Cacheable注解
     * 先查缓存，缓存不存在则执行方法并缓存结果
     */
    @Around("@annotation(com.muyingmall.annotation.Cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        // 生成缓存键
        String cacheKey = generateCacheKey(cacheable.keyPrefix(), joinPoint.getArgs(), cacheable.useParams());

        // 尝试从缓存获取
        Object cachedValue = redisUtil.get(cacheKey);
        if (cachedValue != null) {
            log.debug("缓存命中: {}", cacheKey);
            return cachedValue;
        }

        // 缓存未命中，执行方法
        log.debug("缓存未命中，执行方法: {}", cacheKey);
        Object result = joinPoint.proceed();

        // 缓存结果（如果不为null）
        if (result != null) {
            redisUtil.set(cacheKey, result, cacheable.expireTime());
            log.debug("结果已缓存: {}, 过期时间: {}秒", cacheKey, cacheable.expireTime());
        }

        return result;
    }

    /**
     * 处理@CacheEvict注解
     * 清除指定的缓存
     */
    @Around("@annotation(com.muyingmall.annotation.CacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        // 先执行方法
        Object result = joinPoint.proceed();

        // 清除缓存
        for (String keyPrefix : cacheEvict.keyPrefixes()) {
            if (cacheEvict.allEntries()) {
                // 清除所有匹配的键
                Set<String> keys = redisUtil.keys(keyPrefix + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisUtil.del(keys.toArray(new String[0]));
                    log.debug("已清除缓存: {} (共{}个键)", keyPrefix, keys.size());
                }
            } else {
                // 只清除精确匹配的键
                redisUtil.del(keyPrefix);
                log.debug("已清除缓存: {}", keyPrefix);
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

        // 将参数转换为字符串并拼接
        String paramStr = Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.toString())
                .collect(Collectors.joining(":"));

        return prefix + paramStr;
    }
}
