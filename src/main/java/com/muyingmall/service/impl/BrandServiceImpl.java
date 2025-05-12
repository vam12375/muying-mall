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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BRAND_CACHE_KEY = "brand:";
    private static final String BRAND_LIST_KEY = BRAND_CACHE_KEY + "list:";
    private static final String BRAND_DETAIL_KEY = BRAND_CACHE_KEY + "detail:";
    private static final long BRAND_EXPIRE_TIME = 7200; // 2小时

    @Override
    public Page<Brand> getBrandPage(int page, int size, String keyword) {
        log.debug("获取品牌分页数据: page={}, size={}, keyword={}", page, size, keyword);

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
            log.debug("从缓存中获取品牌分页数据: {}", cacheKey);
            return (Page<Brand>) cacheResult;
        }

        // 缓存不存在，查询数据库
        log.debug("缓存未命中，从数据库查询品牌分页数据");
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
        log.debug("从数据库查询品牌分页数据成功: 总记录数={}, 当前页记录数={}", result.getTotal(), result.getRecords().size());

        // 缓存结果
        redisUtil.set(cacheKey.toString(), result, BRAND_EXPIRE_TIME);
        log.debug("将品牌分页数据缓存到Redis: {}", cacheKey);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Brand getBrandDetail(Integer id) {
        log.debug("获取品牌详情: id={}", id);

        // 构建缓存键
        String cacheKey = BRAND_DETAIL_KEY + id;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取品牌详情: id={}", id);
            return (Brand) cacheResult;
        }

        // 缓存不存在，查询数据库
        log.debug("缓存未命中，从数据库查询品牌详情: id={}", id);
        Brand brand = getById(id);

        if (brand != null) {
            // 缓存结果
            redisUtil.set(cacheKey, brand, BRAND_EXPIRE_TIME);
            log.debug("将品牌详情缓存到Redis: id={}", id);
        } else {
            log.debug("品牌不存在: id={}", id);
        }

        return brand;
    }

    @Override
    @Transactional
    public boolean createBrand(Brand brand) {
        log.debug("创建品牌: {}", brand);
        boolean result = save(brand);
        if (result) {
            log.debug("创建品牌成功: id={}, name={}", brand.getBrandId(), brand.getName());
            // 清除列表缓存
            cleanListCache();
        } else {
            log.debug("创建品牌失败");
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateBrand(Brand brand) {
        log.debug("更新品牌: id={}, brand={}", brand.getBrandId(), brand);
        boolean result = updateById(brand);
        if (result) {
            log.debug("更新品牌成功: id={}", brand.getBrandId());
            // 清除详情缓存
            redisUtil.del(BRAND_DETAIL_KEY + brand.getBrandId());
            // 清除列表缓存
            cleanListCache();
        } else {
            log.debug("更新品牌失败: id={}", brand.getBrandId());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean deleteBrand(Integer id) {
        log.debug("删除品牌: id={}", id);
        boolean result = removeById(id);
        if (result) {
            log.debug("删除品牌成功: id={}", id);
            // 清除详情缓存
            redisUtil.del(BRAND_DETAIL_KEY + id);
            // 清除列表缓存
            cleanListCache();
        } else {
            log.debug("删除品牌失败: id={}", id);
        }
        return result;
    }

    /**
     * 清除列表缓存
     */
    private void cleanListCache() {
        log.debug("清除品牌列表缓存");
        // 获取所有匹配的键
        Set<String> keys = redisTemplate.keys(BRAND_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("清除品牌列表缓存成功: 清除键数量={}", keys.size());
        } else {
            log.debug("没有找到需要清除的品牌列表缓存");
        }
    }
}