package com.muyingmall.annotation;

import java.lang.annotation.*;

/**
 * 管理员操作日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminOperationLog {

    /**
     * 操作名称
     */
    String operation() default "";

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 操作目标类型
     */
    String targetType() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean recordResult() default false;
}
