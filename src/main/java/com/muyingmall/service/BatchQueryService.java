package com.muyingmall.service;

import com.muyingmall.entity.Brand;
import com.muyingmall.entity.Category;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 批量查询服务接口
 * 用于优化N+1查询问题
 * 
 * 来源：性能优化 - 批量查询
 * 
 */
public interface BatchQueryService {
    
    /**
     * 批量查询用户信息
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    Map<Integer, User> batchGetUsers(List<Integer> userIds);
    
    /**
     * 批量查询商品信息
     * @param productIds 商品ID列表
     * @return 商品ID -> 商品信息的映射
     */
    Map<Integer, Product> batchGetProducts(List<Integer> productIds);
    
    /**
     * 批量查询品牌信息
     * @param brandIds 品牌ID列表
     * @return 品牌ID -> 品牌信息的映射
     */
    Map<Integer, Brand> batchGetBrands(List<Integer> brandIds);
    
    /**
     * 批量查询分类信息
     * @param categoryIds 分类ID列表
     * @return 分类ID -> 分类信息的映射
     */
    Map<Integer, Category> batchGetCategories(List<Integer> categoryIds);
}
