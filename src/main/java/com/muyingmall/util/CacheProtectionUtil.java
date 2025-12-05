package com.muyingmall.util;

import com.muyingmall.common.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存防护工具类
 * 提供缓存穿透、缓存击穿和缓存雪崩保护机制
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheProtectionUtil {

    private final RedisUtil redisUtil;

    // 空值缓存的过期时间(秒)
    private static final long NULL_VALUE_EXPIRE_TIME = 60;

    // 布隆过滤器缓存键前缀
    private static final String BLOOM_FILTER_KEY_PREFIX = "bloom:";

    // 缓存锁过期时间(秒)
    private static final long LOCK_EXPIRE_TIME = 10;

    /**
     * 防止缓存穿透的查询方法
     * 对于不存在的数据也会缓存一个空值，避免频繁查询数据库
     *
     * @param cacheKey   缓存键
     * @param expireTime 缓存过期时间(秒)
     * @param dbFallback 数据库查询函数
     * @param <T>        返回值类型
     * @return 查询结果
     */
    public <T> T queryWithProtection(String cacheKey, long expireTime, Callable<T> dbFallback) {
        // 1. 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);

        // 2. 判断是否命中
        if (cacheResult != null) {
            // 特殊处理：如果是空值标记，则返回null
            if (CacheConstants.EMPTY_CACHE_VALUE.equals(cacheResult.toString())) {
                log.debug("命中空值缓存: {}", cacheKey);
                return null;
            }

            log.debug("缓存命中: {}", cacheKey);
            return (T) cacheResult;
        }

        // 3. 查询数据库
        try {
            // 执行数据库查询
            T dbResult = dbFallback.call();

            // 4. 写入缓存
            if (dbResult != null) {
                // 添加随机过期时间，避免缓存雪崩
                long finalExpireTime = getRandomExpireTime(expireTime);
                redisUtil.set(cacheKey, dbResult, finalExpireTime);
                log.debug("将查询结果写入缓存: key={}, expireTime={}s", cacheKey, finalExpireTime);
            } else {
                // 缓存空值，避免缓存穿透
                redisUtil.set(cacheKey, CacheConstants.EMPTY_CACHE_VALUE, NULL_VALUE_EXPIRE_TIME);
                log.debug("数据不存在，写入空值缓存: key={}, expireTime={}s", cacheKey, NULL_VALUE_EXPIRE_TIME);
            }

            return dbResult;
        } catch (Exception e) {
            log.error("查询数据库失败: key={}, error={}", cacheKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 防止缓存击穿的查询方法
     * 使用setIfAbsent实现简单的互斥锁，确保同一时刻只有一个线程查询数据库
     * 优化：未获取锁时直接查询数据库，避免高并发下大量请求返回null
     *
     * @param cacheKey   缓存键
     * @param lockKey    锁键(可以与cacheKey相同)
     * @param expireTime 缓存过期时间(秒)
     * @param dbFallback 数据库查询函数
     * @param <T>        返回值类型
     * @return 查询结果
     */
    public <T> T queryWithMutex(String cacheKey, String lockKey, long expireTime, Callable<T> dbFallback) {
        // 1. 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);

        // 2. 判断是否命中
        if (cacheResult != null) {
            // 特殊处理：如果是空值标记，则返回null
            if (CacheConstants.EMPTY_CACHE_VALUE.equals(cacheResult.toString())) {
                return null;
            }
            return (T) cacheResult;
        }

        // 3. 尝试获取简单互斥锁（使用setIfAbsent，更可靠）
        boolean lockAcquired = redisUtil.setIfAbsent(lockKey, "1", LOCK_EXPIRE_TIME);

        try {
            if (lockAcquired) {
                // 双重检查，再次查询缓存
                cacheResult = redisUtil.get(cacheKey);
                if (cacheResult != null) {
                    if (CacheConstants.EMPTY_CACHE_VALUE.equals(cacheResult.toString())) {
                        return null;
                    }
                    return (T) cacheResult;
                }
            }

            // 4. 查询数据库（无论是否获取锁都查询，避免高并发下返回null）
            T dbResult = dbFallback.call();

            // 5. 写入缓存（只有获取锁的线程写入，避免并发写入）
            if (lockAcquired) {
                if (dbResult != null) {
                    long finalExpireTime = getRandomExpireTime(expireTime);
                    redisUtil.set(cacheKey, dbResult, finalExpireTime);
                    log.debug("将查询结果写入缓存: key={}, expireTime={}s", cacheKey, finalExpireTime);
                } else {
                    // 缓存空值，避免缓存穿透
                    redisUtil.set(cacheKey, CacheConstants.EMPTY_CACHE_VALUE, NULL_VALUE_EXPIRE_TIME);
                    log.debug("数据不存在，写入空值缓存: key={}", cacheKey);
                }
            }

            return dbResult;
        } catch (Exception e) {
            log.error("查询数据库失败: key={}, error={}", cacheKey, e.getMessage(), e);
            return null;
        } finally {
            // 6. 释放锁
            if (lockAcquired) {
                redisUtil.del(lockKey);
            }
        }
    }

    /**
     * 使用布隆过滤器优化的查询方法
     * 结合布隆过滤器和空值缓存防止缓存穿透
     *
     * @param cacheKey        缓存键
     * @param id              数据ID，用于在布隆过滤器中判断
     * @param bloomFilterName 布隆过滤器名称
     * @param expireTime      缓存过期时间(秒)
     * @param dbFallback      数据库查询函数
     * @param <T>             返回值类型
     * @return 查询结果
     */
    public <T> T queryWithBloomFilter(String cacheKey, Object id, String bloomFilterName, long expireTime,
            Callable<T> dbFallback) {
        // 1. 判断布隆过滤器
        String bloomKey = BLOOM_FILTER_KEY_PREFIX + bloomFilterName;

        // 简化处理：这里使用Set实现布隆过滤器效果
        // 实际应用中应该使用专门的布隆过滤器库
        boolean mayExist = redisUtil.sIsMember(bloomKey, id.toString());

        // 如果布隆过滤器判断这个值一定不存在，直接返回null
        if (!mayExist) {
            log.debug("布隆过滤器拦截: key={}, id={}", cacheKey, id);
            return null;
        }

        // 2. 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);

        // 3. 判断是否命中
        if (cacheResult != null) {
            // 特殊处理：如果是空值标记，则返回null
            if (CacheConstants.EMPTY_CACHE_VALUE.equals(cacheResult.toString())) {
                return null;
            }
            return (T) cacheResult;
        }

        // 4. 查询数据库
        try {
            T dbResult = dbFallback.call();

            // 5. 写入缓存
            if (dbResult != null) {
                // 添加随机过期时间，避免缓存雪崩
                long finalExpireTime = getRandomExpireTime(expireTime);
                redisUtil.set(cacheKey, dbResult, finalExpireTime);

                // 确保该ID存在于布隆过滤器中
                redisUtil.sAdd(bloomKey, id.toString());

                log.debug("将查询结果写入缓存: key={}, expireTime={}s", cacheKey, finalExpireTime);
            } else {
                // 缓存空值，避免缓存穿透
                redisUtil.set(cacheKey, CacheConstants.EMPTY_CACHE_VALUE, NULL_VALUE_EXPIRE_TIME);
                log.debug("数据不存在，写入空值缓存: key={}, expireTime={}s", cacheKey, NULL_VALUE_EXPIRE_TIME);
            }

            return dbResult;
        } catch (Exception e) {
            log.error("查询数据库失败: key={}, error={}", cacheKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 带降级机制的查询方法
     * 当缓存和数据库都查询失败时，使用降级方法返回兜底数据
     *
     * @param cacheKey        缓存键
     * @param expireTime      缓存过期时间(秒)
     * @param dbFallback      数据库查询函数
     * @param degradeFunction 降级函数
     * @param <T>             返回值类型
     * @return 查询结果
     */
    public <T> T queryWithDegradation(String cacheKey, long expireTime, Callable<T> dbFallback,
            Function<String, T> degradeFunction) {
        // 1. 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);

        // 2. 判断是否命中
        if (cacheResult != null) {
            // 特殊处理：如果是空值标记，则返回降级结果
            if (CacheConstants.EMPTY_CACHE_VALUE.equals(cacheResult.toString())) {
                log.debug("命中空值缓存，使用降级方法: {}", cacheKey);
                return degradeFunction.apply(cacheKey);
            }

            log.debug("缓存命中: {}", cacheKey);
            return (T) cacheResult;
        }

        // 3. 查询数据库
        try {
            T dbResult = dbFallback.call();

            // 4. 写入缓存
            if (dbResult != null) {
                // 添加随机过期时间，避免缓存雪崩
                long finalExpireTime = getRandomExpireTime(expireTime);
                redisUtil.set(cacheKey, dbResult, finalExpireTime);
                log.debug("将查询结果写入缓存: key={}, expireTime={}s", cacheKey, finalExpireTime);
                return dbResult;
            } else {
                // 缓存空值，避免缓存穿透
                redisUtil.set(cacheKey, CacheConstants.EMPTY_CACHE_VALUE, NULL_VALUE_EXPIRE_TIME);
                log.debug("数据不存在，写入空值缓存: key={}, expireTime={}s", cacheKey, NULL_VALUE_EXPIRE_TIME);

                // 返回降级结果
                log.debug("使用降级方法: {}", cacheKey);
                return degradeFunction.apply(cacheKey);
            }
        } catch (Exception e) {
            log.error("查询数据库失败，使用降级方法: key={}, error={}", cacheKey, e.getMessage(), e);
            return degradeFunction.apply(cacheKey);
        }
    }

    /**
     * 获取带有随机波动的过期时间
     * 避免大量缓存同时过期导致缓存雪崩
     *
     * @param baseExpireTime 基础过期时间(秒)
     * @return 带有随机波动的过期时间(秒)
     */
    private long getRandomExpireTime(long baseExpireTime) {
        if (baseExpireTime <= 0) {
            return baseExpireTime;
        }

        // 添加随机波动，范围为基础时间的80%~120%
        long min = (long) (baseExpireTime * 0.8);
        long max = (long) (baseExpireTime * 1.2);

        return min + ThreadLocalRandom.current().nextLong(max - min);
    }
}