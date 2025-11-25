package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.ProductSku;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商品SKU Mapper
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 根据商品ID查询SKU列表
     */
    @Select("SELECT * FROM product_sku WHERE product_id = #{productId} ORDER BY sort_order ASC, sku_id ASC")
    List<ProductSku> selectByProductId(@Param("productId") Integer productId);

    /**
     * 根据SKU编码查询
     */
    @Select("SELECT * FROM product_sku WHERE sku_code = #{skuCode} LIMIT 1")
    ProductSku selectBySkuCode(@Param("skuCode") String skuCode);

    /**
     * 扣减库存（使用乐观锁）
     */
    @Update("UPDATE product_sku SET stock = stock - #{quantity}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND stock >= #{quantity} AND version = #{version}")
    int deductStock(@Param("skuId") Long skuId, 
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 恢复库存
     */
    @Update("UPDATE product_sku SET stock = stock + #{quantity}, version = version + 1 " +
            "WHERE sku_id = #{skuId}")
    int restoreStock(@Param("skuId") Long skuId, 
                     @Param("quantity") Integer quantity);

    /**
     * 查询库存不足的SKU列表
     */
    @Select("SELECT ps.* FROM product_sku ps " +
            "WHERE ps.stock <= #{threshold} AND ps.status = 1 " +
            "ORDER BY ps.stock ASC")
    List<ProductSku> selectLowStockSkus(@Param("threshold") Integer threshold);

    /**
     * 根据商品ID删除SKU
     */
    @Delete("DELETE FROM product_sku WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Integer productId);
}
