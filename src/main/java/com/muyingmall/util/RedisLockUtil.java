package com.muyingmall.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis分布式锁工具类
 * 实现基于Redis的分布式锁，支持自动续期
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // 锁前缀
    private static final String LOCK_PREFIX = "lock:";

    // 默认锁过期时间(秒)
    private static final int DEFAULT_EXPIRE_SECONDS = 30;

    // 自动续期的阈值比例(剩余过期时间/总过期时间)，当低于此值时，自动续期
    private static final double RENEW_THRESHOLD = 0.7;

    // 自动续期间隔(毫秒)，默认为过期时间的1/3
    private static final double RENEW_INTERVAL_FACTOR = 1.0 / 3;

    // 获取锁的重试次数
    private static final int RETRY_TIMES = 3;

    // 重试间隔(毫秒)
    private static final long RETRY_INTERVAL = 100;

    // 用于执行自动续期任务的线程池
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    /**
     * 获取分布式锁
     *
     * @param lockKey       锁键
     * @param value         锁值，用于标识锁的持有者
     * @param expireSeconds 锁过期时间(秒)
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String value, int expireSeconds) {
        String key = getLockKey(lockKey);
        return Boolean.TRUE
                .equals(redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS));
    }

    /**
     * 获取分布式锁(使用默认过期时间)
     *
     * @param lockKey 锁键
     * @param value   锁值，用于标识锁的持有者
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String value) {
        return tryLock(lockKey, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 获取分布式锁，支持重试
     *
     * @param lockKey             锁键
     * @param value               锁值，用于标识锁的持有者
     * @param expireSeconds       锁过期时间(秒)
     * @param retryTimes          重试次数
     * @param retryIntervalMillis 重试间隔(毫秒)
     * @return 是否获取成功
     */
    public boolean tryLockWithRetry(String lockKey, String value, int expireSeconds, int retryTimes,
            long retryIntervalMillis) {
        // 首次尝试获取锁
        if (tryLock(lockKey, value, expireSeconds)) {
            return true;
        }

        // 重试指定次数
        for (int i = 0; i < retryTimes; i++) {
            try {
                // 虚拟线程友好的睡眠方式（自动让出CPU）
                TimeUnit.MILLISECONDS.sleep(retryIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            // 重试获取锁
            if (tryLock(lockKey, value, expireSeconds)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取分布式锁，使用默认重试参数
     *
     * @param lockKey       锁键
     * @param value         锁值，用于标识锁的持有者
     * @param expireSeconds 锁过期时间(秒)
     * @return 是否获取成功
     */
    public boolean tryLockWithRetry(String lockKey, String value, int expireSeconds) {
        return tryLockWithRetry(lockKey, value, expireSeconds, RETRY_TIMES, RETRY_INTERVAL);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁键
     * @param value   锁值，必须与加锁时的值相同
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String value) {
        String key = getLockKey(lockKey);

        // 使用Lua脚本确保原子性：只有当锁存在且值匹配时才删除
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result == 1L;
    }

    /**
     * 续期分布式锁
     *
     * @param lockKey       锁键
     * @param value         锁值，必须与加锁时的值相同
     * @param expireSeconds 新的过期时间(秒)
     * @return 是否续期成功
     */
    public boolean renewLock(String lockKey, String value, int expireSeconds) {
        String key = getLockKey(lockKey);

        // 使用Lua脚本确保原子性：只有当锁存在且值匹配时才更新过期时间
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value,
                String.valueOf(expireSeconds));
        return result != null && result == 1L;
    }

    /**
     * 获取锁并自动续期
     * 
     * @param lockKey       锁键
     * @param expireSeconds 锁过期时间(秒)
     * @return 锁上下文对象，包含锁信息和续期任务
     */
    public LockContext lockWithRenewal(String lockKey, int expireSeconds) {
        // 生成唯一的锁值
        String value = UUID.randomUUID().toString();

        // 尝试获取锁
        if (!tryLock(lockKey, value, expireSeconds)) {
            return null;
        }

        // 创建锁上下文
        LockContext lockContext = new LockContext(lockKey, value, expireSeconds);

        // 计算自动续期的间隔时间(毫秒)
        long renewIntervalMillis = (long) (expireSeconds * RENEW_INTERVAL_FACTOR * 1000);

        // 启动自动续期任务
        lockContext.setRenewalTask(SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                // 如果已经取消续期，则停止任务
                if (lockContext.isCancelled()) {
                    lockContext.cancelRenewal();
                    return;
                }

                // 续期锁
                boolean renewed = renewLock(lockKey, value, expireSeconds);

                // 更新续期次数
                if (renewed) {
                    lockContext.incrementRenewCount();
                    log.debug("自动续期锁成功: key={}, 当前续期次数={}", lockKey, lockContext.getRenewCount());
                } else {
                    log.warn("自动续期锁失败: key={}, 可能锁已被释放或过期", lockKey);
                    lockContext.cancelRenewal();
                }
            } catch (Exception e) {
                log.error("自动续期锁异常: key={}, error={}", lockKey, e.getMessage(), e);
            }
        }, renewIntervalMillis, renewIntervalMillis, TimeUnit.MILLISECONDS));

        return lockContext;
    }

    /**
     * 获取锁并自动续期(使用默认过期时间)
     * 
     * @param lockKey 锁键
     * @return 锁上下文对象
     */
    public LockContext lockWithRenewal(String lockKey) {
        return lockWithRenewal(lockKey, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁键
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        String key = getLockKey(lockKey);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 检查锁是否被指定值持有
     *
     * @param lockKey 锁键
     * @param value   锁值
     * @return 是否匹配
     */
    public boolean isLockedByValue(String lockKey, String value) {
        String key = getLockKey(lockKey);
        Object lockValue = redisTemplate.opsForValue().get(key);
        return lockValue != null && lockValue.toString().equals(value);
    }

    /**
     * 获取锁的剩余过期时间(秒)
     *
     * @param lockKey 锁键
     * @return 剩余时间(秒)，如果锁不存在则返回-2
     */
    public long getLockExpire(String lockKey) {
        String key = getLockKey(lockKey);
        Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expireTime != null ? expireTime : -2;
    }

    /**
     * 获取完整的锁键
     *
     * @param lockKey 锁键
     * @return 完整锁键
     */
    private String getLockKey(String lockKey) {
        return LOCK_PREFIX + lockKey;
    }

    /**
     * 锁上下文类，包含锁信息和自动续期任务
     */
    public static class LockContext {
        private final String lockKey;
        private final String value;
        private final int expireSeconds;
        private java.util.concurrent.ScheduledFuture<?> renewalTask;
        private final AtomicInteger renewCount = new AtomicInteger(0);
        private volatile boolean cancelled = false;

        public LockContext(String lockKey, String value, int expireSeconds) {
            this.lockKey = lockKey;
            this.value = value;
            this.expireSeconds = expireSeconds;
        }

        /**
         * 获取锁键
         */
        public String getLockKey() {
            return lockKey;
        }

        /**
         * 获取锁值
         */
        public String getValue() {
            return value;
        }

        /**
         * 获取锁过期时间(秒)
         */
        public int getExpireSeconds() {
            return expireSeconds;
        }

        /**
         * 获取续期次数
         */
        public int getRenewCount() {
            return renewCount.get();
        }

        /**
         * 增加续期次数
         */
        public void incrementRenewCount() {
            renewCount.incrementAndGet();
        }

        /**
         * 设置续期任务
         */
        public void setRenewalTask(java.util.concurrent.ScheduledFuture<?> renewalTask) {
            this.renewalTask = renewalTask;
        }

        /**
         * 取消续期任务
         */
        public void cancelRenewal() {
            if (renewalTask != null && !renewalTask.isCancelled()) {
                renewalTask.cancel(false);
            }
        }

        /**
         * 设置取消标志
         */
        public void cancel() {
            this.cancelled = true;
        }

        /**
         * 是否已取消
         */
        public boolean isCancelled() {
            return cancelled;
        }
    }
}