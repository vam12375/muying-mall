package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.Product;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品缓存服务 - 优化商品查询性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final RedisUtil redisUtil;

    // 商品详情缓存时间：5分钟
    private static final long PRODUCT_DETAIL_EXPIRE = 300L;
    
    // 商品列表缓存时间：2分钟
    private static final long PRODUCT_LIST_EXPIRE = 120L;

    /**
     * 缓存商品详情
     */
    public void cacheProductDetail(Integer productId, Product product) {
        String key = CacheConstants.PRODUCT_DETAIL_KEY + productId;
        redisUtil.set(key, product, PRODUCT_DETAIL_EXPIRE);
        log.debug("缓存商品详情: productId={}", productId);
    }

    /**
     * 获取缓存的商品详情
     */
    public Product getProductDetail(Integer productId) {
        String key = CacheConstants.PRODUCT_DETAIL_KEY + productId;
        Object cached = redisUtil.get(key);
        if (cached instanceof Product) {
            log.debug("命中商品详情缓存: productId={}", productId);
            return (Product) cached;
        }
        return null;
    }

    /**
     * 清除商品详情缓存
     */
    public void evictProductDetail(Integer productId) {
        String key = CacheConstants.PRODUCT_DETAIL_KEY + productId;
        redisUtil.del(key);
        log.debug("清除商品详情缓存: productId={}", productId);
    }

    /**
     * 批量清除商品详情缓存
     */
    public void evictProductDetails(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        
        String[] keys = productIds.stream()
            .map(id -> CacheConstants.PRODUCT_DETAIL_KEY + id)
            .toArray(String[]::new);
        
        redisUtil.del(keys);
        log.debug("批量清除商品详情缓存: count={}", productIds.size());
    }
}
