package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.SeckillOrderDTO;
import com.muyingmall.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单Mapper
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    
    /**
     * 查询用户在活动中的购买数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM seckill_order " +
            "WHERE user_id = #{userId} AND activity_id = #{activityId} " +
            "AND seckill_product_id = #{seckillProductId} AND status != 2")
    Integer countUserPurchase(@Param("userId") Integer userId, 
                             @Param("activityId") Long activityId,
                             @Param("seckillProductId") Long seckillProductId);
    
    /**
     * 分页查询秒杀订单（管理后台）
     */
    @Select("<script>" +
            "SELECT so.id, so.order_id, so.user_id, u.username, " +
            "so.activity_id, sa.name as activity_name, " +
            "so.seckill_product_id, sp.product_id, p.product_name, p.product_img as product_image, " +
            "so.sku_id, ps.sku_name, so.quantity, so.seckill_price, " +
            "(so.quantity * so.seckill_price) as total_amount, " +
            "so.status, so.create_time, so.update_time " +
            "FROM seckill_order so " +
            "JOIN user u ON so.user_id = u.user_id " +
            "JOIN seckill_activity sa ON so.activity_id = sa.id " +
            "JOIN seckill_product sp ON so.seckill_product_id = sp.id " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON so.sku_id = ps.id " +
            "WHERE 1=1 " +
            "<if test='activityId != null'> AND so.activity_id = #{activityId} </if>" +
            "<if test='userId != null'> AND so.user_id = #{userId} </if>" +
            "<if test='status != null'> AND so.status = #{status} </if>" +
            "<if test='startTime != null'> AND so.create_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND so.create_time &lt;= #{endTime} </if>" +
            "ORDER BY so.create_time DESC" +
            "</script>")
    IPage<SeckillOrderDTO> selectOrderPage(Page<SeckillOrderDTO> page,
                                           @Param("activityId") Long activityId,
                                           @Param("userId") Integer userId,
                                           @Param("status") Integer status,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询秒杀订单详情
     */
    @Select("SELECT so.id, so.order_id, so.user_id, u.username, " +
            "so.activity_id, sa.name as activity_name, " +
            "so.seckill_product_id, sp.product_id, p.product_name, p.product_img as product_image, " +
            "so.sku_id, ps.sku_name, so.quantity, so.seckill_price, " +
            "(so.quantity * so.seckill_price) as total_amount, " +
            "so.status, so.create_time, so.update_time " +
            "FROM seckill_order so " +
            "JOIN user u ON so.user_id = u.user_id " +
            "JOIN seckill_activity sa ON so.activity_id = sa.id " +
            "JOIN seckill_product sp ON so.seckill_product_id = sp.id " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON so.sku_id = ps.id " +
            "WHERE so.id = #{id}")
    SeckillOrderDTO selectOrderDetail(@Param("id") Long id);
    
    /**
     * 按天统计销售趋势
     */
    @Select("<script>" +
            "SELECT DATE(create_time) as date, " +
            "COUNT(*) as order_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 1 THEN quantity * seckill_price ELSE 0 END) as total_amount " +
            "FROM seckill_order " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC" +
            "</script>")
    List<Map<String, Object>> selectSalesTrendByDay(@Param("startTime") LocalDateTime startTime);
}

