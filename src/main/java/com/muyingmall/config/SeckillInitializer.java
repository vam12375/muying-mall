package com.muyingmall.config;

import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 秒杀库存初始化器
 * 应用启动时将所有商品库存同步到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillInitializer implements ApplicationRunner {

    private final SeckillService seckillService;
    private final ProductSkuMapper productSkuMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("========================================");
            log.info("开始初始化秒杀库存到Redis...");
            log.info("========================================");
            
            // 查询所有启用状态的SKU
            List<ProductSku> skuList = productSkuMapper.selectList(null);
            
            int totalCount = 0;
            int successCount = 0;
            int skippedCount = 0;
            
            for (ProductSku sku : skuList) {
                totalCount++;
                
                if (sku.getStatus() != 1) {
                    log.debug("跳过禁用的SKU: skuId={}, status={}", sku.getSkuId(), sku.getStatus());
                    skippedCount++;
                    continue;
                }
                
                if (sku.getStock() == null || sku.getStock() <= 0) {
                    log.debug("跳过零库存SKU: skuId={}, stock={}", sku.getSkuId(), sku.getStock());
                    skippedCount++;
                    continue;
                }
                
                try {
                    seckillService.initSeckillStock(sku.getSkuId(), sku.getStock());
                    log.info("✓ 初始化SKU库存: skuId={}, skuCode={}, stock={}", 
                             sku.getSkuId(), sku.getSkuCode(), sku.getStock());
                    successCount++;
                } catch (Exception e) {
                    log.error("✗ 初始化SKU库存失败: skuId={}, error={}", 
                              sku.getSkuId(), e.getMessage());
                }
            }
            
            log.info("========================================");
            log.info("秒杀库存初始化完成:");
            log.info("  总SKU数量: {}", totalCount);
            log.info("  成功初始化: {}", successCount);
            log.info("  跳过数量: {}", skippedCount);
            log.info("  失败数量: {}", totalCount - successCount - skippedCount);
            log.info("========================================");
            
            // 验证Redis中的数据
            if (successCount > 0) {
                log.info("验证Redis库存数据...");
                for (ProductSku sku : skuList) {
                    if (sku.getStatus() == 1 && sku.getStock() != null && sku.getStock() > 0) {
                        Integer redisStock = seckillService.getRedisStock(sku.getSkuId());
                        if (redisStock != null) {
                            log.debug("Redis库存验证: skuId={}, dbStock={}, redisStock={}", 
                                     sku.getSkuId(), sku.getStock(), redisStock);
                        } else {
                            log.warn("Redis库存验证失败: skuId={} 在Redis中不存在", sku.getSkuId());
                        }
                        break; // 只验证第一个，避免日志过多
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("秒杀库存初始化失败", e);
        }
    }
}
