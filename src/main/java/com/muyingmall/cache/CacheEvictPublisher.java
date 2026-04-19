package com.muyingmall.cache;

import com.muyingmall.common.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 本地缓存失效消息发布者
 * 当本节点清除 L1 后，通过 Redis Pub/Sub 通知其他节点同步清除
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NodeIdentifier nodeIdentifier;

    /**
     * 广播"按精确 key 失效"
     */
    public void publishKey(String key) {
        publish(new CacheEvictMessage(
                CacheEvictMessage.EvictType.KEY,
                key,
                nodeIdentifier.getNodeId(),
                System.currentTimeMillis()));
    }

    /**
     * 广播"按前缀失效"
     */
    public void publishPrefix(String prefix) {
        publish(new CacheEvictMessage(
                CacheEvictMessage.EvictType.PREFIX,
                prefix,
                nodeIdentifier.getNodeId(),
                System.currentTimeMillis()));
    }

    /**
     * 广播"全部失效"
     */
    public void publishAll() {
        publish(new CacheEvictMessage(
                CacheEvictMessage.EvictType.ALL,
                null,
                nodeIdentifier.getNodeId(),
                System.currentTimeMillis()));
    }

    private void publish(CacheEvictMessage message) {
        try {
            redisTemplate.convertAndSend(CacheConstants.LOCAL_CACHE_EVICT_CHANNEL, message);
            log.debug("本地缓存失效消息已发布: type={}, target={}", message.getType(), message.getTarget());
        } catch (Exception e) {
            log.error("发布本地缓存失效消息失败: {}", e.getMessage(), e);
        }
    }
}
