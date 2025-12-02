package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.ProductSkuDTO;
import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.entity.ProductSku;

import java.util.List;

/**
 * 商品SKU Service
 * 
 * @author 青柠檬
 * @date 2025-11-24
 */
public interface ProductSkuService extends IService<ProductSku> {

    /**
     * 根据商品ID获取SKU列表
     */
    List<ProductSkuDTO> getSkuListByProductId(Integer productId);

    /**
     * 根据SKU ID获取SKU详情
     */
    ProductSkuDTO getSkuById(Long skuId);

    /**
     * 根据SKU编码获取SKU详情
     */
    ProductSkuDTO getSkuByCode(String skuCode);

    /**
     * 批量保存或更新SKU
     */
    boolean saveOrUpdateBatch(Integer productId, List<ProductSkuDTO> skuList);

    /**
     * 扣减库存
     */
    boolean deductStock(Long skuId, Integer quantity);

    /**
     * 扣减库存（带订单ID）
     */
    boolean deductStock(Long skuId, Integer quantity, Integer orderId, String remark);

    /**
     * 批量扣减库存
     */
    boolean batchDeductStock(List<SkuStockDTO> stockList);

    /**
     * 恢复库存
     */
    boolean restoreStock(Long skuId, Integer quantity);

    /**
     * 批量恢复库存
     */
    boolean batchRestoreStock(List<SkuStockDTO> stockList);

    /**
     * 调整库存
     */
    boolean adjustStock(Long skuId, Integer quantity, String operator, String remark);

    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long skuId, Integer quantity);

    /**
     * 删除商品的所有SKU
     */
    boolean deleteByProductId(Integer productId);

    /**
     * 查询库存不足的SKU列表
     */
    List<ProductSkuDTO> getLowStockSkus(Integer threshold);

    /**
     * 更新商品的价格范围
     */
    void updateProductPriceRange(Integer productId);
}
