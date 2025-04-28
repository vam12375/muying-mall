package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.ProductImage;
import com.muyingmall.entity.ProductSpecs;
import com.muyingmall.mapper.ProductImageMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.ProductSpecsMapper;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper productImageMapper;
    private final ProductSpecsMapper productSpecsMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<Product> getProductPage(int page, int size, Integer categoryId, Boolean isHot, Boolean isNew,
            Boolean isRecommend, String keyword) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.PRODUCT_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (categoryId != null) {
            cacheKey.append("_category_").append(categoryId);
        }

        if (isHot != null && isHot) {
            cacheKey.append("_hot_").append(1);
        }

        if (isNew != null && isNew) {
            cacheKey.append("_new_").append(1);
        }

        if (isRecommend != null && isRecommend) {
            cacheKey.append("_recommend_").append(1);
        }

        if (StringUtils.hasText(keyword)) {
            cacheKey.append("_keyword_").append(keyword);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            return (Page<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        Page<Product> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架商品
        queryWrapper.eq(Product::getProductStatus, "上架");

        // 条件查询
        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        if (isHot != null && isHot) {
            queryWrapper.eq(Product::getIsHot, 1);
        }

        if (isNew != null && isNew) {
            queryWrapper.eq(Product::getIsNew, 1);
        }

        if (isRecommend != null && isRecommend) {
            queryWrapper.eq(Product::getIsRecommend, 1);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getProductDetail, keyword));
        }

        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(Product::getCreateTime);

        Page<Product> result = page(pageParam, queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey.toString(), result, CacheConstants.MEDIUM_EXPIRE_TIME);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductDetail(Integer id) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_DETAIL_KEY + id;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (Product) cacheResult;
        }

        // 缓存不存在，查询数据库
        // 获取商品基本信息
        Product product = getById(id);

        if (product != null) {
            // 获取商品图片
            LambdaQueryWrapper<ProductImage> imageQueryWrapper = new LambdaQueryWrapper<>();
            imageQueryWrapper.eq(ProductImage::getProductId, id);
            imageQueryWrapper.orderByAsc(ProductImage::getSortOrder);
            List<ProductImage> images = productImageMapper.selectList(imageQueryWrapper);
            product.setImages(images);

            // 获取商品规格
            LambdaQueryWrapper<ProductSpecs> specsQueryWrapper = new LambdaQueryWrapper<>();
            specsQueryWrapper.eq(ProductSpecs::getProductId, id);
            List<ProductSpecs> specsList = productSpecsMapper.selectList(specsQueryWrapper);
            product.setSpecsList(specsList);

            // 缓存结果
            redisUtil.set(cacheKey, product, CacheConstants.PRODUCT_EXPIRE_TIME);
        }

        return product;
    }

    /**
     * 获取热门商品列表
     * 
     * @param limit 数量限制
     * @return 热门商品列表
     */
    public List<Product> getHotProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_HOT_KEY + "_" + limit;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductStatus, "上架")
                .eq(Product::getIsHot, 1)
                .orderByDesc(Product::getSales, Product::getCreateTime)
                .last("LIMIT " + limit);

        List<Product> hotProducts = list(queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey, hotProducts, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);

        return hotProducts;
    }

    /**
     * 获取新品商品列表
     * 
     * @param limit 数量限制
     * @return 新品商品列表
     */
    public List<Product> getNewProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_NEW_KEY + "_" + limit;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductStatus, "上架")
                .eq(Product::getIsNew, 1)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);

        List<Product> newProducts = list(queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey, newProducts, CacheConstants.MEDIUM_EXPIRE_TIME);

        return newProducts;
    }

    /**
     * 获取推荐商品列表
     * 
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    public List<Product> getRecommendProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_RECOMMEND_KEY + "_" + limit;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductStatus, "上架")
                .eq(Product::getIsRecommend, 1)
                .orderByDesc(Product::getRating, Product::getSales)
                .last("LIMIT " + limit);

        List<Product> recommendProducts = list(queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey, recommendProducts, CacheConstants.MEDIUM_EXPIRE_TIME);

        return recommendProducts;
    }
}