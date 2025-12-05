package com.muyingmall.controller.admin;

import com.muyingmall.common.api.Result;
import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀库存管理控制器（管理后台）
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
@Tag(name = "秒杀库存管理")
public class SeckillStockController {

    private final SeckillService seckillService;
    private final ProductSkuMapper productSkuMapper;

    @GetMapping("/stock/check/{skuId}")
    @Operation(summary = "检查SKU的Redis库存")
    public Result<Map<String, Object>> checkStock(@PathVariable Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            return Result.error("SKU不存在");
        }

        Integer redisStock = seckillService.getRedisStock(skuId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("skuId", skuId);
        data.put("skuCode", sku.getSkuCode());
        data.put("dbStock", sku.getStock());
        data.put("redisStock", redisStock);
        data.put("synced", redisStock != null);
        
        return Result.success(data);
    }

    @PostMapping("/stock/sync/{skuId}")
    @Operation(summary = "同步SKU库存到Redis")
    public Result<Void> syncStock(@PathVariable Long skuId) {
        seckillService.syncStockToRedis(skuId);
        return Result.success(null, "同步成功");
    }
}
