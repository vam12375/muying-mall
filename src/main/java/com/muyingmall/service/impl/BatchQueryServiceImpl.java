package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.annotation.Cacheable;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.entity.Brand;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.BrandMapper;
import com.muyingmall.mapper.CategoryMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.BatchQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 批量查询服务实现
 * 
 * Source: 性能优化 - 批量查询实现
 * 遵循协议: AURA-X-KYS (KISS/YAGNI/SOLID)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchQueryServiceImpl implements BatchQueryService {
    
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    
    @Override
    @Cacheable(keyPrefix = CacheConstants.USER_KEY_PREFIX + "batch:", expireTime = 1800)
    public Map<Integer, User> batchGetUsers(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.debug("批量查询用户，数量: {}", userIds.size());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getUserId, userIds);
        
        List<User> users = userMapper.selectList(wrapper);
        
        return users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));
    }
    
    @Override
    @Cacheable(keyPrefix = CacheConstants.PRODUCT_KEY_PREFIX + "batch:", expireTime = 1800)
    public Map<Integer, Product> batchGetProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.debug("批量查询商品，数量: {}", productIds.size());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getProductId, productIds);
        
        List<Product> products = productMapper.selectList(wrapper);
        
        return products.stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));
    }
    
    @Override
    @Cacheable(keyPrefix = CacheConstants.BRAND_KEY_PREFIX + "batch:", expireTime = 3600)
    public Map<Integer, Brand> batchGetBrands(List<Integer> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.debug("批量查询品牌，数量: {}", brandIds.size());
        
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Brand::getBrandId, brandIds);
        
        List<Brand> brands = brandMapper.selectList(wrapper);
        
        return brands.stream()
                .collect(Collectors.toMap(Brand::getBrandId, brand -> brand));
    }
    
    @Override
    @Cacheable(keyPrefix = CacheConstants.CATEGORY_KEY_PREFIX + "batch:", expireTime = 3600)
    public Map<Integer, Category> batchGetCategories(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.debug("批量查询分类，数量: {}", categoryIds.size());
        
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Category::getCategoryId, categoryIds);
        
        List<Category> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, category -> category));
    }
}
