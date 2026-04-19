package com.muyingmall.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存注解
 * 支持 Redis (L2) 单层缓存 或 Caffeine (L1) + Redis (L2) 二级缓存
 * 来源：性能优化 - Redis + Caffeine 二级缓存增强
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    /**
     * 缓存键前缀
     */
    String keyPrefix();

    /**
     * Redis (L2) 缓存过期时间（秒）
     * 默认 1 小时
     */
    long expireTime() default 3600;

    /**
     * 是否使用参数作为缓存键的一部分
     * 默认 true
     */
    boolean useParams() default true;

    /**
     * 是否启用 Caffeine (L1) 本地缓存
     * 默认 false（仅使用 Redis）
     * 启用后：L1 先查 → L1 未命中查 L2 → 回填 L1；
     * 配合 @CacheEvict 的 useLocalCache + Redis Pub/Sub 广播保证多节点 L1 失效一致性
     */
    boolean useLocalCache() default false;

    /**
     * Caffeine (L1) 本地缓存过期时间（秒）
     * 默认 60 秒。L1 TTL 应显著小于 L2 TTL，控制脏数据窗口
     */
    long localExpireSeconds() default 60;
}
