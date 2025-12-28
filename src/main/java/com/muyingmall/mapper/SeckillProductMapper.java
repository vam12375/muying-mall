package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 秒杀商品Mapper
 */
@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {
    
    /**
     * 查询秒杀商品列表（含商品信息）
     * 修复：添加soldCount统计，支持进度条显示
     */
    @Select("SELECT sp.id, sp.activity_id, sa.name as activity_name, " +
            "sp.product_id, p.product_name, p.product_img as product_image, " +
            "sp.sku_id, ps.sku_name, ps.price as original_price, " +
            "sp.seckill_price, sp.seckill_stock, sp.limit_per_user, " +
            "sa.start_time, sa.end_time, sa.status as activity_status, " +
            "COALESCE(SUM(so.quantity), 0) as sold_count " +
            "FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON sp.sku_id = ps.sku_id " +
            "LEFT JOIN seckill_order so ON sp.id = so.seckill_product_id AND so.status = 1 " +
            "WHERE sa.id = #{activityId} " +
            "GROUP BY sp.id, sp.activity_id, sa.name, sp.product_id, p.product_name, p.product_img, " +
            "sp.sku_id, ps.sku_name, ps.price, sp.seckill_price, sp.seckill_stock, sp.limit_per_user, " +
            "sa.start_time, sa.end_time, sa.status " +
            "ORDER BY sp.sort_order ASC, sp.id DESC")
    List<SeckillProductDTO> selectSeckillProductsByActivity(@Param("activityId") Long activityId);
    
    /**
     * 查询秒杀商品详情
     * 修复：添加soldCount统计，支持进度条显示
     */
    @Select("SELECT sp.id, sp.activity_id, sa.name as activity_name, " +
            "sp.product_id, p.product_name, p.product_img as product_image, " +
            "sp.sku_id, ps.sku_name, ps.price as original_price, " +
            "sp.seckill_price, sp.seckill_stock, sp.limit_per_user, " +
            "sa.start_time, sa.end_time, sa.status as activity_status, " +
            "COALESCE(SUM(so.quantity), 0) as sold_count " +
            "FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON sp.sku_id = ps.sku_id " +
            "LEFT JOIN seckill_order so ON sp.id = so.seckill_product_id AND so.status = 1 " +
            "WHERE sp.id = #{id} " +
            "GROUP BY sp.id, sp.activity_id, sa.name, sp.product_id, p.product_name, p.product_img, " +
            "sp.sku_id, ps.sku_name, ps.price, sp.seckill_price, sp.seckill_stock, sp.limit_per_user, " +
            "sa.start_time, sa.end_time, sa.status")
    SeckillProductDTO selectSeckillProductDetail(@Param("id") Long id);
    
    /**
     * 分页查询秒杀商品（管理后台）
     */
    @Select("<script>" +
            "SELECT sp.id, sp.activity_id, sa.name as activity_name, " +
            "sp.product_id, p.product_name, p.product_img as product_image, " +
            "sp.sku_id, ps.sku_name, ps.price as original_price, " +
            "sp.seckill_price, sp.seckill_stock, sp.limit_per_user, sp.sort_order, " +
            "sa.start_time, sa.end_time, sa.status as activity_status " +
            "FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON sp.sku_id = ps.sku_id " +
            "WHERE 1=1 " +
            "<if test='activityId != null'> AND sp.activity_id = #{activityId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (p.product_name LIKE CONCAT('%', #{keyword}, '%') OR sa.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY sp.create_time DESC" +
            "</script>")
    IPage<SeckillProductDTO> selectProductPage(Page<SeckillProductDTO> page, 
                                                @Param("activityId") Long activityId, 
                                                @Param("keyword") String keyword);
    
    /**
     * 获取库存预警列表
     */
    @Select("SELECT sp.id, sp.product_id, p.product_name, sp.sku_id, ps.sku_name, " +
            "sp.seckill_stock as original_stock, sp.seckill_stock as current_stock, " +
            "sp.activity_id, sa.name as activity_name " +
            "FROM seckill_product sp " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON sp.sku_id = ps.sku_id " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE sa.status = 1 " +
            "ORDER BY sp.seckill_stock ASC")
    List<Map<String, Object>> selectStockWarningList();
    
    /**
     * 获取热门商品排行
     * 按秒杀成功次数排序，统计全部历史数据
     */
    @Select("<script>" +
            "SELECT sp.id, sp.product_id as productId, p.product_name as productName, " +
            "p.product_img as productImage, " +
            "sp.sku_id as skuId, ps.sku_name as skuName, sp.seckill_price as seckillPrice, " +
            "sa.name as activityName, " +
            "COALESCE(SUM(so.quantity), 0) as salesCount, " +
            "COALESCE(SUM(so.quantity * so.seckill_price), 0) as salesAmount " +
            "FROM seckill_product sp " +
            "JOIN product p ON sp.product_id = p.product_id " +
            "JOIN product_sku ps ON sp.sku_id = ps.sku_id " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "LEFT JOIN seckill_order so ON sp.id = so.seckill_product_id AND so.status = 1 " +
            "<if test='startTime != null'> AND so.create_time >= #{startTime} </if>" +
            "GROUP BY sp.id, sp.product_id, p.product_name, p.product_img, " +
            "sp.sku_id, ps.sku_name, sp.seckill_price, sa.name " +
            "ORDER BY salesCount DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> selectHotProductsRanking(@Param("limit") int limit, 
                                                        @Param("startTime") LocalDateTime startTime);
    
    /**
     * 扣减秒杀库存（使用乐观锁）
     * 
     * @param seckillProductId 秒杀商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    @org.apache.ibatis.annotations.Update("UPDATE seckill_product " +
            "SET seckill_stock = seckill_stock - #{quantity}, " +
            "update_time = NOW() " +
            "WHERE id = #{seckillProductId} " +
            "AND seckill_stock >= #{quantity}")
    int deductStock(@Param("seckillProductId") Long seckillProductId, 
                    @Param("quantity") Integer quantity);
}

