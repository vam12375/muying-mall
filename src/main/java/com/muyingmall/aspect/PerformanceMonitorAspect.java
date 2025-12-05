package com.muyingmall.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * 监控关键接口的响应时间
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {

    // 监控登录接口
    @Pointcut("execution(* com.muyingmall.controller.user.UserController.login(..))")
    public void loginPointcut() {}

    // 监控库存扣减
    @Pointcut("execution(* com.muyingmall.service.impl.ProductSkuServiceImpl.deductStock(..))")
    public void deductStockPointcut() {}

    @Around("loginPointcut() || deductStockPointcut()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 慢请求告警（超过1秒）
            if (duration > 1000) {
                log.warn("慢请求告警: method={}, duration={}ms", methodName, duration);
            } else {
                log.debug("性能监控: method={}, duration={}ms", methodName, duration);
            }
            
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("请求异常: method={}, duration={}ms, error={}", 
                      methodName, duration, e.getMessage());
            throw e;
        }
    }
}
