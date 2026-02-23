package com.muyingmall.service.impl;

import com.muyingmall.entity.ProductSku;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.service.SeckillService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
 * 1. 简单模式：使用Lua脚本原子性预扣减（preDeductStock方法）
 * 2. 完整模式：使用Lua脚本保证原子性并包含用户去重检查（deductStockWithLua方法）
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

    // Lua脚本：完整库存扣减（含用户去重）
    private DefaultRedisScript<Long> stockDeductScript;
    // Lua脚本：库存恢复
    private DefaultRedisScript<Long> stockRestoreScript;
    // Lua脚本：简单预扣减（不含用户去重）
    private DefaultRedisScript<Long> stockPreDeductScript;

    /**
     * 初始化Lua脚本
     * 通过 @PostConstruct 在Bean初始化后加载，脚本会被预编译并缓存SHA1摘要
     */
    @PostConstruct
    public void initLuaScripts() {
        // 加载完整库存扣减脚本（含用户去重）
        stockDeductScript = new DefaultRedisScript<>();
        stockDeductScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_deduct.lua")));
        stockDeductScript.setResultType(Long.class);

        // 加载库存恢复脚本
        stockRestoreScript = new DefaultRedisScript<>();
        stockRestoreScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_restore.lua")));
        stockRestoreScript.setResultType(Long.class);

        // 加载简单预扣减脚本
        stockPreDeductScript = new DefaultRedisScript<>();
        stockPreDeductScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_pre_deduct.lua")));
        stockPreDeductScript.setResultType(Long.class);

        log.info("秒杀Lua脚本初始化完成：stock_deduct.lua, stock_restore.lua, stock_pre_deduct.lua");
    }

    @Override
    public int deductStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId, Long expireSeconds) {
        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = SECKILL_USERS_KEY + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        // 过期时间默认使用库存缓存过期时间（24小时），优先使用传入的活动剩余秒数
        String expireStr = String.valueOf(expireSeconds != null ? expireSeconds : STOCK_CACHE_EXPIRE);

        // 执行Lua脚本，保证库存检查、扣减、用户去重在同一原子操作内完成
        Long result = redisTemplate.execute(
                stockDeductScript,
                keys,
                quantity.toString(),
                userId != null ? userId.toString() : "",
                expireStr);

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

    @Override
    public int restoreStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId) {
        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = SECKILL_USERS_KEY + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        // 执行Lua脚本，原子性恢复库存并清除用户购买记录
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

        // 先检查库存是否存在，不存在则从数据库同步
        Object stockObj = redisTemplate.opsForValue().get(key);
        if (stockObj == null) {
            log.error("Redis库存不存在，需要初始化: skuId={}", skuId);
            syncStockToRedis(skuId);
            stockObj = redisTemplate.opsForValue().get(key);
            if (stockObj == null) {
                log.error("Redis库存同步失败: skuId={}", skuId);
                return false;
            }
        }

        // 使用Lua脚本原子性扣减库存，避免先decrement再判断再increment的竞态条件
        Long result = redisTemplate.execute(
                stockPreDeductScript,
                Collections.singletonList(key),
                quantity.toString());

        if (result == null || result < 0) {
            log.debug("Redis预减库存失败，库存不足: skuId={}, quantity={}, result={}",
                    skuId, quantity, result);
            return false;
        }

        log.debug("Redis预减库存成功: skuId={}, quantity={}, remaining={}",
                skuId, quantity, result);
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
        String pattern = SECKILL_STOCK_KEY + "*";

        // 使用SCAN命令增量迭代，避免KEYS命令阻塞Redis
        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(100).build())) {

            while (cursor.hasNext()) {
                String key = cursor.next();
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
        }

        log.info("同步Redis库存到数据库完成，共同步 {} 个商品", syncedCount);
        return syncedCount;
    }
}
