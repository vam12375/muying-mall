package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CategoryMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.service.CategoryService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "category:";
    private static final String CATEGORY_LIST_KEY = CATEGORY_CACHE_KEY + "list";
    private static final String CATEGORY_TREE_KEY = CATEGORY_CACHE_KEY + "tree";
    private static final String CATEGORY_DETAIL_KEY = CATEGORY_CACHE_KEY + "detail:";
    private static final long CATEGORY_EXPIRE_TIME = 7200; // 2小时

    @Override
    public List<Category> listWithTree() {
        log.debug("获取分类树形结构");

        // 尝试从缓存获取
        try {
            Object cacheResult = redisUtil.get(CATEGORY_TREE_KEY);
            if (cacheResult != null) {
                log.debug("从缓存获取分类树形结构");
                try {
                    return (List<Category>) cacheResult;
                } catch (ClassCastException e) {
                    log.error("缓存数据类型转换异常: {}", e.getMessage());
                    // 缓存数据类型错误，继续执行，从数据库查询
                }
            }
        } catch (Exception e) {
            log.error("从缓存获取分类树形结构异常: {}", e.getMessage());
            // 缓存异常，继续执行，从数据库查询
        }

        try {
            // 1. 查询所有分类
            List<Category> categories = this.list();
            if (categories == null) {
                log.warn("未查询到任何分类数据");
                return Collections.emptyList();
            }

            // 2. 组装成父子的树形结构
            // 2.1 找到所有的一级分类
            List<Category> levelOneCategories = categories.stream()
                    .filter(category -> category.getParentId() == 0)
                    .map(category -> {
                        // 2.2 找到一级分类的子分类
                        category.setChildren(getChildren(category, categories));
                        return category;
                    })
                    .sorted((c1, c2) -> {
                        // 2.3 排序
                        return (c1.getSortOrder() == null ? 0 : c1.getSortOrder()) -
                                (c2.getSortOrder() == null ? 0 : c2.getSortOrder());
                    })
                    .collect(Collectors.toList());

            // 缓存结果
            try {
                redisUtil.set(CATEGORY_TREE_KEY, levelOneCategories, CATEGORY_EXPIRE_TIME);
                log.debug("将分类树形结构缓存到Redis");
            } catch (Exception e) {
                log.error("缓存分类树形结构异常: {}", e.getMessage());
                // 缓存失败不影响正常返回结果
            }

            return levelOneCategories;
        } catch (Exception e) {
            log.error("获取分类树形结构异常: {}", e.getMessage(), e);
            // 发生异常时返回空列表
            return Collections.emptyList();
        }
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<Category> getChildren(Category root, List<Category> all) {
        if (root == null || all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Category> children = all.stream()
                    .filter(category -> category.getParentId() != null
                            && category.getParentId().equals(root.getCategoryId()))
                    .map(category -> {
                        // 递归找子菜单
                        category.setChildren(getChildren(category, all));
                        return category;
                    })
                    .sorted((c1, c2) -> {
                        // 排序
                        return (c1.getSortOrder() == null ? 0 : c1.getSortOrder()) -
                                (c2.getSortOrder() == null ? 0 : c2.getSortOrder());
                    })
                    .collect(Collectors.toList());

            return children;
        } catch (Exception e) {
            log.error("获取子分类异常: parentId={}, error={}", root.getCategoryId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasChildren(Integer id) {
        if (id == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getParentId, id);
            return this.count(queryWrapper) > 0;
        } catch (Exception e) {
            log.error("检查分类是否有子分类异常: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasProducts(Integer id) {
        if (id == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getCategoryId, id);
            return productMapper.selectCount(queryWrapper) > 0;
        } catch (Exception e) {
            log.error("检查分类是否有商品异常: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || status == null) {
            log.warn("更新分类状态参数无效: id={}, status={}", id, status);
            return false;
        }

        try {
            Category category = this.getById(id);
            if (category == null) {
                log.warn("分类不存在: id={}", id);
                return false;
            }

            category.setStatus(status);
            boolean result = this.updateById(category);

            if (result) {
                // 清除缓存
                cleanCache(id);
            }

            return result;
        } catch (Exception e) {
            log.error("更新分类状态异常: id={}, status={}, error={}", id, status, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Category> list() {
        log.debug("获取所有分类列表");

        // 尝试从缓存获取
        try {
            Object cacheResult = redisUtil.get(CATEGORY_LIST_KEY);
            if (cacheResult != null) {
                log.debug("从缓存获取分类列表");
                try {
                    return (List<Category>) cacheResult;
                } catch (ClassCastException e) {
                    log.error("缓存数据类型转换异常: {}", e.getMessage());
                    // 缓存数据类型错误，继续执行，从数据库查询
                }
            }
        } catch (Exception e) {
            log.error("从缓存获取分类列表异常: {}", e.getMessage());
            // 缓存异常，继续执行，从数据库查询
        }

        try {
            // 从数据库查询
            List<Category> categories = super.list();

            // 缓存结果
            try {
                redisUtil.set(CATEGORY_LIST_KEY, categories, CATEGORY_EXPIRE_TIME);
                log.debug("将分类列表缓存到Redis");
            } catch (Exception e) {
                log.error("缓存分类列表异常: {}", e.getMessage());
                // 缓存失败不影响正常返回结果
            }

            return categories;
        } catch (Exception e) {
            log.error("获取分类列表异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Category getById(java.io.Serializable id) {
        if (id == null) {
            return null;
        }

        if (id instanceof Integer) {
            return getById((Integer) id);
        }

        log.error("不支持的ID类型：{}", id.getClass());
        return null;
    }

    /**
     * 根据ID获取分类详情
     * 
     * @param id 分类ID
     * @return 分类详情
     */
    public Category getById(Integer id) {
        if (id == null) {
            return null;
        }

        log.debug("获取分类详情: id={}", id);

        // 尝试从缓存获取
        String cacheKey = CATEGORY_DETAIL_KEY + id;
        try {
            Object cacheResult = redisUtil.get(cacheKey);
            if (cacheResult != null) {
                log.debug("从缓存获取分类详情: id={}", id);
                try {
                    return (Category) cacheResult;
                } catch (ClassCastException e) {
                    log.error("缓存数据类型转换异常: {}", e.getMessage());
                    // 缓存数据类型错误，继续执行，从数据库查询
                }
            }
        } catch (Exception e) {
            log.error("从缓存获取分类详情异常: {}", e.getMessage());
            // 缓存异常，继续执行，从数据库查询
        }

        try {
            // 从数据库查询
            Category category = super.getById(id);
            if (category != null) {
                // 缓存结果
                try {
                    redisUtil.set(cacheKey, category, CATEGORY_EXPIRE_TIME);
                    log.debug("将分类详情缓存到Redis: id={}", id);
                } catch (Exception e) {
                    log.error("缓存分类详情异常: {}", e.getMessage());
                    // 缓存失败不影响正常返回结果
                }
            }

            return category;
        } catch (Exception e) {
            log.error("获取分类详情异常: id={}, error={}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean save(Category entity) {
        if (entity == null) {
            return false;
        }

        try {
            boolean result = super.save(entity);

            if (result) {
                // 清除缓存
                cleanAllCache();
            }

            return result;
        } catch (Exception e) {
            log.error("保存分类异常: {}, error={}", entity, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateById(Category entity) {
        if (entity == null || entity.getCategoryId() == null) {
            return false;
        }

        try {
            boolean result = super.updateById(entity);

            if (result) {
                // 清除缓存
                cleanCache(entity.getCategoryId());
            }

            return result;
        } catch (Exception e) {
            log.error("更新分类异常: id={}, error={}", entity.getCategoryId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        if (id == null) {
            return false;
        }

        if (id instanceof Integer) {
            return removeById((Integer) id);
        }

        log.error("不支持的ID类型：{}", id.getClass());
        return false;
    }

    /**
     * 根据ID删除分类
     * 
     * @param id 分类ID
     * @return 是否成功
     */
    public boolean removeById(Integer id) {
        if (id == null) {
            return false;
        }

        try {
            boolean result = super.removeById(id);

            if (result) {
                // 清除缓存
                cleanCache(id);
            }

            return result;
        } catch (Exception e) {
            log.error("删除分类异常: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Category> listWithTreeAndCount() {
        log.debug("获取带商品数量的分类树形结构");

        try {
            // 1. 查询所有分类
            List<Category> categories = this.list();
            if (categories == null) {
                log.warn("未查询到任何分类数据");
                return Collections.emptyList();
            }

            // 2. 组装成父子的树形结构，并添加商品数量
            // 2.1 找到所有的一级分类
            List<Category> levelOneCategories = categories.stream()
                    .filter(category -> category.getParentId() == 0)
                    .map(category -> {
                        // 2.2 找到一级分类的子分类
                        category.setChildren(getChildrenWithCount(category, categories));

                        // 2.3 设置商品数量（包括子分类的商品）
                        try {
                            int productCount = getProductCount(category.getCategoryId());
                            // 递归计算子分类的商品数量
                            productCount += calculateChildrenProductCount(category);

                            // 使用TableField(exist = false)注解的字段，需要手动设置
                            category.getClass().getDeclaredFields();
                            try {
                                java.lang.reflect.Field field = Category.class.getDeclaredField("productCount");
                                field.setAccessible(true);
                                field.set(category, productCount);
                            } catch (Exception e) {
                                log.warn("设置productCount字段异常: {}", e.getMessage());
                                // 忽略异常
                            }
                        } catch (Exception e) {
                            log.error("获取分类商品数量异常: id={}, error={}", category.getCategoryId(), e.getMessage());
                        }

                        return category;
                    })
                    .sorted((c1, c2) -> {
                        // 2.4 排序
                        return (c1.getSortOrder() == null ? 0 : c1.getSortOrder()) -
                                (c2.getSortOrder() == null ? 0 : c2.getSortOrder());
                    })
                    .collect(Collectors.toList());

            return levelOneCategories;
        } catch (Exception e) {
            log.error("获取带商品数量的分类树形结构异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 递归查找所有菜单的子菜单，并添加商品数量
     */
    private List<Category> getChildrenWithCount(Category root, List<Category> all) {
        if (root == null || all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Category> children = all.stream()
                    .filter(category -> category.getParentId() != null
                            && category.getParentId().equals(root.getCategoryId()))
                    .map(category -> {
                        // 递归找子菜单
                        category.setChildren(getChildrenWithCount(category, all));

                        // 设置商品数量（包括子分类的商品）
                        try {
                            int productCount = getProductCount(category.getCategoryId());
                            // 递归计算子分类的商品数量
                            productCount += calculateChildrenProductCount(category);

                            // 使用TableField(exist = false)注解的字段，需要手动设置
                            try {
                                java.lang.reflect.Field field = Category.class.getDeclaredField("productCount");
                                field.setAccessible(true);
                                field.set(category, productCount);
                            } catch (Exception e) {
                                log.warn("设置productCount字段异常: {}", e.getMessage());
                                // 忽略异常
                            }
                        } catch (Exception e) {
                            log.error("获取分类商品数量异常: id={}, error={}", category.getCategoryId(), e.getMessage());
                        }

                        return category;
                    })
                    .sorted((c1, c2) -> {
                        // 排序
                        return (c1.getSortOrder() == null ? 0 : c1.getSortOrder()) -
                                (c2.getSortOrder() == null ? 0 : c2.getSortOrder());
                    })
                    .collect(Collectors.toList());

            return children;
        } catch (Exception e) {
            log.error("获取子分类异常: parentId={}, error={}", root.getCategoryId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 递归计算子分类的商品数量
     */
    private int calculateChildrenProductCount(Category category) {
        int count = 0;

        try {
            List<Category> children = category.getChildren();
            if (children != null && !children.isEmpty()) {
                for (Category child : children) {
                    // 当前子分类的商品数量
                    count += getProductCount(child.getCategoryId());
                    // 递归计算子分类的子分类的商品数量
                    count += calculateChildrenProductCount(child);
                }
            }
        } catch (Exception e) {
            log.error("计算子分类商品数量异常: categoryId={}, error={}",
                    category != null ? category.getCategoryId() : null, e.getMessage(), e);
        }

        return count;
    }

    @Override
    public int getProductCount(Integer categoryId) {
        if (categoryId == null) {
            return 0;
        }

        try {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getCategoryId, categoryId);
            Long count = productMapper.selectCount(queryWrapper);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("获取分类商品数量异常: categoryId={}, error={}", categoryId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 清除分类相关的所有缓存
     */
    private void cleanAllCache() {
        log.debug("清除分类相关的所有缓存");

        try {
            // 清除分类列表缓存
            redisUtil.del(CATEGORY_LIST_KEY);

            // 清除分类树形结构缓存
            redisUtil.del(CATEGORY_TREE_KEY);

            // 清除所有分类详情缓存
            Set<String> keys = redisTemplate.keys(CATEGORY_DETAIL_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.debug("清除分类相关的所有缓存成功");
        } catch (Exception e) {
            log.error("清除分类相关的所有缓存异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 清除指定分类的缓存
     * 
     * @param id 分类ID
     */
    private void cleanCache(Integer id) {
        log.debug("清除分类缓存: id={}", id);

        try {
            // 清除分类列表缓存
            redisUtil.del(CATEGORY_LIST_KEY);

            // 清除分类树形结构缓存
            redisUtil.del(CATEGORY_TREE_KEY);

            // 清除分类详情缓存
            redisUtil.del(CATEGORY_DETAIL_KEY + id);

            log.debug("清除分类缓存成功: id={}", id);
        } catch (Exception e) {
            log.error("清除分类缓存异常: id={}, error={}", id, e.getMessage(), e);
        }
    }
}