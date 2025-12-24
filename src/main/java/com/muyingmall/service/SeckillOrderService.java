package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.SeckillOrderDTO;
import com.muyingmall.dto.SeckillRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService {
    
    /**
     * 执行秒杀（核心方法）
     * 
     * @param userId 用户ID
     * @param request 秒杀请求
     * @return 订单ID
     */
    Long executeSeckill(Integer userId, SeckillRequestDTO request);
    
    /**
     * 检查用户是否可以参与秒杀
     */
    boolean canUserParticipate(Integer userId, Long seckillProductId);
    
    /**
     * 分页查询秒杀订单（管理后台）
     */
    IPage<SeckillOrderDTO> getOrderPage(Page<SeckillOrderDTO> page, Long activityId, Integer userId, 
                                        String status, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取秒杀订单详情
     */
    SeckillOrderDTO getOrderDetailById(Long id);
    
    /**
     * 根据活动ID统计订单数量
     */
    long countByActivityId(Long activityId);
    
    /**
     * 根据活动ID和状态统计订单数量
     */
    long countByActivityIdAndStatus(Long activityId, String status);
    
    /**
     * 根据活动ID统计销售额
     */
    BigDecimal sumAmountByActivityId(Long activityId);
    
    /**
     * 根据商品ID统计订单数量
     */
    long countByProductId(Long productId);
    
    /**
     * 根据商品ID和状态统计订单数量
     */
    long countByProductIdAndStatus(Long productId, String status);
    
    /**
     * 根据商品ID统计销售数量
     */
    Integer sumQuantityByProductId(Long productId);
    
    /**
     * 根据商品ID统计销售额
     */
    BigDecimal sumAmountByProductId(Long productId);
    
    /**
     * 根据时间范围统计订单数量
     */
    long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据时间范围和状态统计订单数量
     */
    long countByTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status);
    
    /**
     * 根据时间范围统计销售额
     */
    BigDecimal sumAmountByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取销售趋势数据
     */
    Map<String, Object> getSalesTrend(int days);
    
    /**
     * 根据时间范围统计参与用户数
     */
    long countDistinctUsersByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据订单ID查询秒杀订单
     * 
     * @param orderId 订单ID
     * @return 秒杀订单，如果不存在则返回null
     */
    com.muyingmall.entity.SeckillOrder getByOrderId(Integer orderId);
    
    /**
     * 更新秒杀订单
     * 
     * @param seckillOrder 秒杀订单
     * @return 是否更新成功
     */
    boolean updateById(com.muyingmall.entity.SeckillOrder seckillOrder);

}
