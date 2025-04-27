package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Product;

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
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    Product getProductDetail(Integer id);
}