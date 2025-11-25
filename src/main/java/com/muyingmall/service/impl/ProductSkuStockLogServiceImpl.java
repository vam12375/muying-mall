package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.dto.SkuStockLogDTO;
import com.muyingmall.entity.ProductSkuStockLog;
import com.muyingmall.mapper.ProductSkuStockLogMapper;
import com.muyingmall.service.ProductSkuStockLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU库存日志 Service实现类
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Slf4j
@Service
public class ProductSkuStockLogServiceImpl extends ServiceImpl<ProductSkuStockLogMapper, ProductSkuStockLog> 
    implements ProductSkuStockLogService {

    @Resource
    private ProductSkuStockLogMapper stockLogMapper;

    @Override
    public IPage<SkuStockLogDTO> getLogPage(Integer page, Integer size, 
                                            Long skuId, Integer orderId, String changeType,
                                            LocalDateTime startTime, LocalDateTime endTime) {
        Page<SkuStockLogDTO> pageParam = new Page<>(page, size);
        return stockLogMapper.selectLogPage(pageParam, skuId, orderId, changeType, startTime, endTime);
    }

    @Override
    public List<SkuStockLogDTO> getLogsBySkuId(Long skuId) {
        return stockLogMapper.selectBySkuId(skuId);
    }

    @Override
    public List<SkuStockLogDTO> getLogsByOrderId(Integer orderId) {
        return stockLogMapper.selectByOrderId(orderId);
    }
}
