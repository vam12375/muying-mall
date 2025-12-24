package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillProduct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 秒杀商品服务接口
 */
public interface SeckillProductService extends IService<SeckillProduct> {
    
    /**
     * 分页查询秒杀商品（管理后台）
     */
    IPage<SeckillProductDTO> getProductPage(Page<SeckillProductDTO> page, Long activityId, String keyword);
    
    /**
     * 根据活动ID获取秒杀商品列表
     */
    List<SeckillProductDTO> getProductsByActivityId(Long activityId);
    
    /**
     * 获取秒杀商品详情
     */
    SeckillProductDTO getProductDetailById(Long id);
    
    /**
     * 根据活动ID统计商品数量
     */
    long countByActivityId(Long activityId);
    
    /**
     * 获取库存预警列表
     */
    List<Map<String, Object>> getStockWarningList(int threshold);
    
    /**
     * 获取热门商品排行
     */
    List<Map<String, Object>> getHotProductsRanking(int limit, LocalDateTime startTime);
}
