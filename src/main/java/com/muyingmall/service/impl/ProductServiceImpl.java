package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.ProductImage;
import com.muyingmall.entity.ProductSpecs;
import com.muyingmall.mapper.CategoryMapper;
import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductImageMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.mapper.ProductSpecsMapper;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.CacheProtectionUtil;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 优化：增加缓存穿透保护，提升高并发场景下的稳定性
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper productImageMapper;
    private final ProductSpecsMapper productSpecsMapper;
    private final ProductSkuMapper productSkuMapper;
    private final RedisUtil redisUtil;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final CacheProtectionUtil cacheProtectionUtil;

    @Override
    public Page<Product> getProductPage(int page, int size, Integer categoryId, Integer brandId, Boolean isHot, Boolean isNew,
            Boolean isRecommend, String keyword) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.PRODUCT_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (categoryId != null) {
            cacheKey.append("_category_").append(categoryId);
        }

        if (brandId != null) {
            cacheKey.append("_brand_").append(brandId);
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

        String finalCacheKey = cacheKey.toString();
        String lockKey = "lock:" + finalCacheKey;

        // 使用缓存保护工具查询，防止缓存击穿
        return cacheProtectionUtil.queryWithMutex(
                finalCacheKey,
                lockKey,
                CacheConstants.MEDIUM_EXPIRE_TIME,
                () -> {
                    // 缓存不存在，查询数据库
                    Page<Product> pageParam = new Page<>(page, size);

                    LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
                    // 只查询上架商品
                    queryWrapper.eq(Product::getProductStatus, "上架");

                    // 条件查询
                    if (categoryId != null) {
                        queryWrapper.eq(Product::getCategoryId, categoryId);
                    }

                    if (brandId != null) {
                        queryWrapper.eq(Product::getBrandId, brandId);
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

                    return page(pageParam, queryWrapper);
                }
        );
    }

    @Override
    public Page<Product> getProductPage(int page, int size, Integer categoryId, Integer brandId,
            String keyword, Integer status) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.PRODUCT_ADMIN_LIST_KEY);
        cacheKey.append("page_").append(page)
                .append("_size_").append(size);

        if (categoryId != null) {
            cacheKey.append("_category_").append(categoryId);
        }

        if (brandId != null) {
            cacheKey.append("_brand_").append(brandId);
        }

        if (StringUtils.hasText(keyword)) {
            cacheKey.append("_keyword_").append(keyword);
        }

        if (status != null) {
            cacheKey.append("_status_").append(status);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            return (Page<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        Page<Product> pageParam = new Page<>(page, size);

        // 转换状态值为字符串
        String statusStr = null;
        if (status != null) {
            statusStr = status == 1 ? "上架" : "下架";
        }

        // 使用新的查询方法，直接传递参数
        Page<Product> result = baseMapper.selectProductPageWithParams(
                pageParam,
                categoryId,
                brandId,
                keyword,
                statusStr);

        // 打印测试日志
        if (result != null && result.getRecords() != null) {
            for (Product product : result.getRecords()) {
                System.out.println("查询结果 - 商品ID: " + product.getProductId()
                        + ", 名称: " + product.getProductName()
                        + ", 分类ID: " + product.getCategoryId()
                        + ", 分类名称: " + product.getCategoryName()
                        + ", 品牌ID: " + product.getBrandId()
                        + ", 品牌名称: " + product.getBrandName());
            }
        }

        // 缓存结果
        redisUtil.set(cacheKey.toString(), result, CacheConstants.SHORT_EXPIRE_TIME);

        return result;
    }

    /**
     * 获取商品详情
     * 委托给带缓存保护的方法，统一缓存策略
     */
    @Override
    @Transactional(readOnly = true)
    public Product getProductDetail(Integer id) {
        return getProductDetailWithProtection(id);
    }

    /**
     * 获取商品详情（带缓存穿透保护）
     * 使用CacheProtectionUtil防止缓存穿透攻击
     * 对不存在的商品ID也会缓存空值标记，避免频繁查询数据库
     */
    @Override
    @Transactional(readOnly = true)
    public Product getProductDetailWithProtection(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        String cacheKey = CacheConstants.PRODUCT_DETAIL_KEY + id;
        String lockKey = "lock:product:detail:" + id;

        // 使用带互斥锁的缓存查询，防止缓存击穿和穿透
        return cacheProtectionUtil.queryWithMutex(
                cacheKey,
                lockKey,
                CacheConstants.PRODUCT_EXPIRE_TIME,
                () -> {
                    // 查询数据库
                    Product product = getById(id);
                    if (product == null) {
                        log.debug("商品不存在: productId={}", id);
                        return null;
                    }

                    // 获取商品图片
                    List<ProductImage> images = productImageMapper.selectList(
                            new LambdaQueryWrapper<ProductImage>()
                                    .eq(ProductImage::getProductId, id)
                                    .orderByAsc(ProductImage::getSortOrder));
                    product.setImages(images);

                    // 获取商品规格
                    List<ProductSpecs> specs = productSpecsMapper.selectList(
                            new LambdaQueryWrapper<ProductSpecs>()
                                    .eq(ProductSpecs::getProductId, id)
                                    .orderByAsc(ProductSpecs::getSortOrder));
                    product.setSpecsList(specs);

                    return product;
                }
        );
    }

    @Override
    @Transactional
    public boolean createProduct(Product product) {
        // 保存商品基本信息
        boolean result = save(product);

        if (result) {
            // 如果有商品图片，保存图片
            List<ProductImage> images = product.getImages();
            if (images != null && !images.isEmpty()) {
                for (ProductImage image : images) {
                    image.setProductId(product.getProductId());
                    productImageMapper.insert(image);
                }
            }

            // 如果有商品规格，保存规格
            List<ProductSpecs> specsList = product.getSpecsList();
            if (specsList != null && !specsList.isEmpty()) {
                for (ProductSpecs specs : specsList) {
                    specs.setProductId(product.getProductId());
                    productSpecsMapper.insert(specs);
                }
            }

            // 清除缓存
            cleanProductCache();
        }

        return result;
    }

    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        Integer productId = product.getProductId();
        
        // 如果启用了SKU，自动计算总库存（SKU库存之和）
        if (product.getHasSku() != null && product.getHasSku() == 1) {
            List<ProductSku> skuList = productSkuMapper.selectByProductId(productId);
            if (skuList != null && !skuList.isEmpty()) {
                // 计算所有启用状态SKU的库存总和
                int totalStock = skuList.stream()
                    .filter(sku -> sku.getStatus() != null && sku.getStatus() == 1)
                    .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                    .sum();
                product.setStock(totalStock);
                log.info("商品 {} 启用SKU，自动计算总库存: {}", productId, totalStock);
            }
        }
        
        // 更新商品基本信息
        boolean result = updateById(product);

        if (result) {

            // 如果有商品图片，先删除原有图片，再保存新图片
            List<ProductImage> images = product.getImages();
            if (images != null) {
                // 删除原有图片
                LambdaQueryWrapper<ProductImage> imageQueryWrapper = new LambdaQueryWrapper<>();
                imageQueryWrapper.eq(ProductImage::getProductId, productId);
                productImageMapper.delete(imageQueryWrapper);

                // 保存新图片
                if (!images.isEmpty()) {
                    for (ProductImage image : images) {
                        image.setProductId(productId);
                        productImageMapper.insert(image);
                    }
                }
            }

            // 如果有商品规格，先删除原有规格，再保存新规格
            List<ProductSpecs> specsList = product.getSpecsList();
            if (specsList != null) {
                // 删除原有规格
                LambdaQueryWrapper<ProductSpecs> specsQueryWrapper = new LambdaQueryWrapper<>();
                specsQueryWrapper.eq(ProductSpecs::getProductId, productId);
                productSpecsMapper.delete(specsQueryWrapper);

                // 保存新规格
                if (!specsList.isEmpty()) {
                    for (ProductSpecs specs : specsList) {
                        specs.setProductId(productId);
                        productSpecsMapper.insert(specs);
                    }
                }
            }

            // 清除缓存
            cleanProductCache(productId);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean deleteById(Integer id) {
        // 删除商品基本信息
        boolean result = removeById(id);

        if (result) {
            // 删除商品图片
            LambdaQueryWrapper<ProductImage> imageQueryWrapper = new LambdaQueryWrapper<>();
            imageQueryWrapper.eq(ProductImage::getProductId, id);
            productImageMapper.delete(imageQueryWrapper);

            // 删除商品规格
            LambdaQueryWrapper<ProductSpecs> specsQueryWrapper = new LambdaQueryWrapper<>();
            specsQueryWrapper.eq(ProductSpecs::getProductId, id);
            productSpecsMapper.delete(specsQueryWrapper);

            // 清除缓存
            cleanProductCache(id);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean updateStatus(Integer id, Integer status) {
        Product product = getById(id);
        if (product == null) {
            return false;
        }

        product.setProductStatus(status == 1 ? "上架" : "下架");
        boolean result = updateById(product);

        if (result) {
            // 清除缓存
            cleanProductCache(id);
        }

        return result;
    }

    /**
     * 更新商品列表缓存中的商品数据
     * 确保列表页和详情页数据一致
     */
    private void updateProductInListCache(Product product) {
        if (product == null) {
            return;
        }

        try {
            // 更新热门商品缓存
            updateProductInSpecificListCache(CacheConstants.PRODUCT_HOT_KEY, product);

            // 更新新品商品缓存
            updateProductInSpecificListCache(CacheConstants.PRODUCT_NEW_KEY, product);

            // 更新推荐商品缓存
            updateProductInSpecificListCache(CacheConstants.PRODUCT_RECOMMEND_KEY, product);

            // 更新分类商品缓存
            if (product.getCategoryId() != null) {
                String categoryCacheKey = CacheConstants.PRODUCT_CATEGORY_KEY + product.getCategoryId();
                updateProductInSpecificListCache(categoryCacheKey, product);
            }

            log.info("更新商品列表缓存成功: productId={}", product.getProductId());
        } catch (Exception e) {
            log.error("更新商品列表缓存失败: productId={}, error={}", product.getProductId(), e.getMessage(), e);
        }
    }

    /**
     * 更新特定列表缓存中的商品数据
     */
    @SuppressWarnings("unchecked")
    private void updateProductInSpecificListCache(String cacheKey, Product product) {
        Object cacheValue = redisUtil.get(cacheKey);
        if (cacheValue != null && cacheValue instanceof List) {
            List<Product> products = (List<Product>) cacheValue;
            for (int i = 0; i < products.size(); i++) {
                Product cachedProduct = products.get(i);
                if (cachedProduct.getProductId().equals(product.getProductId())) {
                    // 更新商品基本信息，保留原有的图片和规格等信息
                    products.set(i, product);
                    redisUtil.set(cacheKey, products, getExpireTimeForKey(cacheKey));
                    log.info("更新缓存列表中的商品: cacheKey={}, productId={}", cacheKey, product.getProductId());
                    break;
                }
            }
        }
    }

    /**
     * 根据缓存键获取相应的过期时间
     */
    private long getExpireTimeForKey(String cacheKey) {
        if (cacheKey.startsWith(CacheConstants.PRODUCT_HOT_KEY)) {
            return CacheConstants.PRODUCT_HOT_EXPIRE_TIME;
        } else if (cacheKey.startsWith(CacheConstants.PRODUCT_NEW_KEY)) {
            return CacheConstants.PRODUCT_HOT_EXPIRE_TIME;
        } else if (cacheKey.startsWith(CacheConstants.PRODUCT_RECOMMEND_KEY)) {
            return CacheConstants.PRODUCT_HOT_EXPIRE_TIME;
        } else {
            return CacheConstants.PRODUCT_EXPIRE_TIME;
        }
    }

    /**
     * 清除商品相关缓存
     */
    private void cleanProductCache() {
        // 清除商品列表缓存
        Set<String> keys = redisUtil.keys(CacheConstants.PRODUCT_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys.toArray(new String[0]));
            log.info("清除所有商品相关缓存，共{}个键", keys.size());
        }
    }

    /**
     * 清除指定商品的缓存
     */
    private void cleanProductCache(Integer productId) {
        if (productId == null) {
            return;
        }

        try {
            // 删除商品详情缓存
            String detailKey = CacheConstants.PRODUCT_DETAIL_KEY + productId;
            redisUtil.del(detailKey);

            // 删除商品参数和规格缓存 - 确保前端可以获取到最新数据
            String detailWithParamsKey = "product:detail_with_params:" + productId;
            redisUtil.del(detailWithParamsKey);

            // 删除可能的规格参数缓存
            String productParamsKey = "product:params:" + productId;
            redisUtil.del(productParamsKey);

            String productSpecsKey = "product:specs:" + productId;
            redisUtil.del(productSpecsKey);

            // 获取所有可能与该商品相关的缓存键
            Set<String> keys = redisUtil.keys(CacheConstants.PRODUCT_LIST_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    // 检查该缓存键对应的缓存中是否包含该商品
                    if (checkProductInCache(key, productId)) {
                        // 删除与该商品相关的缓存
                        redisUtil.del(key);
                    }
                }
            }

            // 获取管理后台商品列表相关的缓存键
            Set<String> adminKeys = redisUtil.keys(CacheConstants.PRODUCT_ADMIN_LIST_KEY + "*");
            if (adminKeys != null && !adminKeys.isEmpty()) {
                for (String key : adminKeys) {
                    // 删除管理后台商品列表缓存
                    redisUtil.del(key);
                }
            }

            // 删除热门商品缓存
            redisUtil.del(CacheConstants.PRODUCT_HOT_KEY);

            // 删除新品商品缓存
            redisUtil.del(CacheConstants.PRODUCT_NEW_KEY);

            // 删除推荐商品缓存
            redisUtil.del(CacheConstants.PRODUCT_RECOMMEND_KEY);

            log.info("已清除商品缓存，商品ID: {}", productId);
        } catch (Exception e) {
            log.error("清除商品缓存失败，商品ID: {}", productId, e);
        }
    }

    /**
     * 检查特定缓存中是否包含指定商品
     */
    @SuppressWarnings("unchecked")
    private boolean checkProductInCache(String cacheKey, Integer productId) {
        try {
            Object cacheValue = redisUtil.get(cacheKey);
            if (cacheValue instanceof List) {
                List<Product> products = (List<Product>) cacheValue;
                return products.stream().anyMatch(p -> p.getProductId().equals(productId));
            }
        } catch (Exception e) {
            log.error("检查缓存中商品失败: key={}, productId={}, error={}",
                    cacheKey, productId, e.getMessage());
        }
        return false;
    }

    /**
     * 获取热门商品列表
     * 优化：使用Sorted Set存储热门商品ID，按销量和评分排序
     * 
     * @param limit 数量限制
     * @return 热门商品列表
     */
    public List<Product> getHotProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_HOT_KEY + "_" + limit;
        String hotProductsRankKey = CacheConstants.PRODUCT_HOT_KEY + "_rank"; // 存储热门商品ID的有序集合

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 检查是否有热门商品排名缓存
        if (redisUtil.hasKey(hotProductsRankKey)) {
            try {
                // 从有序集合中获取排名前limit的商品ID
                Set<Object> topProductIds = redisUtil.zReverseRange(hotProductsRankKey, 0, limit - 1);
                if (topProductIds != null && !topProductIds.isEmpty()) {
                    // 根据ID批量获取商品详情
                    List<Product> hotProducts = new ArrayList<>();
                    for (Object idObj : topProductIds) {
                        Integer productId = Integer.valueOf(idObj.toString());
                        Product product = getProductDetail(productId);
                        if (product != null && "上架".equals(product.getProductStatus())) {
                            hotProducts.add(product);
                        }
                    }

                    // 缓存结果
                    if (!hotProducts.isEmpty()) {
                        redisUtil.set(cacheKey, hotProducts, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);
                        return hotProducts;
                    }
                }
            } catch (Exception e) {
                log.error("从热门商品排名缓存获取商品失败: {}", e.getMessage(), e);
                // 发生异常，继续从数据库获取
            }
        }

        // 缓存不存在或获取失败，查询数据库
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductStatus, "上架")
                .eq(Product::getIsHot, 1)
                .orderByDesc(Product::getSales, Product::getRating)
                .last("LIMIT " + limit);

        List<Product> hotProducts = list(queryWrapper);

        // 缓存结果
        if (hotProducts != null && !hotProducts.isEmpty()) {
            // 缓存完整的热门商品列表
            redisUtil.set(cacheKey, hotProducts, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);

            // 更新热门商品排名缓存
            try {
                // 先清除旧的排名数据
                redisUtil.del(hotProductsRankKey);

                // 添加新的排名数据，使用销量*10 + 评分作为分数
                for (Product product : hotProducts) {
                    double score = product.getSales() * 10
                            + (product.getRating() != null ? product.getRating().doubleValue() : 0);
                    redisUtil.zAdd(hotProductsRankKey, product.getProductId(), score);
                }

                // 设置过期时间
                redisUtil.expire(hotProductsRankKey, CacheConstants.PRODUCT_HOT_EXPIRE_TIME);

                log.info("热门商品排名已更新: count={}", hotProducts.size());
            } catch (Exception e) {
                log.error("更新热门商品排名缓存失败: {}", e.getMessage(), e);
                // 缓存失败不影响正常业务
            }
        }

        return hotProducts;
    }

    /**
     * 获取新品商品列表
     * 优化：使用Sorted Set存储新品商品ID，按创建时间排序
     * 
     * @param limit 数量限制
     * @return 新品商品列表
     */
    public List<Product> getNewProducts(int limit) {
        // 构建缓存键
        String cacheKey = CacheConstants.PRODUCT_NEW_KEY + "_" + limit;
        String newProductsRankKey = CacheConstants.PRODUCT_NEW_KEY + "_rank"; // 存储新品商品ID的有序集合

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 检查是否有新品商品排名缓存
        if (redisUtil.hasKey(newProductsRankKey)) {
            try {
                // 从有序集合中获取排名前limit的商品ID
                Set<Object> topProductIds = redisUtil.zReverseRange(newProductsRankKey, 0, limit - 1);
                if (topProductIds != null && !topProductIds.isEmpty()) {
                    // 根据ID批量获取商品详情
                    List<Product> newProducts = new ArrayList<>();
                    for (Object idObj : topProductIds) {
                        Integer productId = Integer.valueOf(idObj.toString());
                        Product product = getProductDetail(productId);
                        if (product != null && "上架".equals(product.getProductStatus())) {
                            newProducts.add(product);
                        }
                    }

                    // 缓存结果
                    if (!newProducts.isEmpty()) {
                        redisUtil.set(cacheKey, newProducts, CacheConstants.MEDIUM_EXPIRE_TIME);
                        return newProducts;
                    }
                }
            } catch (Exception e) {
                log.error("从新品商品排名缓存获取商品失败: {}", e.getMessage(), e);
                // 发生异常，继续从数据库获取
            }
        }

        // 缓存不存在或获取失败，查询数据库
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductStatus, "上架")
                .eq(Product::getIsNew, 1)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);

        List<Product> newProducts = list(queryWrapper);

        // 缓存结果
        if (newProducts != null && !newProducts.isEmpty()) {
            // 缓存完整的新品商品列表
            redisUtil.set(cacheKey, newProducts, CacheConstants.MEDIUM_EXPIRE_TIME);

            // 更新新品商品排名缓存
            try {
                // 先清除旧的排名数据
                redisUtil.del(newProductsRankKey);

                // 添加新的排名数据，使用时间戳作为分数
                for (Product product : newProducts) {
                    double score = 0;
                    if (product.getCreateTime() != null) {
                        score = product.getCreateTime().toEpochSecond(ZoneOffset.UTC);
                    }
                    redisUtil.zAdd(newProductsRankKey, product.getProductId(), score);
                }

                // 设置过期时间
                redisUtil.expire(newProductsRankKey, CacheConstants.MEDIUM_EXPIRE_TIME);

                log.info("新品商品排名已更新: count={}", newProducts.size());
            } catch (Exception e) {
                log.error("更新新品商品排名缓存失败: {}", e.getMessage(), e);
                // 缓存失败不影响正常业务
            }
        }

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

    @Override
    public List<Product> getRecommendedProducts(Integer productId, Integer categoryId, int limit, String type) {
        // 构建缓存键
        StringBuilder cacheKey = new StringBuilder(CacheConstants.PRODUCT_RECOMMEND_KEY);
        cacheKey.append("_").append(type)
                .append("_limit_").append(limit);

        if (productId != null) {
            cacheKey.append("_pid_").append(productId);
        }

        if (categoryId != null) {
            cacheKey.append("_cid_").append(categoryId);
        }

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey.toString());
        if (cacheResult != null) {
            return (List<Product>) cacheResult;
        }

        // 缓存不存在，查询数据库
        List<Product> allProducts;
        try {
            allProducts = list(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductStatus, "上架"));
        } catch (Exception e) {
            log.error("查询上架商品列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }

        if (allProducts == null || allProducts.isEmpty()) {
            // 缓存空结果，避免频繁查询
            redisUtil.set(cacheKey.toString(), Collections.emptyList(), CacheConstants.SHORT_EXPIRE_TIME);
            return Collections.emptyList();
        }

        final List<Product> result = new ArrayList<>();

        // 根据不同类型进行推荐
        if ("shop".equals(type)) {
            // 本店推荐 - 优先同类别、同品牌商品
            if (productId != null) {
                // 获取当前商品，用于提取品牌ID
                Product currentProduct = getById(productId);
                if (currentProduct != null && currentProduct.getBrandId() != null) {
                    // 找出同品牌的商品
                    List<Product> sameBrandProducts = allProducts.stream()
                            .filter(p -> Objects.equals(p.getBrandId(), currentProduct.getBrandId()))
                            .filter(p -> !Objects.equals(p.getProductId(), productId)) // 排除当前商品
                            .collect(Collectors.toList());

                    if (!sameBrandProducts.isEmpty()) {
                        // 随机打乱同品牌商品
                        Collections.shuffle(sameBrandProducts, new Random(System.currentTimeMillis()));
                        // 取指定数量的商品
                        List<Product> selectedProducts = sameBrandProducts.size() <= limit ? sameBrandProducts
                                : sameBrandProducts.subList(0, limit);
                        result.addAll(selectedProducts);
                    }
                }
            }
        } else if ("view".equals(type)) {
            // 猜你喜欢 - 优先同类别商品，其次是热门商品
            if (categoryId != null) {
                // 找出同类别的商品
                List<Product> sameCategoryProducts = allProducts.stream()
                        .filter(p -> Objects.equals(p.getCategoryId(), categoryId))
                        .filter(p -> productId == null || !Objects.equals(p.getProductId(), productId)) // 排除当前商品
                        .collect(Collectors.toList());

                if (!sameCategoryProducts.isEmpty()) {
                    // 取出热门商品（根据销量排序）
                    sameCategoryProducts.sort((p1, p2) -> {
                        int sales1 = p1.getSales() != null ? p1.getSales() : 0;
                        int sales2 = p2.getSales() != null ? p2.getSales() : 0;
                        return sales2 - sales1; // 降序排序
                    });

                    // 随机打乱前20个热门商品
                    List<Product> topProducts = sameCategoryProducts.size() <= 20 ? sameCategoryProducts
                            : sameCategoryProducts.subList(0, 20);
                    Collections.shuffle(topProducts, new Random(System.currentTimeMillis()));

                    // 取指定数量的商品
                    List<Product> selectedProducts = topProducts.size() <= limit ? topProducts
                            : topProducts.subList(0, limit);
                    result.addAll(selectedProducts);
                }
            }
        }

        // 如果通过上面的逻辑没有获取到足够的推荐商品，则随机选择
        if (result.size() < limit) {
            // 排除已推荐的商品和当前商品
            final List<Product> finalResult = result; // 创建一个final引用以便在lambda中使用
            List<Product> remainingProducts = allProducts.stream()
                    .filter(p -> !finalResult.contains(p))
                    .filter(p -> productId == null || !Objects.equals(p.getProductId(), productId))
                    .collect(Collectors.toList());

            if (!remainingProducts.isEmpty()) {
                // 随机打乱
                Collections.shuffle(remainingProducts, new Random(System.currentTimeMillis()));

                // 添加剩余所需数量的商品
                int needed = limit - result.size();
                List<Product> additionalProducts = remainingProducts.size() <= needed ? remainingProducts
                        : remainingProducts.subList(0, needed);

                result.addAll(additionalProducts);
            }
        }

        // 缓存结果（即使为空也缓存，避免缓存穿透）
        try {
            redisUtil.set(cacheKey.toString(), result, CacheConstants.SHORT_EXPIRE_TIME);
        } catch (Exception e) {
            log.warn("缓存推荐商品结果失败: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public int getProductCountByBrandId(Integer brandId) {
        if (brandId == null) {
            return 0;
        }
        return productMapper.countProductByBrandId(brandId);
    }

    @Override
    public List<Map<String, Object>> getCategorySales() {
        // 查询所有商品分类
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 从分类表获取所有分类
            List<Category> categories = categoryMapper.selectList(null);

            // 统计每个分类的商品数量和销量
            for (Category category : categories) {
                Map<String, Object> categoryData = new HashMap<>();

                // 查询该分类下的商品数量
                QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("category_id", category.getCategoryId());
                long productCount = this.count(queryWrapper);

                // 如果没有商品，则跳过
                if (productCount == 0) {
                    continue;
                }

                categoryData.put("name", category.getName());
                categoryData.put("value", productCount);

                result.add(categoryData);
            }

            // 如果没有数据，添加一些模拟数据
            if (result.isEmpty()) {
                String[] mockCategories = { "婴儿奶粉", "尿不湿", "婴儿服装", "婴儿玩具", "婴儿洗护", "其他" };
                int[] mockValues = { 32, 24, 16, 12, 8, 8 };

                for (int i = 0; i < mockCategories.length; i++) {
                    Map<String, Object> mockCategory = new HashMap<>();
                    mockCategory.put("name", mockCategories[i]);
                    mockCategory.put("value", mockValues[i]);
                    result.add(mockCategory);
                }
            }
        } catch (Exception e) {
            log.error("获取商品分类销售数据失败", e);

            // 返回模拟数据
            String[] mockCategories = { "婴儿奶粉", "尿不湿", "婴儿服装", "婴儿玩具", "婴儿洗护", "其他" };
            int[] mockValues = { 32, 24, 16, 12, 8, 8 };

            for (int i = 0; i < mockCategories.length; i++) {
                Map<String, Object> mockCategory = new HashMap<>();
                mockCategory.put("name", mockCategories[i]);
                mockCategory.put("value", mockValues[i]);
                result.add(mockCategory);
            }
        }

        return result;
    }
}