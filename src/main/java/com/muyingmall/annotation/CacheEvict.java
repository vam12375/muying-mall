package com.muyingmall.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存清除注解
 * 用于标记需要清除缓存的方法
 * 
 * Source: 性能优化 - Redis缓存增强
 * 
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
     * 默认true
     */
    boolean allEntries() default true;
}
