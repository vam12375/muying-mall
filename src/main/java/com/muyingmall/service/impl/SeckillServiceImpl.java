package com.muyingmall.service.impl;

import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.service.SeckillService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现 - Redis预减库存 + Lua脚本原子性操作
 *
 * 支持两种库存扣减模式：
 * 1. 简单模式：使用Redis DECR命令（preDeductStock方法）
 * 2. Lua脚本模式：使用Lua脚本保证原子性（deductStockWithLua方法）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductSkuMapper productSkuMapper;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USERS_KEY = "seckill:users:";
    private static final long STOCK_CACHE_EXPIRE = 24 * 60 * 60; // 24小时

    // Lua脚本：库存扣减
    private DefaultRedisScript<Long> stockDeductScript;
    // Lua脚本：库存恢复
    private DefaultRedisScript<Long> stockRestoreScript;

    /**
     * 初始化Lua脚本
     */
    @PostConstruct
    public void initLuaScripts() {
        // 加载库存扣减脚本
        stockDeductScript = new DefaultRedisScript<>();
        stockDeductScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_deduct.lua")));
        stockDeductScript.setResultType(Long.class);

        // 加载库存恢复脚本
        stockRestoreScript = new DefaultRedisScript<>();
        stockRestoreScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_restore.lua")));
        stockRestoreScript.setResultType(Long.class);

        log.info("秒杀Lua脚本初始化完成：stock_deduct.lua, stock_restore.lua");
    }

    /**
     * 使用Lua脚本原子性扣减库存（推荐方式）
     *
     * @param seckillProductId 秒杀商品ID
     * @param skuId            SKU ID
     * @param quantity         扣减数量
     * @param userId           用户ID（用于防重复购买，可传null跳过检查）
     * @return 扣减结果：1成功，-1库存不足，-2用户已购买，-3库存Key不存在
     */
    public int deductStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId) {
        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = SECKILL_USERS_KEY + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        // 执行Lua脚本
        Long result = redisTemplate.execute(
                stockDeductScript,
                keys,
                quantity.toString(),
                userId != null ? userId.toString() : "");

        int resultCode = result != null ? result.intValue() : -3;

        switch (resultCode) {
            case 1:
                log.debug("Lua脚本扣减库存成功: seckillProductId={}, skuId={}, quantity={}, userId={}",
                        seckillProductId, skuId, quantity, userId);
                break;
            case -1:
                log.debug("Lua脚本扣减库存失败，库存不足: skuId={}, quantity={}", skuId, quantity);
                break;
            case -2:
                log.debug("Lua脚本扣减库存失败，用户已购买: seckillProductId={}, userId={}", seckillProductId, userId);
                break;
            case -3:
                log.warn("Lua脚本扣减库存失败，库存Key不存在: skuId={}", skuId);
                break;
            default:
                log.error("Lua脚本扣减库存返回未知结果: skuId={}, result={}", skuId, resultCode);
        }

        return resultCode;
    }

    /**
     * 使用Lua脚本原子性恢复库存
     *
     * @param seckillProductId 秒杀商品ID
     * @param skuId            SKU ID
     * @param quantity         恢复数量
     * @param userId           用户ID（用于清除购买记录，可传null跳过）
     * @return 恢复结果：1成功，-1库存Key不存在
     */
    public int restoreStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId) {
        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = SECKILL_USERS_KEY + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        Long result = redisTemplate.execute(
                stockRestoreScript,
                keys,
                quantity.toString(),
                userId != null ? userId.toString() : "");

        int resultCode = result != null ? result.intValue() : -1;

        if (resultCode == 1) {
            log.debug("Lua脚本恢复库存成功: seckillProductId={}, skuId={}, quantity={}, userId={}",
                    seckillProductId, skuId, quantity, userId);
        } else {
            log.warn("Lua脚本恢复库存失败: skuId={}, result={}", skuId, resultCode);
        }

        return resultCode;
    }

    @Override
    public void initSeckillStock(Long skuId, Integer stock) {
        String key = SECKILL_STOCK_KEY + skuId;
        redisTemplate.opsForValue().set(key, stock, STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
        log.debug("初始化秒杀库存到Redis: skuId={}, stock={}", skuId, stock);
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
        log.debug("恢复Redis库存: skuId={}, quantity={}", skuId, quantity);
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

    @Override
    public int syncRedisStockToDatabase() {
        int syncedCount = 0;

        // 获取所有秒杀库存的Redis key
        java.util.Set<String> keys = redisTemplate.keys(SECKILL_STOCK_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("没有需要同步的Redis库存");
            return 0;
        }

        for (String key : keys) {
            try {
                // 从key中提取skuId
                String skuIdStr = key.replace(SECKILL_STOCK_KEY, "");
                Long skuId = Long.parseLong(skuIdStr);

                // 获取Redis中的库存
                Object stockObj = redisTemplate.opsForValue().get(key);
                if (stockObj != null) {
                    Integer redisStock = (Integer) stockObj;

                    // 更新数据库中的库存
                    ProductSku sku = productSkuMapper.selectById(skuId);
                    if (sku != null) {
                        sku.setStock(redisStock);
                        productSkuMapper.updateById(sku);
                        syncedCount++;
                        log.debug("同步库存到数据库: skuId={}, stock={}", skuId, redisStock);
                    }
                }
            } catch (Exception e) {
                log.error("同步库存失败: key={}", key, e);
            }
        }

        log.info("同步Redis库存到数据库完成，共同步 {} 个商品", syncedCount);
        return syncedCount;
    }
}
