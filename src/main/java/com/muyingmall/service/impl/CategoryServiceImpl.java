package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CategoryMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public List<Category> listWithTree() {
        // 1. 查询所有分类
        List<Category> categories = this.list();

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

        return levelOneCategories;
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<Category> getChildren(Category root, List<Category> all) {
        List<Category> children = all.stream()
                .filter(category -> category.getParentId().equals(root.getCategoryId()))
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
    }

    @Override
    public boolean hasChildren(Integer id) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, id);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean hasProducts(Integer id) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, id);
        return productMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        Category category = this.getById(id);
        if (category == null) {
            return false;
        }
        category.setStatus(status);
        return this.updateById(category);
    }

    @Override
    public List<Category> listWithTreeAndCount() {
        // 1. 查询所有分类
        List<Category> categories = this.list();

        // 2. 组装成父子的树形结构，并添加商品数量
        // 2.1 找到所有的一级分类
        List<Category> levelOneCategories = categories.stream()
                .filter(category -> category.getParentId() == 0)
                .map(category -> {
                    // 2.2 找到一级分类的子分类
                    category.setChildren(getChildrenWithCount(category, categories));

                    // 2.3 设置商品数量（包括子分类的商品）
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
                        // 忽略异常
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
    }

    /**
     * 递归查找所有菜单的子菜单，并添加商品数量
     */
    private List<Category> getChildrenWithCount(Category root, List<Category> all) {
        List<Category> children = all.stream()
                .filter(category -> category.getParentId().equals(root.getCategoryId()))
                .map(category -> {
                    // 递归找子菜单
                    category.setChildren(getChildrenWithCount(category, all));

                    // 设置商品数量（包括子分类的商品）
                    int productCount = getProductCount(category.getCategoryId());
                    // 递归计算子分类的商品数量
                    productCount += calculateChildrenProductCount(category);

                    // 使用TableField(exist = false)注解的字段，需要手动设置
                    try {
                        java.lang.reflect.Field field = Category.class.getDeclaredField("productCount");
                        field.setAccessible(true);
                        field.set(category, productCount);
                    } catch (Exception e) {
                        // 忽略异常
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
    }

    /**
     * 递归计算子分类的商品数量
     */
    private int calculateChildrenProductCount(Category category) {
        int count = 0;
        List<Category> children = category.getChildren();
        if (children != null && !children.isEmpty()) {
            for (Category child : children) {
                // 当前子分类的商品数量
                count += getProductCount(child.getCategoryId());
                // 递归计算子分类的子分类的商品数量
                count += calculateChildrenProductCount(child);
            }
        }
        return count;
    }

    @Override
    public int getProductCount(Integer categoryId) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, categoryId);
        return productMapper.selectCount(queryWrapper).intValue();
    }
}