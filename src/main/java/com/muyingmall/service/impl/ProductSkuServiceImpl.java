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
import com.muyingmall.service.SeckillService;
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
    private final SeckillService seckillService;

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

        // 更新商品的价格范围和总库存
        if (result) {
            updateProductPriceRange(productId);
            updateProductTotalStock(productId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long skuId, Integer quantity) {
        return deductStock(skuId, quantity, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long skuId, Integer quantity, Integer orderId, String remark) {
        // Redis预减库存 - 快速失败，避免数据库压力
        if (!seckillService.preDeductStock(skuId, quantity)) {
            throw new BusinessException("库存不足");
        }

        try {
            // 查询SKU信息
            ProductSku sku = productSkuMapper.selectById(skuId);
            if (sku == null) {
                // 回滚Redis库存
                seckillService.restoreRedisStock(skuId, quantity);
                throw new BusinessException("SKU不存在");
            }

            // 检查数据库库存（双重保险）
            if (sku.getStock() < quantity) {
                // 回滚Redis库存
                seckillService.restoreRedisStock(skuId, quantity);
                throw new BusinessException("库存不足，当前库存：" + sku.getStock() + "，需要：" + quantity);
            }

            // 扣减数据库库存（使用乐观锁，最多重试3次）
            int retryCount = 0;
            int maxRetries = 3;
            int rows = 0;
            
            while (retryCount < maxRetries) {
                rows = productSkuMapper.deductStock(skuId, quantity, sku.getVersion());
                if (rows > 0) {
                    break;
                }
                
                // 重新查询最新版本
                sku = productSkuMapper.selectById(skuId);
                if (sku == null || sku.getStock() < quantity) {
                    break;
                }
                
                retryCount++;
                log.debug("库存扣减乐观锁冲突，重试第{}次: skuId={}", retryCount, skuId);
            }
            
            if (rows == 0) {
                // 回滚Redis库存
                seckillService.restoreRedisStock(skuId, quantity);
                throw new BusinessException("库存扣减失败，请重试");
            }

            // 记录日志
            String logRemark = remark != null ? remark : "扣减库存";
            recordStockLog(skuId, orderId, "DEDUCT", -quantity, sku.getStock(), 
                          sku.getStock() - quantity, null, logRemark);

            // 更新商品总库存
            updateProductTotalStock(sku.getProductId());

            return true;
            
        } catch (Exception e) {
            // 异常时回滚Redis库存
            seckillService.restoreRedisStock(skuId, quantity);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeductStock(List<SkuStockDTO> stockList) {
        // 记录需要更新总库存的商品ID（去重）
        java.util.Set<Integer> productIds = new java.util.HashSet<>();
        
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
            
            // 记录商品ID
            productIds.add(sku.getProductId());
        }

        // 批量更新商品总库存
        for (Integer productId : productIds) {
            updateProductTotalStock(productId);
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

        // 自动更新商品总库存
        updateProductTotalStock(sku.getProductId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRestoreStock(List<SkuStockDTO> stockList) {
        // 记录需要更新总库存的商品ID（去重）
        java.util.Set<Integer> productIds = new java.util.HashSet<>();
        
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
            
            // 记录商品ID
            productIds.add(sku.getProductId());
        }

        // 批量更新商品总库存
        for (Integer productId : productIds) {
            updateProductTotalStock(productId);
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

        // 自动更新商品总库存
        updateProductTotalStock(sku.getProductId());

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
     * 更新商品总库存（所有启用SKU的库存之和）
     * 
     * @param productId 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProductTotalStock(Integer productId) {
        List<ProductSku> skuList = productSkuMapper.selectByProductId(productId);
        if (skuList.isEmpty()) {
            return;
        }

        // 计算总库存（只统计启用状态的SKU）
        Integer totalStock = skuList.stream()
            .filter(sku -> sku.getStatus() == 1) // 只统计启用的SKU
            .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
            .sum();

        // 更新商品表的库存字段
        Product product = new Product();
        product.setProductId(productId);
        product.setStock(totalStock);
        productMapper.updateById(product);
        
        log.debug("商品 {} 总库存已更新为: {}", productId, totalStock);
    }

    /**
     * 记录库存变更日志
     */
    private void recordStockLog(Long skuId, Integer orderId, String changeType, 
                                Integer changeQuantity, Integer beforeStock, Integer afterStock,
                                String operator, String remark) {
        // 查询SKU信息获取sku_code
        ProductSku sku = productSkuMapper.selectById(skuId);
        
        ProductSkuStockLog log = new ProductSkuStockLog();
        log.setSkuId(skuId);
        log.setSkuCode(sku != null ? sku.getSkuCode() : "UNKNOWN"); // 设置sku_code
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
     * 支持两种格式：
     * 1. 数组格式: [{"spec_name":"颜色","spec_value":"红色"}]
     * 2. 对象格式: {"颜色":"红色","尺寸":"M"}
     */
    private ProductSkuDTO convertToDTO(ProductSku entity) {
        ProductSkuDTO dto = new ProductSkuDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 解析JSON格式的规格值
        if (entity.getSpecValues() != null && !entity.getSpecValues().isEmpty()) {
            try {
                String specJson = entity.getSpecValues().trim();
                List<Map<String, String>> specValues = new ArrayList<>();
                
                if (specJson.startsWith("[")) {
                    // 数组格式: [{"spec_name":"颜色","spec_value":"红色"}]
                    specValues = JSON.parseObject(specJson, new TypeReference<List<Map<String, String>>>() {});
                } else if (specJson.startsWith("{")) {
                    // 对象格式: {"颜色":"红色","尺寸":"M"} -> 转换为数组格式
                    Map<String, String> specMap = JSON.parseObject(specJson, new TypeReference<Map<String, String>>() {});
                    for (Map.Entry<String, String> entry : specMap.entrySet()) {
                        Map<String, String> item = new java.util.HashMap<>();
                        item.put("spec_name", entry.getKey());
                        item.put("spec_value", entry.getValue());
                        specValues.add(item);
                    }
                }
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
