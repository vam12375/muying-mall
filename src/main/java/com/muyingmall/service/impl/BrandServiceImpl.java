package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
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

        // 验证参数
        if (page < 1) {
            log.warn("页码参数不合法，已重置为1: {}", page);
            page = 1;
        }
        if (size < 1 || size > 100) {
            log.warn("每页大小参数不合法，已重置为默认值10: {}", size);
            size = 10;
        }

        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(BRAND_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (StringUtils.hasText(keyword)) {
            cacheKey.append("_keyword_").append(keyword);
        }

        Page<Brand> result = null;
        boolean cacheError = false;

        try {
            // 查询缓存
            Object cacheResult = redisUtil.get(cacheKey.toString());
            if (cacheResult != null) {
                log.debug("从缓存中获取品牌分页数据: {}", cacheKey);
                try {
                    result = (Page<Brand>) cacheResult;
                    return result;
                } catch (ClassCastException e) {
                    log.error("缓存数据类型转换异常: {}", e.getMessage());
                    cacheError = true;
                    // 继续执行，从数据库查询
                }
            }
        } catch (Exception e) {
            log.error("从缓存获取品牌分页数据异常: {}", e.getMessage());
            cacheError = true;
            // 继续执行，从数据库查询
        }

        // 缓存不存在或发生错误，查询数据库
        log.debug("从数据库查询品牌分页数据");
        Page<Brand> pageParam = new Page<>(page, size);

        try {
            LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();

            // 条件查询
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(Brand::getName, keyword)
                        .or()
                        .like(Brand::getDescription, keyword);
            }

            // 暂时不过滤status，显示所有品牌
            // queryWrapper.eq(Brand::getStatus, 1);

            // 默认按排序值和创建时间排序
            queryWrapper.orderByAsc(Brand::getSortOrder)
                    .orderByDesc(Brand::getCreateTime);

            result = page(pageParam, queryWrapper);
            log.debug("从数据库查询品牌分页数据成功: 总记录数={}, 当前页记录数={}",
                    result.getTotal(), result.getRecords().size());

            // 如果之前缓存出错，先清除可能损坏的缓存
            if (cacheError) {
                try {
                    redisUtil.del(cacheKey.toString());
                    log.debug("清除可能损坏的缓存: {}", cacheKey);
                } catch (Exception ex) {
                    log.warn("清除缓存异常，忽略: {}", ex.getMessage());
                }
            }

            // 缓存结果（即使之前有错误，也尝试再次缓存新结果）
            try {
                redisUtil.set(cacheKey.toString(), result, BRAND_EXPIRE_TIME);
                log.debug("将品牌分页数据缓存到Redis: {}", cacheKey);
            } catch (Exception e) {
                log.error("缓存品牌分页数据异常: {}", e.getMessage());
                // 缓存失败不影响正常返回结果
            }
        } catch (Exception e) {
            log.error("查询品牌分页数据异常: {}", e.getMessage());
            // 发生异常时仍然尽量返回一个空页面，而不是抛出异常
            if (result == null) {
                result = new Page<>(page, size);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Brand getBrandDetail(Integer id) {
        if (id == null || id <= 0) {
            log.warn("无效的品牌ID: {}", id);
            return null;
        }

        log.debug("获取品牌详情: id={}", id);

        Brand brand = null;
        boolean cacheError = false;

        try {
            // 构建缓存键
            String cacheKey = BRAND_DETAIL_KEY + id;

            // 查询缓存
            Object cacheResult = redisUtil.get(cacheKey);
            if (cacheResult != null) {
                log.debug("从缓存中获取品牌详情: id={}", id);
                try {
                    brand = (Brand) cacheResult;
                    return brand;
                } catch (ClassCastException e) {
                    log.error("缓存数据类型转换异常: {}", e.getMessage());
                    cacheError = true;
                    // 继续执行，从数据库查询
                }
            }
        } catch (Exception e) {
            log.error("从缓存获取品牌详情异常: {}", e.getMessage());
            cacheError = true;
            // 继续执行，从数据库查询
        }

        try {
            // 缓存不存在或出错，查询数据库
            log.debug("从数据库查询品牌详情: id={}", id);
            brand = getById(id);

            if (brand != null) {
                // 检查状态是否正常（status为null或1都认为是正常）
                if (brand.getStatus() != null && brand.getStatus() != 1) {
                    log.debug("品牌状态不正常: id={}, status={}", id, brand.getStatus());
                    return null;
                }

                // 如果之前缓存出错，先清除可能损坏的缓存
                String cacheKey = BRAND_DETAIL_KEY + id;
                if (cacheError) {
                    try {
                        redisUtil.del(cacheKey);
                        log.debug("清除可能损坏的缓存: {}", cacheKey);
                    } catch (Exception ex) {
                        log.warn("清除缓存异常，忽略: {}", ex.getMessage());
                    }
                }

                // 缓存结果
                try {
                    redisUtil.set(cacheKey, brand, BRAND_EXPIRE_TIME);
                    log.debug("将品牌详情缓存到Redis: id={}", id);
                } catch (Exception e) {
                    log.error("缓存品牌详情异常: {}", e.getMessage());
                    // 缓存失败不影响正常返回结果
                }
            } else {
                log.debug("品牌不存在: id={}", id);
            }
        } catch (Exception e) {
            log.error("查询品牌详情异常: id={}, error={}", id, e.getMessage());
            // 继续返回null
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