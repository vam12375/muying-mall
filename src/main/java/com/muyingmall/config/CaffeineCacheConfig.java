package com.muyingmall.config;

import com.muyingmall.cache.CacheEvictListener;
import com.muyingmall.common.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Caffeine + Redis 二级缓存配置
 * 注册 Redis Pub/Sub 监听容器，订阅本地缓存失效 channel
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CaffeineCacheConfig {

    /**
     * 注册 Redis 消息监听容器，订阅本地缓存失效 channel
     */
    @Bean
    public RedisMessageListenerContainer cacheEvictListenerContainer(
            RedisConnectionFactory connectionFactory,
            CacheEvictListener cacheEvictListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(cacheEvictListener,
                new ChannelTopic(CacheConstants.LOCAL_CACHE_EVICT_CHANNEL));
        log.info("Redis Pub/Sub 本地缓存失效订阅已注册 - channel={}",
                CacheConstants.LOCAL_CACHE_EVICT_CHANNEL);
        return container;
    }
}
