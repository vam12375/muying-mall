package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.SkuStockLogDTO;
import com.muyingmall.entity.ProductSkuStockLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU库存日志 Service
 * 
 * @author 青柠檬
 * @date 2024-11-24
 */
public interface ProductSkuStockLogService extends IService<ProductSkuStockLog> {

    /**
     * 分页查询库存日志
     */
    IPage<SkuStockLogDTO> getLogPage(Integer page, Integer size, 
                                     Long skuId, Integer orderId, String changeType,
                                     LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据SKU ID查询日志
     */
    List<SkuStockLogDTO> getLogsBySkuId(Long skuId);

    /**
     * 根据订单ID查询日志
     */
    List<SkuStockLogDTO> getLogsByOrderId(Integer orderId);
}
