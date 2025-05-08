package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.entity.Brand;
import com.muyingmall.mapper.BrandMapper;
import com.muyingmall.service.BrandService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 品牌服务实现类
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BRAND_CACHE_KEY = "brand:";
    private static final String BRAND_LIST_KEY = BRAND_CACHE_KEY + "list:";
    private static final String BRAND_DETAIL_KEY = BRAND_CACHE_KEY + "detail:";
    private static final long BRAND_EXPIRE_TIME = 7200; // 2小时

    @Override
    public Page<Brand> getBrandPage(int page, int size, String keyword) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(BRAND_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (StringUtils.hasText(keyword)) {
            cacheKey.append("_keyword_").append(keyword);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            return (Page<Brand>) cacheResult;
        }

        // 缓存不存在，查询数据库
        Page<Brand> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Brand::getName, keyword)
                    .or()
                    .like(Brand::getDescription, keyword);
        }

        // 默认按排序值和创建时间排序
        queryWrapper.orderByAsc(Brand::getSortOrder)
                .orderByDesc(Brand::getCreateTime);

        Page<Brand> result = page(pageParam, queryWrapper);

        // 缓存结果
        redisUtil.set(cacheKey.toString(), result, BRAND_EXPIRE_TIME);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Brand getBrandDetail(Integer id) {
        // 构建缓存键
        String cacheKey = BRAND_DETAIL_KEY + id;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (Brand) cacheResult;
        }

        // 缓存不存在，查询数据库
        Brand brand = getById(id);

        if (brand != null) {
            // 缓存结果
            redisUtil.set(cacheKey, brand, BRAND_EXPIRE_TIME);
        }

        return brand;
    }

    @Override
    @Transactional
    public boolean createBrand(Brand brand) {
        boolean result = save(brand);
        if (result) {
            // 清除列表缓存
            cleanListCache();
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateBrand(Brand brand) {
        boolean result = updateById(brand);
        if (result) {
            // 清除详情缓存
            redisUtil.del(BRAND_DETAIL_KEY + brand.getBrandId());
            // 清除列表缓存
            cleanListCache();
        }
        return result;
    }

    @Override
    @Transactional
    public boolean deleteBrand(Integer id) {
        boolean result = removeById(id);
        if (result) {
            // 清除详情缓存
            redisUtil.del(BRAND_DETAIL_KEY + id);
            // 清除列表缓存
            cleanListCache();
        }
        return result;
    }

    /**
     * 清除列表缓存
     */
    private void cleanListCache() {
        // 获取所有匹配的键
        Set<String> keys = redisTemplate.keys(BRAND_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}