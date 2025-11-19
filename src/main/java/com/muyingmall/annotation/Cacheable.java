package com.muyingmall.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存注解
 * 用于标记需要缓存的方法
 * 
 * Source: 性能优化 - Redis缓存增强
 * 遵循协议: AURA-X-KYS (KISS/YAGNI/SOLID)
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
     * 缓存过期时间（秒）
     * 默认1小时
     */
    long expireTime() default 3600;
    
    /**
     * 是否使用参数作为缓存键的一部分
     * 默认true
     */
    boolean useParams() default true;
}
