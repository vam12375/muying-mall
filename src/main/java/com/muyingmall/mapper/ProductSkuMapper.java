package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU Mapper
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 根据商品ID查询SKU列表
     */
    List<ProductSku> selectByProductId(@Param("productId") Integer productId);

    /**
     * 根据SKU编码查询
     */
    ProductSku selectBySkuCode(@Param("skuCode") String skuCode);

    /**
     * 扣减库存（使用乐观锁）
     */
    int deductStock(@Param("skuId") Long skuId, 
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 恢复库存
     */
    int restoreStock(@Param("skuId") Long skuId, 
                     @Param("quantity") Integer quantity);

    /**
     * 批量更新库存
     */
    int batchUpdateStock(@Param("list") List<ProductSku> list);

    /**
     * 查询库存不足的SKU列表
     */
    List<ProductSku> selectLowStockSkus(@Param("threshold") Integer threshold);

    /**
     * 根据商品ID删除SKU
     */
    int deleteByProductId(@Param("productId") Integer productId);
}
