package com.muyingmall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

        /**
         * 分页获取商品列表
         *
         * @param page        页码
         * @param size        每页大小
         * @param categoryId  分类ID
         * @param isHot       是否热门
         * @param isNew       是否新品
         * @param isRecommend 是否推荐
         * @param keyword     关键词
         * @return 商品分页对象
         */
        Page<Product> getProductPage(int page, int size, Integer categoryId, Boolean isHot, Boolean isNew,
                        Boolean isRecommend, String keyword);

        /**
         * 分页获取商品列表（管理后台）
         *
         * @param page       页码
         * @param size       每页大小
         * @param categoryId 分类ID
         * @param brandId    品牌ID
         * @param keyword    关键词
         * @param status     状态
         * @return 商品分页对象
         */
        Page<Product> getProductPage(int page, int size, Integer categoryId, Integer brandId, String keyword,
                        Integer status);

        /**
         * 获取商品详情
         *
         * @param id 商品ID
         * @return 商品详情
         */
        Product getProductDetail(Integer id);

        /**
         * 创建商品
         *
         * @param product 商品信息
         * @return 创建结果
         */
        boolean createProduct(Product product);

        /**
         * 更新商品
         *
         * @param product 商品信息
         * @return 更新结果
         */
        boolean updateProduct(Product product);

        /**
         * 删除商品
         *
         * @param id 商品ID
         * @return 删除结果
         */
        boolean deleteById(Integer id);

        /**
         * 更新商品状态
         *
         * @param id     商品ID
         * @param status 状态值（0-下架，1-上架）
         * @return 更新结果
         */
        boolean updateStatus(Integer id, Integer status);

        /**
         * 获取品牌关联的商品数量
         *
         * @param brandId 品牌ID
         * @return 商品数量
         */
        int getProductCountByBrandId(Integer brandId);

        /**
         * 获取推荐商品列表
         * 
         * @param productId  当前商品ID，可为空
         * @param categoryId 当前商品分类ID，可为空
         * @param limit      商品数量限制
         * @param type       推荐类型：shop-本店推荐，view-猜你喜欢
         * @return 推荐商品列表
         */
        List<Product> getRecommendedProducts(Integer productId, Integer categoryId, int limit, String type);

        /**
         * 获取商品分类销售数据
         * 
         * @return 分类销售数据列表，每项包含name(分类名)和value(销售数量/比例)
         */
        List<Map<String, Object>> getCategorySales();

        /**
         * 获取热门商品列表
         * 
         * @param limit 数量限制
         * @return 热门商品列表
         */
        List<Product> getHotProducts(int limit);

        /**
         * 获取新品商品列表
         * 
         * @param limit 数量限制
         * @return 新品商品列表
         */
        List<Product> getNewProducts(int limit);

        /**
         * 获取推荐商品列表
         * 
         * @param limit 数量限制
         * @return 推荐商品列表
         */
        List<Product> getRecommendProducts(int limit);
}