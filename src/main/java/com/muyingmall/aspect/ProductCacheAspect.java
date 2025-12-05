package com.muyingmall.aspect;

import com.muyingmall.service.impl.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 商品缓存切面 - 自动清除缓存
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ProductCacheAspect {

    private final ProductCacheService productCacheService;

    // 监控商品更新操作
    @Pointcut("execution(* com.muyingmall.service.impl.ProductServiceImpl.updateById(..)) || " +
              "execution(* com.muyingmall.service.impl.ProductServiceImpl.saveOrUpdate(..))")
    public void productUpdatePointcut() {}

    // 监控商品删除操作
    @Pointcut("execution(* com.muyingmall.service.impl.ProductServiceImpl.removeById(..))")
    public void productDeletePointcut() {}

    @AfterReturning("productUpdatePointcut() || productDeletePointcut()")
    public void evictProductCache(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            try {
                // 尝试从第一个参数获取productId
                if (args[0] instanceof Integer) {
                    Integer productId = (Integer) args[0];
                    productCacheService.evictProductDetail(productId);
                    log.info("自动清除商品缓存: productId={}", productId);
                } else if (args[0] instanceof com.muyingmall.entity.Product) {
                    com.muyingmall.entity.Product product = 
                        (com.muyingmall.entity.Product) args[0];
                    if (product.getProductId() != null) {
                        productCacheService.evictProductDetail(product.getProductId());
                        log.info("自动清除商品缓存: productId={}", product.getProductId());
                    }
                }
            } catch (Exception e) {
                log.error("清除商品缓存失败", e);
            }
        }
    }
}
