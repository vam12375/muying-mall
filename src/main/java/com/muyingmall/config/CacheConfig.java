package com.muyingmall.config;

import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.Product;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 缓存配置类
 * 配置缓存预热
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {

    private final ProductService productService;
    private final RedisUtil redisUtil;

    /**
     * 启动时预热商品缓存
     */
    @Bean
    public CommandLineRunner cacheWarmUp() {
        return args -> {
            try {
                log.info("开始预热商品缓存...");

                // 预热热门商品
                warmUpHotProducts();

                // 预热新品商品
                warmUpNewProducts();

                // 预热推荐商品
                warmUpRecommendProducts();

                log.info("商品缓存预热完成！");
            } catch (Exception e) {
                log.error("商品缓存预热失败: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * 预热热门商品缓存
     */
    private void warmUpHotProducts() {
        try {
            // 检查缓存是否已存在
            if (redisUtil.hasKey(CacheConstants.PRODUCT_HOT_KEY)) {
                log.info("热门商品缓存已存在，跳过预热");
                return;
            }

            // 获取热门商品
            List<Product> hotProducts = productService.getHotProducts(20);

            // 缓存热门商品
            if (hotProducts != null && !hotProducts.isEmpty()) {
                redisUtil.set(CacheConstants.PRODUCT_HOT_KEY, hotProducts, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
                log.info("预热热门商品缓存成功，共{}个商品", hotProducts.size());

                // 同时缓存每个热门商品的详情
                for (Product product : hotProducts) {
                    String cacheKey = CacheConstants.PRODUCT_DETAIL_KEY + product.getProductId();
                    redisUtil.set(cacheKey, product, CacheConstants.PRODUCT_EXPIRE_TIME);
                }
            }
        } catch (Exception e) {
            log.error("预热热门商品缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 预热新品商品缓存
     */
    private void warmUpNewProducts() {
        try {
            // 检查缓存是否已存在
            if (redisUtil.hasKey(CacheConstants.PRODUCT_NEW_KEY)) {
                log.info("新品商品缓存已存在，跳过预热");
                return;
            }

            // 获取新品商品
            List<Product> newProducts = productService.getNewProducts(20);

            // 缓存新品商品
            if (newProducts != null && !newProducts.isEmpty()) {
                redisUtil.set(CacheConstants.PRODUCT_NEW_KEY, newProducts, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
                log.info("预热新品商品缓存成功，共{}个商品", newProducts.size());

                // 同时缓存每个新品商品的详情
                for (Product product : newProducts) {
                    String cacheKey = CacheConstants.PRODUCT_DETAIL_KEY + product.getProductId();
                    if (!redisUtil.hasKey(cacheKey)) {
                        redisUtil.set(cacheKey, product, CacheConstants.PRODUCT_EXPIRE_TIME);
                    }
                }
            }
        } catch (Exception e) {
            log.error("预热新品商品缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 预热推荐商品缓存
     */
    private void warmUpRecommendProducts() {
        try {
            // 检查缓存是否已存在
            if (redisUtil.hasKey(CacheConstants.PRODUCT_RECOMMEND_KEY)) {
                log.info("推荐商品缓存已存在，跳过预热");
                return;
            }

            // 获取推荐商品
            List<Product> recommendProducts = productService.getRecommendProducts(20);

            // 缓存推荐商品
            if (recommendProducts != null && !recommendProducts.isEmpty()) {
                redisUtil.set(CacheConstants.PRODUCT_RECOMMEND_KEY, recommendProducts,
                        CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
                log.info("预热推荐商品缓存成功，共{}个商品", recommendProducts.size());

                // 同时缓存每个推荐商品的详情
                for (Product product : recommendProducts) {
                    String cacheKey = CacheConstants.PRODUCT_DETAIL_KEY + product.getProductId();
                    if (!redisUtil.hasKey(cacheKey)) {
                        redisUtil.set(cacheKey, product, CacheConstants.PRODUCT_EXPIRE_TIME);
                    }
                }
            }
        } catch (Exception e) {
            log.error("预热推荐商品缓存失败: {}", e.getMessage(), e);
        }
    }
}