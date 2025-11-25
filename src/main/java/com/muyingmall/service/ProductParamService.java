package com.muyingmall.service;

import com.muyingmall.entity.ProductParam;

import java.util.List;

/**
 * 商品参数服务接口
 */
public interface ProductParamService {
    
    /**
     * 根据商品ID获取参数列表
     * @param productId 商品ID
     * @return 参数列表
     */
    List<ProductParam> getParamsByProductId(Integer productId);
}
