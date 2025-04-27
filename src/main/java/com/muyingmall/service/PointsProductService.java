package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsProduct;

import java.util.List;

/**
 * 积分商品服务接口
 */
public interface PointsProductService extends IService<PointsProduct> {

    /**
     * 分页获取积分商品列表
     *
     * @param page     页码
     * @param size     每页大小
     * @param category 商品分类
     * @param isHot    是否热门
     * @return 积分商品分页对象
     */
    Page<PointsProduct> getPointsProductPage(int page, int size, String category, Boolean isHot);

    /**
     * 获取积分商品详情
     *
     * @param id 积分商品ID
     * @return 积分商品详情
     */
    PointsProduct getPointsProductDetail(Long id);

    /**
     * 获取推荐的积分商品列表
     *
     * @param limit 数量限制
     * @return 推荐的积分商品列表
     */
    List<PointsProduct> getRecommendProducts(int limit);
}