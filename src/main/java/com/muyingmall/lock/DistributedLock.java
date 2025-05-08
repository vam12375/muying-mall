package com.muyingmall.lock;

/**
 * 分布式锁接口
 */
public interface DistributedLock {

    /**
     * 尝试获取锁
     *
     * @param lockKey    锁键
     * @param requestId  请求标识（用于识别锁的持有者）
     * @param expireTime 锁过期时间（毫秒）
     * @return 是否成功获取锁
     */
    boolean tryLock(String lockKey, String requestId, long expireTime);

    /**
     * 释放锁
     *
     * @param lockKey   锁键
     * @param requestId 请求标识（用于验证锁的持有者）
     * @return 是否成功释放锁
     */
    boolean releaseLock(String lockKey, String requestId);

    /**
     * 带超时的尝试获取锁
     *
     * @param lockKey    锁键
     * @param requestId  请求标识
     * @param expireTime 锁过期时间（毫秒）
     * @param timeout    获取锁的超时时间（毫秒）
     * @return 是否成功获取锁
     */
    boolean tryLockWithTimeout(String lockKey, String requestId, long expireTime, long timeout);
}