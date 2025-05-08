package com.muyingmall.lock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁实现
 */
@Component
public class RedisDistributedLock implements DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Long> unlockScript;

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        // 初始化获取锁脚本
        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/lock.lua")));
        lockScript.setResultType(Long.class);

        // 初始化释放锁脚本
        unlockScript = new DefaultRedisScript<>();
        unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/unlock.lua")));
        unlockScript.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        // 使用Lua脚本实现原子性操作
        Long result = redisTemplate.execute(
                lockScript,
                Collections.singletonList(lockKey),
                requestId,
                expireTime);
        return result != null && result == 1L;
    }

    @Override
    public boolean releaseLock(String lockKey, String requestId) {
        // 使用Lua脚本确保原子性释放
        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(lockKey),
                requestId);
        return result != null && result == 1L;
    }

    @Override
    public boolean tryLockWithTimeout(String lockKey, String requestId, long expireTime, long timeout) {
        long startTime = System.currentTimeMillis();
        long sleepTime = 100; // 重试间隔，单位毫秒

        while (System.currentTimeMillis() - startTime < timeout) {
            if (tryLock(lockKey, requestId, expireTime)) {
                return true;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }
}