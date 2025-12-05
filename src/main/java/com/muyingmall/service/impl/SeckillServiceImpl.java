package com.muyingmall.service.impl;

import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现 - Redis预减库存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductSkuMapper productSkuMapper;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final long STOCK_CACHE_EXPIRE = 24 * 60 * 60; // 24小时

    @Override
    public void initSeckillStock(Long skuId, Integer stock) {
        String key = SECKILL_STOCK_KEY + skuId;
        redisTemplate.opsForValue().set(key, stock, STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
        log.info("初始化秒杀库存到Redis: skuId={}, stock={}", skuId, stock);
    }

    @Override
    public boolean preDeductStock(Long skuId, Integer quantity) {
        String key = SECKILL_STOCK_KEY + skuId;
        
        // 先检查库存是否存在
        Object stockObj = redisTemplate.opsForValue().get(key);
        if (stockObj == null) {
            log.error("Redis库存不存在，需要初始化: skuId={}", skuId);
            // 尝试从数据库同步
            syncStockToRedis(skuId);
            stockObj = redisTemplate.opsForValue().get(key);
            if (stockObj == null) {
                log.error("Redis库存同步失败: skuId={}", skuId);
                return false;
            }
        }
        
        // 使用Redis原子操作扣减库存
        Long remaining = redisTemplate.opsForValue().decrement(key, quantity);
        
        if (remaining == null || remaining < 0) {
            // 库存不足，回滚
            if (remaining != null && remaining < 0) {
                redisTemplate.opsForValue().increment(key, quantity);
            }
            log.debug("Redis预减库存失败，库存不足: skuId={}, quantity={}, remaining={}", 
                     skuId, quantity, remaining);
            return false;
        }
        
        log.debug("Redis预减库存成功: skuId={}, quantity={}, remaining={}", 
                  skuId, quantity, remaining);
        return true;
    }

    @Override
    public void restoreRedisStock(Long skuId, Integer quantity) {
        String key = SECKILL_STOCK_KEY + skuId;
        redisTemplate.opsForValue().increment(key, quantity);
        log.info("恢复Redis库存: skuId={}, quantity={}", skuId, quantity);
    }

    @Override
    public Integer getRedisStock(Long skuId) {
        String key = SECKILL_STOCK_KEY + skuId;
        Object stock = redisTemplate.opsForValue().get(key);
        return stock != null ? (Integer) stock : null;
    }

    @Override
    public void syncStockToRedis(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku != null) {
            initSeckillStock(skuId, sku.getStock());
        }
    }
}
