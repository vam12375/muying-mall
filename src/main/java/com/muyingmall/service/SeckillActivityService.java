package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillActivity;

import java.util.List;

/**
 * 秒杀活动服务接口
 */
public interface SeckillActivityService extends IService<SeckillActivity> {
    
    /**
     * 获取进行中的秒杀活动列表
     */
    List<SeckillActivity> getActiveActivities();
    
    /**
     * 获取活动的秒杀商品列表
     */
    List<SeckillProductDTO> getActivityProducts(Long activityId);
    
    /**
     * 获取秒杀商品详情
     */
    SeckillProductDTO getSeckillProductDetail(Long seckillProductId);
    
    /**
     * 初始化秒杀活动库存到Redis
     */
    void initActivityStock(Long activityId);
    
    /**
     * 分页查询秒杀活动（管理后台）
     */
    IPage<SeckillActivity> getActivityPage(Page<SeckillActivity> page, String keyword, Integer status);
    
    /**
     * 根据状态统计活动数量
     */
    long countByStatus(Integer status);
    
    /**
     * 根据状态获取活动列表
     */
    List<SeckillActivity> getActivitiesByStatus(Integer status);
    
    /**
     * 更新活动状态（根据时间自动更新）
     */
    int updateActivityStatus();
}
