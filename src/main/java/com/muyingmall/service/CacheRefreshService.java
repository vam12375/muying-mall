package com.muyingmall.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 缓存刷新服务
 * 用于在数据更新后立即清理相关缓存，确保数据一致性
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheRefreshService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 立即刷新订单相关缓存
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    public void refreshOrderCache(Integer orderId, Integer userId) {
        try {
            log.debug("开始刷新订单缓存: orderId={}, userId={}", orderId, userId);
            
            // 1. 清理订单详情缓存
            clearOrderDetailCache(orderId);
            
            // 2. 清理用户订单列表缓存
            if (userId != null) {
                clearUserOrderListCache(userId);
            }
            
            // 3. 清理订单统计缓存
            clearOrderStatsCache();
            
            // 4. 清理相关的业务缓存
            clearRelatedBusinessCache(orderId, userId);
            
            log.debug("订单缓存刷新完成: orderId={}, userId={}", orderId, userId);
            
        } catch (Exception e) {
            log.error("刷新订单缓存失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }

    /**
     * 清理订单详情缓存
     */
    private void clearOrderDetailCache(Integer orderId) {
        try {
            // 清理多种可能的缓存key格式
            String[] cacheKeys = {
                "order:detail:" + orderId,
                "order_detail_" + orderId,
                "order:" + orderId,
                "muying:order:detail:" + orderId
            };
            
            for (String key : cacheKeys) {
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.TRUE.equals(deleted)) {
                    log.debug("清理订单详情缓存成功: {}", key);
                }
            }
            
        } catch (Exception e) {
            log.error("清理订单详情缓存失败: orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    /**
     * 清理用户订单列表缓存
     */
    private void clearUserOrderListCache(Integer userId) {
        try {
            // 清理多种可能的缓存key格式
            String[] patterns = {
                "order:user:" + userId + "*",
                "user_order_list_" + userId + "*",
                "muying:order:user:" + userId + "*",
                "order:list:user:" + userId + "*"
            };
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    log.debug("清理用户订单列表缓存: pattern={}, deletedCount={}", pattern, deletedCount);
                }
            }
            
        } catch (Exception e) {
            log.error("清理用户订单列表缓存失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 清理订单统计缓存
     */
    private void clearOrderStatsCache() {
        try {
            String[] patterns = {
                "order:stats:*",
                "order_stats_*",
                "muying:order:stats:*"
            };
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    log.debug("清理订单统计缓存: pattern={}, deletedCount={}", pattern, deletedCount);
                }
            }
            
        } catch (Exception e) {
            log.error("清理订单统计缓存失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 清理相关的业务缓存
     */
    private void clearRelatedBusinessCache(Integer orderId, Integer userId) {
        try {
            // 清理支付相关缓存
            clearPaymentCache(orderId);
            
            // 清理用户相关缓存
            if (userId != null) {
                clearUserRelatedCache(userId);
            }
            
        } catch (Exception e) {
            log.error("清理相关业务缓存失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }

    /**
     * 清理支付相关缓存
     */
    private void clearPaymentCache(Integer orderId) {
        try {
            String[] patterns = {
                "payment:order:" + orderId + "*",
                "payment_order_" + orderId + "*"
            };
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    log.debug("清理支付缓存: pattern={}, deletedCount={}", pattern, deletedCount);
                }
            }
            
        } catch (Exception e) {
            log.error("清理支付缓存失败: orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    /**
     * 清理用户相关缓存
     */
    private void clearUserRelatedCache(Integer userId) {
        try {
            String[] patterns = {
                "user:profile:" + userId,
                "user_profile_" + userId,
                "user:account:" + userId + "*",
                "user_account_" + userId + "*"
            };
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    log.debug("清理用户相关缓存: pattern={}, deletedCount={}", pattern, deletedCount);
                }
            }
            
        } catch (Exception e) {
            log.error("清理用户相关缓存失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 强制刷新指定缓存key
     */
    public void forceRefreshCache(String cacheKey) {
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.debug("强制刷新缓存: key={}, deleted={}", cacheKey, deleted);
        } catch (Exception e) {
            log.error("强制刷新缓存失败: key={}, error={}", cacheKey, e.getMessage(), e);
        }
    }

    /**
     * 批量强制刷新缓存
     */
    public void batchForceRefreshCache(String... cacheKeys) {
        for (String key : cacheKeys) {
            forceRefreshCache(key);
        }
    }
}
