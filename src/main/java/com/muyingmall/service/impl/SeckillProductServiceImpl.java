package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.mapper.SeckillProductMapper;
import com.muyingmall.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct> 
        implements SeckillProductService {
    
    private final SeckillProductMapper seckillProductMapper;
    
    @Override
    public IPage<SeckillProductDTO> getProductPage(Page<SeckillProductDTO> page, Long activityId, String keyword) {
        return seckillProductMapper.selectProductPage(page, activityId, keyword);
    }
    
    @Override
    public List<SeckillProductDTO> getProductsByActivityId(Long activityId) {
        return seckillProductMapper.selectSeckillProductsByActivity(activityId);
    }
    
    @Override
    public SeckillProductDTO getProductDetailById(Long id) {
        return seckillProductMapper.selectSeckillProductDetail(id);
    }
    
    @Override
    public long countByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(SeckillProduct::getActivityId, activityId)
                .count();
    }
    
    @Override
    public List<Map<String, Object>> getStockWarningList(int threshold) {
        List<Map<String, Object>> warningList = seckillProductMapper.selectStockWarningList();
        
        // 计算库存百分比并过滤
        return warningList.stream()
                .filter(item -> {
                    Integer originalStock = (Integer) item.get("original_stock");
                    Integer currentStock = (Integer) item.get("current_stock");
                    if (originalStock != null && originalStock > 0) {
                        int stockPercent = (currentStock * 100) / originalStock;
                        item.put("stock_percent", stockPercent);
                        return stockPercent <= threshold;
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getHotProductsRanking(int limit, LocalDateTime startTime) {
        return seckillProductMapper.selectHotProductsRanking(limit, startTime);
    }
}
