package com.muyingmall.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 本地缓存失效消息订阅者
 * 收到其他节点的 evict 广播后，同步清除本节点的 Caffeine (L1) 缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictListener implements MessageListener {

    private final LocalCache localCache;
    private final NodeIdentifier nodeIdentifier;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            CacheEvictMessage evict = objectMapper.readValue(body, CacheEvictMessage.class);

            // 忽略自己发出的消息（本地已处理过）
            if (nodeIdentifier.getNodeId().equals(evict.getSourceNodeId())) {
                return;
            }

            switch (evict.getType()) {
                case KEY -> {
                    localCache.evict(evict.getTarget());
                    log.debug("收到广播 - 本地缓存清除 key: {}", evict.getTarget());
                }
                case PREFIX -> {
                    localCache.evictByPrefix(evict.getTarget());
                    log.debug("收到广播 - 本地缓存按前缀清除: {}", evict.getTarget());
                }
                case ALL -> {
                    localCache.evictAll();
                    log.debug("收到广播 - 本地缓存全部清空");
                }
            }
        } catch (Exception e) {
            log.error("处理本地缓存失效广播失败: {}", e.getMessage(), e);
        }
    }
}
