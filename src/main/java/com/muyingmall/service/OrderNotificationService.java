package com.muyingmall.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 订单通知服务
 * 用于在订单状态变更时通知前端
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderNotificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 注入虚拟线程执行器，用于异步延迟任务
    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    /**
     * 通知订单状态变更
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param reason 变更原因
     */
    public void notifyOrderStatusChange(Integer orderId, Integer userId, String oldStatus, String newStatus, String reason) {
        try {
            log.debug("发送订单状态变更通知: orderId={}, userId={}, oldStatus={}, newStatus={}, reason={}", 
                    orderId, userId, oldStatus, newStatus, reason);
            
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ORDER_STATUS_CHANGE");
            notification.put("orderId", orderId);
            notification.put("userId", userId);
            notification.put("oldStatus", oldStatus);
            notification.put("newStatus", newStatus);
            notification.put("reason", reason);
            notification.put("timestamp", System.currentTimeMillis());
            
            // 发布到Redis频道，供前端WebSocket监听
            String channel = "order_notifications:" + userId;
            redisTemplate.convertAndSend(channel, notification);
            
            // 同时发布到全局频道
            redisTemplate.convertAndSend("order_notifications:all", notification);
            
            log.debug("订单状态变更通知发送成功: channel={}", channel);
            
        } catch (Exception e) {
            log.error("发送订单状态变更通知失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }

    /**
     * 通知支付成功
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param paymentMethod 支付方式
     * @param amount 支付金额
     */
    public void notifyPaymentSuccess(Integer orderId, Integer userId, String paymentMethod, Double amount) {
        try {
            log.debug("发送支付成功通知: orderId={}, userId={}, paymentMethod={}, amount={}", 
                    orderId, userId, paymentMethod, amount);
            
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "PAYMENT_SUCCESS");
            notification.put("orderId", orderId);
            notification.put("userId", userId);
            notification.put("paymentMethod", paymentMethod);
            notification.put("amount", amount);
            notification.put("timestamp", System.currentTimeMillis());
            
            // 发布到Redis频道
            String channel = "payment_notifications:" + userId;
            redisTemplate.convertAndSend(channel, notification);
            
            // 同时发布到全局频道
            redisTemplate.convertAndSend("payment_notifications:all", notification);
            
            log.debug("支付成功通知发送成功: channel={}", channel);
            
        } catch (Exception e) {
            log.error("发送支付成功通知失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }

    /**
     * 通知缓存刷新
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param cacheType 缓存类型
     */
    public void notifyCacheRefresh(Integer orderId, Integer userId, String cacheType) {
        try {
            log.debug("发送缓存刷新通知: orderId={}, userId={}, cacheType={}", 
                    orderId, userId, cacheType);
            
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "CACHE_REFRESH");
            notification.put("orderId", orderId);
            notification.put("userId", userId);
            notification.put("cacheType", cacheType);
            notification.put("timestamp", System.currentTimeMillis());
            
            // 发布到Redis频道
            String channel = "cache_notifications:" + userId;
            redisTemplate.convertAndSend(channel, notification);
            
            log.debug("缓存刷新通知发送成功: channel={}", channel);
            
        } catch (Exception e) {
            log.error("发送缓存刷新通知失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }

    /**
     * 发送实时状态同步通知
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    public void notifyRealTimeSync(Integer orderId, Integer userId) {
        try {
            log.debug("发送实时状态同步通知: orderId={}, userId={}", orderId, userId);
            
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REAL_TIME_SYNC");
            notification.put("orderId", orderId);
            notification.put("userId", userId);
            notification.put("action", "FORCE_REFRESH");
            notification.put("timestamp", System.currentTimeMillis());
            
            // 发布到Redis频道
            String channel = "sync_notifications:" + userId;
            redisTemplate.convertAndSend(channel, notification);
            
            // 延迟发送第二次通知，确保数据同步（使用虚拟线程执行器）
            taskExecutor.execute(() -> {
                try {
                    // 虚拟线程友好的睡眠方式（自动让出CPU）
                    TimeUnit.SECONDS.sleep(2); // 延迟2秒
                    
                    Map<String, Object> delayedNotification = new HashMap<>();
                    delayedNotification.put("type", "DELAYED_SYNC");
                    delayedNotification.put("orderId", orderId);
                    delayedNotification.put("userId", userId);
                    delayedNotification.put("action", "FINAL_REFRESH");
                    delayedNotification.put("timestamp", System.currentTimeMillis());
                    
                    redisTemplate.convertAndSend(channel, delayedNotification);
                    log.debug("延迟同步通知发送成功: orderId={}", orderId);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("延迟同步通知被中断: orderId={}", orderId);
                } catch (Exception e) {
                    log.error("发送延迟同步通知失败: orderId={}, error={}", orderId, e.getMessage());
                }
            }).start();
            
            log.debug("实时状态同步通知发送成功: channel={}", channel);
            
        } catch (Exception e) {
            log.error("发送实时状态同步通知失败: orderId={}, userId={}, error={}", 
                    orderId, userId, e.getMessage(), e);
        }
    }
}
