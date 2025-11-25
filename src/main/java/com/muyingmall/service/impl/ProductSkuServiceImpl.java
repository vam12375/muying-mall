package com.muyingmall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.dto.ProductSkuDTO;
import com.muyingmall.dto.SkuStockDTO;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.ProductSku;
import com.muyingmall.entity.ProductSkuStockLog;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.mapper.ProductSkuStockLogMapper;
import com.muyingmall.service.ProductSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品SKU Service实现类
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    private final ProductSkuMapper productSkuMapper;
    private final ProductSkuStockLogMapper stockLogMapper;
    private final ProductMapper productMapper;

    @Override
    public List<ProductSkuDTO> getSkuListByProductId(Integer productId) {
        List<ProductSku> skuList = productSkuMapper.selectByProductId(productId);
        return skuList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductSkuDTO getSkuById(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        return convertToDTO(sku);
    }

    @Override
    public ProductSkuDTO getSkuByCode(String skuCode) {
        ProductSku sku = productSkuMapper.selectBySkuCode(skuCode);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        return convertToDTO(sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateBatch(Integer productId, List<ProductSkuDTO> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            return true;
        }

        // 删除旧的SKU
        productSkuMapper.deleteByProductId(productId);

        // 保存新的SKU
        List<ProductSku> entities = new ArrayList<>();
        for (ProductSkuDTO dto : skuList) {
            ProductSku entity = convertToEntity(dto);
            entity.setProductId(productId);
            entities.add(entity);
        }

        boolean result = saveBatch(entities);

        // 更新商品的价格范围
        if (result) {
            updateProductPriceRange(productId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long skuId, Integer quantity) {
        // 查询SKU信息
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        // 检查库存
        if (sku.getStock() < quantity) {
            throw new BusinessException("库存不足，当前库存：" + sku.getStock() + "，需要：" + quantity);
        }

        // 扣减库存（使用乐观锁）
        int rows = productSkuMapper.deductStock(skuId, quantity, sku.getVersion());
        if (rows == 0) {
            throw new BusinessException("库存扣减失败，请重试");
        }

        // 记录日志
        recordStockLog(skuId, null, "DEDUCT", -quantity, sku.getStock(), sku.getStock() - quantity, null, "扣减库存");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeductStock(List<SkuStockDTO> stockList) {
        for (SkuStockDTO stockDTO : stockList) {
            // 查询SKU信息
            ProductSku sku = productSkuMapper.selectById(stockDTO.getSkuId());
            if (sku == null) {
                throw new BusinessException("SKU不存在：" + stockDTO.getSkuId());
            }

            // 检查库存
            if (sku.getStock() < stockDTO.getQuantity()) {
                throw new BusinessException("SKU库存不足：" + sku.getSkuName() + 
                    "，当前库存：" + sku.getStock() + "，需要：" + stockDTO.getQuantity());
            }

            // 扣减库存（使用乐观锁）
            int rows = productSkuMapper.deductStock(stockDTO.getSkuId(), stockDTO.getQuantity(), sku.getVersion());
            if (rows == 0) {
                throw new BusinessException("库存扣减失败，请重试：" + sku.getSkuName());
            }

            // 记录日志
            recordStockLog(stockDTO.getSkuId(), stockDTO.getOrderId(), "DEDUCT", 
                -stockDTO.getQuantity(), sku.getStock(), sku.getStock() - stockDTO.getQuantity(),
                stockDTO.getOperator(), stockDTO.getRemark());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(Long skuId, Integer quantity) {
        // 查询SKU信息
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        // 恢复库存
        int rows = productSkuMapper.restoreStock(skuId, quantity);
        if (rows == 0) {
            throw new BusinessException("库存恢复失败");
        }

        // 记录日志
        recordStockLog(skuId, null, "RESTORE", quantity, sku.getStock(), sku.getStock() + quantity, null, "恢复库存");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRestoreStock(List<SkuStockDTO> stockList) {
        for (SkuStockDTO stockDTO : stockList) {
            // 查询SKU信息
            ProductSku sku = productSkuMapper.selectById(stockDTO.getSkuId());
            if (sku == null) {
                throw new BusinessException("SKU不存在：" + stockDTO.getSkuId());
            }

            // 恢复库存
            int rows = productSkuMapper.restoreStock(stockDTO.getSkuId(), stockDTO.getQuantity());
            if (rows == 0) {
                throw new BusinessException("库存恢复失败：" + sku.getSkuName());
            }

            // 记录日志
            recordStockLog(stockDTO.getSkuId(), stockDTO.getOrderId(), "RESTORE",
                stockDTO.getQuantity(), sku.getStock(), sku.getStock() + stockDTO.getQuantity(),
                stockDTO.getOperator(), stockDTO.getRemark());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustStock(Long skuId, Integer quantity, String operator, String remark) {
        // 查询SKU信息
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        Integer oldStock = sku.getStock();
        Integer newStock = quantity;

        // 更新库存
        sku.setStock(newStock);
        int rows = productSkuMapper.updateById(sku);
        if (rows == 0) {
            throw new BusinessException("库存调整失败");
        }

        // 记录日志
        recordStockLog(skuId, null, "ADJUST", newStock - oldStock, oldStock, newStock, operator, remark);

        return true;
    }

    @Override
    public boolean checkStock(Long skuId, Integer quantity) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            return false;
        }
        return sku.getStock() >= quantity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByProductId(Integer productId) {
        return productSkuMapper.deleteByProductId(productId) > 0;
    }

    @Override
    public List<ProductSkuDTO> getLowStockSkus(Integer threshold) {
        List<ProductSku> skuList = productSkuMapper.selectLowStockSkus(threshold);
        return skuList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductPriceRange(Integer productId) {
        List<ProductSku> skuList = productSkuMapper.selectByProductId(productId);
        if (skuList.isEmpty()) {
            return;
        }

        // 计算最低价和最高价
        BigDecimal minPrice = skuList.stream()
            .map(ProductSku::getPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = skuList.stream()
            .map(ProductSku::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        // 更新商品表
        Product product = new Product();
        product.setProductId(productId);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setHasSku(1);
        productMapper.updateById(product);
    }

    /**
     * 记录库存变更日志
     */
    private void recordStockLog(Long skuId, Integer orderId, String changeType, 
                                Integer changeQuantity, Integer beforeStock, Integer afterStock,
                                String operator, String remark) {
        ProductSkuStockLog log = new ProductSkuStockLog();
        log.setSkuId(skuId);
        log.setOrderId(orderId);
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setOperator(operator);
        log.setRemark(remark);
        stockLogMapper.insert(log);
    }

    /**
     * 实体转DTO
     */
    private ProductSkuDTO convertToDTO(ProductSku entity) {
        ProductSkuDTO dto = new ProductSkuDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 解析JSON格式的规格值
        if (entity.getSpecValues() != null) {
            try {
                List<Map<String, String>> specValues = JSON.parseObject(
                    entity.getSpecValues(), 
                    new TypeReference<List<Map<String, String>>>() {}
                );
                dto.setSpecValues(specValues);
            } catch (Exception e) {
                log.error("解析规格值失败: {}", entity.getSpecValues(), e);
            }
        }
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private ProductSku convertToEntity(ProductSkuDTO dto) {
        ProductSku entity = new ProductSku();
        BeanUtils.copyProperties(dto, entity);
        
        // 将规格值列表转为JSON字符串
        if (dto.getSpecValues() != null) {
            entity.setSpecValues(JSON.toJSONString(dto.getSpecValues()));
        }
        
        return entity;
    }
}
