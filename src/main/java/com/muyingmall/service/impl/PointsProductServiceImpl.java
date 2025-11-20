package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.mapper.PointsProductMapper;
import com.muyingmall.service.PointsProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 积分商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointsProductServiceImpl extends ServiceImpl<PointsProductMapper, PointsProduct>
        implements PointsProductService {

    private final RedisUtil redisUtil;

    @Override
    public Page<PointsProduct> getPointsProductPage(int page, int size, String category) {
        // 调用重载方法
        return getPointsProductPage(page, size, category, null);
    }

    /**
     * 分页查询积分商品（带热门标记的扩展方法）
     */
    public Page<PointsProduct> getPointsProductPage(int page, int size, String category, Boolean isHot) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.POINTS_PRODUCT_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (StringUtils.hasText(category)) {
            cacheKey.append("_category_").append(category);
        }

        if (isHot != null && isHot) {
            cacheKey.append("_hot_").append(1);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            return (Page<PointsProduct>) cacheResult;
        }

        // 缓存不存在，查询数据库
        Page<PointsProduct> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsProduct> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架商品
        queryWrapper.eq(PointsProduct::getStatus, 1);

        // 条件查询
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(PointsProduct::getCategory, category);
        }

        if (isHot != null && isHot) {
            queryWrapper.eq(PointsProduct::getIsHot, 1);
        }

        // 按排序号降序，创建时间降序排序
        queryWrapper.orderByDesc(PointsProduct::getSortOrder)
                .orderByDesc(PointsProduct::getCreateTime);

        Page<PointsProduct> result = page(pageParam, queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey.toString(), result, CacheConstants.MEDIUM_EXPIRE_TIME);

        return result;
    }

    @Override
    public PointsProduct getPointsProductDetail(Long id) {
        if (id == null) {
            return null;
        }

        // 构建缓存键
        String cacheKey = CacheConstants.POINTS_PRODUCT_DETAIL_KEY + id;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (PointsProduct) cacheResult;
        }

        // 缓存不存在，查询数据库
        PointsProduct product = getById(id);

        // 缓存结果
        if (product != null) {
            redisUtil.set(cacheKey, product, CacheConstants.PRODUCT_EXPIRE_TIME);
        }

        return product;
    }

    @Override
    public List<PointsProduct> getRecommendProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.POINTS_PRODUCT_RECOMMEND_KEY + "_" + limit;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<PointsProduct>) cacheResult;
        }

        // 缓存不存在，查询数据库
        LambdaQueryWrapper<PointsProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsProduct::getStatus, 1) // 上架状态
                .eq(PointsProduct::getIsHot, 1) // 热门商品
                .orderByDesc(PointsProduct::getSortOrder)
                .orderByDesc(PointsProduct::getCreateTime)
                .last("LIMIT " + limit); // 限制返回数量

        List<PointsProduct> result = list(queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey, result, CacheConstants.MEDIUM_EXPIRE_TIME);

        return result;
    }

    @Override
    public Page<PointsProduct> adminListPointsProducts(Integer page, Integer size, String name, String category) {
        Page<PointsProduct> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsProduct> queryWrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(PointsProduct::getName, name);
        }

        if (StringUtils.hasText(category)) {
            queryWrapper.eq(PointsProduct::getCategory, category);
        }

        // 按排序号降序，创建时间降序排序
        queryWrapper.orderByDesc(PointsProduct::getSortOrder)
                .orderByDesc(PointsProduct::getCreateTime);

        return page(pageParam, queryWrapper);
    }
}