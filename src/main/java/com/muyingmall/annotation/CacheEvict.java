package com.muyingmall.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存清除注解
 * 支持清除 Redis (L2)；当 useLocalCache=true 时，同步清除本地 Caffeine (L1) 并通过 Redis Pub/Sub
 * 广播通知其他节点清除本地缓存
 * 来源：性能优化 - Redis + Caffeine 二级缓存增强
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    /**
     * 需要清除的缓存键前缀数组
     */
    String[] keyPrefixes();

    /**
     * 是否清除所有匹配的键
     * 默认 true
     */
    boolean allEntries() default true;

    /**
     * 是否同步清除 Caffeine (L1) 本地缓存并广播失效事件
     * 默认 false（仅清 Redis）
     */
    boolean useLocalCache() default false;
}
