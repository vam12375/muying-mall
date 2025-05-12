package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树形结构
     *
     * @return 树形结构的分类列表
     */
    List<Category> listWithTree();

    /**
     * 检查分类是否有子分类
     *
     * @param id 分类ID
     * @return 是否有子分类
     */
    boolean hasChildren(Integer id);

    /**
     * 检查分类下是否有商品
     *
     * @param id 分类ID
     * @return 是否有商品
     */
    boolean hasProducts(Integer id);

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态：0-禁用，1-正常
     * @return 更新结果
     */
    boolean updateStatus(Integer id, Integer status);

    /**
     * 获取分类树形结构（包含商品数量）
     *
     * @return 树形结构的分类列表（包含商品数量）
     */
    List<Category> listWithTreeAndCount();

    /**
     * 获取分类下的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    int getProductCount(Integer categoryId);
}