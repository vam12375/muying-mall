package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.SkuStockLogDTO;
import com.muyingmall.entity.ProductSkuStockLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU库存日志 Mapper
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Mapper
public interface ProductSkuStockLogMapper extends BaseMapper<ProductSkuStockLog> {

    /**
     * 分页查询库存日志
     */
    @Select("<script>" +
            "SELECT psl.*, ps.sku_code, ps.sku_name " +
            "FROM product_sku_stock_log psl " +
            "LEFT JOIN product_sku ps ON psl.sku_id = ps.sku_id " +
            "WHERE 1=1 " +
            "<if test='skuId != null'> AND psl.sku_id = #{skuId} </if>" +
            "<if test='orderId != null'> AND psl.order_id = #{orderId} </if>" +
            "<if test='changeType != null and changeType != \"\"'> AND psl.change_type = #{changeType} </if>" +
            "<if test='startTime != null'> AND psl.create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND psl.create_time &lt;= #{endTime} </if>" +
            "ORDER BY psl.create_time DESC" +
            "</script>")
    @Results({
        @Result(column = "log_id", property = "logId"),
        @Result(column = "sku_id", property = "skuId"),
        @Result(column = "sku_code", property = "skuCode"),
        @Result(column = "sku_name", property = "skuName"),
        @Result(column = "order_id", property = "orderId"),
        @Result(column = "change_type", property = "changeType"),
        @Result(column = "change_quantity", property = "changeQuantity"),
        @Result(column = "before_stock", property = "beforeStock"),
        @Result(column = "after_stock", property = "afterStock"),
        @Result(column = "operator", property = "operator"),
        @Result(column = "remark", property = "remark"),
        @Result(column = "create_time", property = "createTime")
    })
    IPage<SkuStockLogDTO> selectLogPage(Page<?> page, 
                                        @Param("skuId") Long skuId,
                                        @Param("orderId") Integer orderId,
                                        @Param("changeType") String changeType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 根据SKU ID查询日志
     */
    @Select("SELECT psl.*, ps.sku_code, ps.sku_name " +
            "FROM product_sku_stock_log psl " +
            "LEFT JOIN product_sku ps ON psl.sku_id = ps.sku_id " +
            "WHERE psl.sku_id = #{skuId} " +
            "ORDER BY psl.create_time DESC")
    @Results({
        @Result(column = "log_id", property = "logId"),
        @Result(column = "sku_id", property = "skuId"),
        @Result(column = "sku_code", property = "skuCode"),
        @Result(column = "sku_name", property = "skuName"),
        @Result(column = "order_id", property = "orderId"),
        @Result(column = "change_type", property = "changeType"),
        @Result(column = "change_quantity", property = "changeQuantity"),
        @Result(column = "before_stock", property = "beforeStock"),
        @Result(column = "after_stock", property = "afterStock"),
        @Result(column = "operator", property = "operator"),
        @Result(column = "remark", property = "remark"),
        @Result(column = "create_time", property = "createTime")
    })
    List<SkuStockLogDTO> selectBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据订单ID查询日志
     */
    @Select("SELECT psl.*, ps.sku_code, ps.sku_name " +
            "FROM product_sku_stock_log psl " +
            "LEFT JOIN product_sku ps ON psl.sku_id = ps.sku_id " +
            "WHERE psl.order_id = #{orderId} " +
            "ORDER BY psl.create_time DESC")
    @Results({
        @Result(column = "log_id", property = "logId"),
        @Result(column = "sku_id", property = "skuId"),
        @Result(column = "sku_code", property = "skuCode"),
        @Result(column = "sku_name", property = "skuName"),
        @Result(column = "order_id", property = "orderId"),
        @Result(column = "change_type", property = "changeType"),
        @Result(column = "change_quantity", property = "changeQuantity"),
        @Result(column = "before_stock", property = "beforeStock"),
        @Result(column = "after_stock", property = "afterStock"),
        @Result(column = "operator", property = "operator"),
        @Result(column = "remark", property = "remark"),
        @Result(column = "create_time", property = "createTime")
    })
    List<SkuStockLogDTO> selectByOrderId(@Param("orderId") Integer orderId);
}
