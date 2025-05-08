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
}