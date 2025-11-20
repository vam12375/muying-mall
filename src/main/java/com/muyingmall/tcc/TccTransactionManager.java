package com.muyingmall.tcc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * TCC事务管理器
 * 负责协调TCC事务的Try、Confirm、Cancel三个阶段
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TccTransactionManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLock distributedLock;
    private final ObjectMapper objectMapper;

    /**
     * TCC事务键前缀
     */
    private static final String TCC_TRANSACTION_KEY_PREFIX = "tcc:transaction:";

    /**
     * TCC事务锁键前缀
     */
    private static final String TCC_LOCK_KEY_PREFIX = "tcc:lock:";

    /**
     * 默认事务超时时间（30秒）
     */
    private static final long DEFAULT_TRANSACTION_TIMEOUT = 30000;

    /**
     * 锁超时时间（10秒）
     */
    private static final long LOCK_EXPIRE_TIME = 10000;

    /**
     * 开始一个TCC事务
     *
     * @param transactionType 事务类型
     * @param businessKey     业务标识
     * @param params          业务参数
     * @param <T>             参数类型
     * @return 事务ID
     */
    public <T> String begin(String transactionType, String businessKey, T params) {
        return begin(transactionType, businessKey, params, DEFAULT_TRANSACTION_TIMEOUT);
    }

    /**
     * 开始一个TCC事务
     *
     * @param transactionType 事务类型
     * @param businessKey     业务标识
     * @param params          业务参数
     * @param timeout         事务超时时间
     * @param <T>             参数类型
     * @return 事务ID
     */
    public <T> String begin(String transactionType, String businessKey, T params, long timeout) {
        // 创建事务ID
        String transactionId = UUID.randomUUID().toString();

        // 创建事务对象
        TccTransaction transaction = new TccTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setTransactionType(transactionType);
        transaction.setBusinessKey(businessKey);
        transaction.setStatus(TccTransaction.TccTransactionStatus.TRYING);
        transaction.setRetryCount(0);
        transaction.setMaxRetryCount(3);
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setLastUpdateTime(LocalDateTime.now());
        transaction.setTimeout(timeout);

        try {
            // 序列化业务参数
            transaction.setSerializedParams(objectMapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            log.error("序列化业务参数失败", e);
            throw new RuntimeException("序列化业务参数失败", e);
        }

        // 保存事务
        saveTransaction(transaction);

        return transactionId;
    }

    /**
     * 执行Try阶段
     *
     * @param transactionId 事务ID
     * @param action        TCC操作
     * @param params        业务参数
     * @param <T>           参数类型
     * @param <R>           结果类型
     * @return 业务结果
     */
    public <T, R> R tryAction(String transactionId, TccAction<T, R> action, T params) {
        // 获取事务
        TccTransaction transaction = getTransaction(transactionId);
        if (transaction == null) {
            throw new RuntimeException("事务不存在：" + transactionId);
        }

        // 检查事务状态
        if (transaction.getStatus() != TccTransaction.TccTransactionStatus.TRYING) {
            throw new RuntimeException("事务状态错误：" + transaction.getStatus());
        }

        // 获取事务锁
        String lockKey = getTccLockKey(transactionId);
        String requestId = UUID.randomUUID().toString();

        try {
            // 尝试获取锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new RuntimeException("获取事务锁失败：" + transactionId);
            }

            // 执行Try阶段

            return action.tryAction(params);
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    /**
     * 确认事务（Confirm阶段）
     *
     * @param transactionId 事务ID
     * @param action        TCC操作
     * @param params        业务参数
     * @param <T>           参数类型
     */
    public <T> void confirmAction(String transactionId, TccAction<T, ?> action, T params) {
        // 获取事务
        TccTransaction transaction = getTransaction(transactionId);
        if (transaction == null) {
            log.warn("确认事务失败，事务不存在：{}", transactionId);
            return;
        }

        // 检查事务状态
        if (transaction.getStatus() == TccTransaction.TccTransactionStatus.CONFIRMED) {
            log.info("事务已确认，忽略重复确认：{}", transactionId);
            return;
        }

        if (transaction.getStatus() == TccTransaction.TccTransactionStatus.CANCELLED) {
            log.warn("事务已取消，无法确认：{}", transactionId);
            return;
        }

        // 获取事务锁
        String lockKey = getTccLockKey(transactionId);
        String requestId = UUID.randomUUID().toString();

        try {
            // 尝试获取锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new RuntimeException("获取事务锁失败：" + transactionId);
            }

            // 更新事务状态
            transaction.setStatus(TccTransaction.TccTransactionStatus.CONFIRMING);
            transaction.setLastUpdateTime(LocalDateTime.now());
            saveTransaction(transaction);

            // 执行Confirm阶段
            try {
                action.confirmAction(params);

                // 更新事务状态为已确认
                transaction.setStatus(TccTransaction.TccTransactionStatus.CONFIRMED);
                transaction.setLastUpdateTime(LocalDateTime.now());
                saveTransaction(transaction);

                log.info("事务确认成功：{}", transactionId);
            } catch (Exception e) {
                // 增加重试次数
                transaction.setRetryCount(transaction.getRetryCount() + 1);
                transaction.setLastUpdateTime(LocalDateTime.now());
                saveTransaction(transaction);

                log.error("事务确认失败：{}，重试次数：{}/{}", transactionId,
                        transaction.getRetryCount(), transaction.getMaxRetryCount(), e);

                // 超过最大重试次数，抛出异常
                if (transaction.getRetryCount() >= transaction.getMaxRetryCount()) {
                    throw new RuntimeException("事务确认失败，超过最大重试次数", e);
                }

                // 否则抛出异常，等待重试
                throw e;
            }
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    /**
     * 取消事务（Cancel阶段）
     *
     * @param transactionId 事务ID
     * @param action        TCC操作
     * @param params        业务参数
     * @param <T>           参数类型
     */
    public <T> void cancelAction(String transactionId, TccAction<T, ?> action, T params) {
        // 获取事务
        TccTransaction transaction = getTransaction(transactionId);
        if (transaction == null) {
            log.warn("取消事务失败，事务不存在：{}", transactionId);
            return;
        }

        // 检查事务状态
        if (transaction.getStatus() == TccTransaction.TccTransactionStatus.CANCELLED) {
            log.info("事务已取消，忽略重复取消：{}", transactionId);
            return;
        }

        if (transaction.getStatus() == TccTransaction.TccTransactionStatus.CONFIRMED) {
            log.warn("事务已确认，无法取消：{}", transactionId);
            return;
        }

        // 获取事务锁
        String lockKey = getTccLockKey(transactionId);
        String requestId = UUID.randomUUID().toString();

        try {
            // 尝试获取锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new RuntimeException("获取事务锁失败：" + transactionId);
            }

            // 更新事务状态
            transaction.setStatus(TccTransaction.TccTransactionStatus.CANCELLING);
            transaction.setLastUpdateTime(LocalDateTime.now());
            saveTransaction(transaction);

            // 执行Cancel阶段
            try {
                action.cancelAction(params);

                // 更新事务状态为已取消
                transaction.setStatus(TccTransaction.TccTransactionStatus.CANCELLED);
                transaction.setLastUpdateTime(LocalDateTime.now());
                saveTransaction(transaction);

                log.info("事务取消成功：{}", transactionId);
            } catch (Exception e) {
                // 增加重试次数
                transaction.setRetryCount(transaction.getRetryCount() + 1);
                transaction.setLastUpdateTime(LocalDateTime.now());
                saveTransaction(transaction);

                log.error("事务取消失败：{}，重试次数：{}/{}", transactionId,
                        transaction.getRetryCount(), transaction.getMaxRetryCount(), e);

                // 超过最大重试次数，抛出异常
                if (transaction.getRetryCount() >= transaction.getMaxRetryCount()) {
                    throw new RuntimeException("事务取消失败，超过最大重试次数", e);
                }

                // 否则抛出异常，等待重试
                throw e;
            }
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    /**
     * 删除事务
     *
     * @param transactionId 事务ID
     * @return 是否删除成功
     */
    public boolean deleteTransaction(String transactionId) {
        String key = getTccTransactionKey(transactionId);
        return redisTemplate.delete(key);
    }

    /**
     * 获取事务
     *
     * @param transactionId 事务ID
     * @return 事务对象
     */
    public TccTransaction getTransaction(String transactionId) {
        String key = getTccTransactionKey(transactionId);
        return (TccTransaction) redisTemplate.opsForValue().get(key);
    }

    /**
     * 保存事务
     *
     * @param transaction 事务对象
     */
    private void saveTransaction(TccTransaction transaction) {
        String key = getTccTransactionKey(transaction.getTransactionId());
        redisTemplate.opsForValue().set(key, transaction, transaction.getTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * 获取事务键
     *
     * @param transactionId 事务ID
     * @return 事务键
     */
    private String getTccTransactionKey(String transactionId) {
        return TCC_TRANSACTION_KEY_PREFIX + transactionId;
    }

    /**
     * 获取事务锁键
     *
     * @param transactionId 事务ID
     * @return 事务锁键
     */
    private String getTccLockKey(String transactionId) {
        return TCC_LOCK_KEY_PREFIX + transactionId;
    }
}