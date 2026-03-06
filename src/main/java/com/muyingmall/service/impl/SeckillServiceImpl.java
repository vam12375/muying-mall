package com.muyingmall.service.impl;

import com.muyingmall.entity.ProductSku;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.mapper.SeckillProductMapper;
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
 * 1. 预扣减模式：返回剩余库存（preDeductStock方法）
 * 2. 扣减模式：返回状态码（deductStockWithLua方法），支持Key不存在时自动从DB同步重试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductSkuMapper productSkuMapper;
    private final SeckillProductMapper seckillProductMapper;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final long STOCK_CACHE_EXPIRE = 24 * 60 * 60; // 24小时

    private DefaultRedisScript<Long> stockDeductScript;
    private DefaultRedisScript<Long> stockRestoreScript;
    private DefaultRedisScript<Long> stockPreDeductScript;

    /**
     * 初始化Lua脚本
     * 通过 @PostConstruct 在Bean初始化后加载，脚本会被预编译并缓存SHA1摘要
     */
    @PostConstruct
    public void initLuaScripts() {
        stockDeductScript = new DefaultRedisScript<>();
        stockDeductScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_deduct.lua")));
        stockDeductScript.setResultType(Long.class);

        stockRestoreScript = new DefaultRedisScript<>();
        stockRestoreScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_restore.lua")));
        stockRestoreScript.setResultType(Long.class);

        stockPreDeductScript = new DefaultRedisScript<>();
        stockPreDeductScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("scripts/stock_pre_deduct.lua")));
        stockPreDeductScript.setResultType(Long.class);

        log.info("秒杀Lua脚本初始化完成：stock_deduct.lua, stock_restore.lua, stock_pre_deduct.lua");
    }

    @Override
    public int deductStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId, Long expireSeconds) {
        if (quantity == null || quantity <= 0) {
            log.warn("Lua脚本扣减库存参数非法: seckillProductId={}, skuId={}, quantity={}, userId={}",
                    seckillProductId, skuId, quantity, userId);
            return -4;
        }

        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = "seckill:user:" + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        Long result;
        if (expireSeconds != null && expireSeconds > 0) {
            result = redisTemplate.execute(stockDeductScript, keys, quantity, userId, expireSeconds);
        } else {
            result = redisTemplate.execute(stockDeductScript, keys, quantity, userId);
        }

        int resultCode = result != null ? result.intValue() : -3;

        if (resultCode == -3) {
            log.warn("Lua脚本扣减库存失败，库存Key不存在，尝试从秒杀商品表同步: seckillProductId={}, skuId={}", seckillProductId, skuId);
            syncSeckillStockToRedis(seckillProductId);
            if (expireSeconds != null && expireSeconds > 0) {
                result = redisTemplate.execute(stockDeductScript, keys, quantity, userId, expireSeconds);
            } else {
                result = redisTemplate.execute(stockDeductScript, keys, quantity, userId);
            }
            resultCode = result != null ? result.intValue() : -3;
        }

        switch (resultCode) {
            case 1:
                log.debug("Lua脚本扣减库存成功: seckillProductId={}, skuId={}, quantity={}, userId={}",
                        seckillProductId, skuId, quantity, userId);
                break;
            case -1:
                log.debug("Lua脚本扣减库存失败，库存不足: skuId={}, quantity={}", skuId, quantity);
                break;
            case -2:
                log.debug("Lua脚本扣减库存失败，用户已参与: seckillProductId={}, skuId={}, userId={}", seckillProductId, skuId, userId);
                break;
            case -3:
                log.warn("Lua脚本扣减库存失败，同步后库存Key仍不存在: skuId={}", skuId);
                break;
            case -4:
                log.warn("Lua脚本扣减库存失败，参数非法: skuId={}, quantity={}", skuId, quantity);
                break;
            default:
                log.error("Lua脚本扣减库存返回未知结果: skuId={}, result={}", skuId, resultCode);
        }

        return resultCode;
    }

    @Override
    public int restoreStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId) {
        if (quantity == null || quantity <= 0) {
            log.warn("Lua脚本恢复库存参数非法: seckillProductId={}, skuId={}, quantity={}, userId={}",
                    seckillProductId, skuId, quantity, userId);
            return -4;
        }

        String stockKey = SECKILL_STOCK_KEY + skuId;
        String userSetKey = "seckill:user:" + seckillProductId;
        List<String> keys = Arrays.asList(stockKey, userSetKey);

        Long result = redisTemplate.execute(
                stockRestoreScript,
                keys,
                quantity, userId);

        if (result != null && result >= 0) {
            log.debug("Lua脚本恢复库存成功: seckillProductId={}, skuId={}, quantity={}, userId={}, newStock={}",
                    seckillProductId, skuId, quantity, userId, result);
            return 1;
        }

        int resultCode = result != null ? result.intValue() : -1;
        log.warn("Lua脚本恢复库存失败: skuId={}, result={}", skuId, resultCode);
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
        // 参数兜底校验：避免 quantity 非法导致 Lua 中 tonumber 返回 nil。
        if (quantity == null || quantity <= 0) {
            log.warn("Redis预扣减参数非法: skuId={}, quantity={}", skuId, quantity);
            return false;
        }

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

        // 关键修复：传入数值对象，避免数量参数被序列化为带引号字符串而导致 Lua 解析失败。
        Long result = redisTemplate.execute(
                stockPreDeductScript,
                Collections.singletonList(key),
                quantity);

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
        if (quantity == null || quantity <= 0) {
            log.warn("恢复Redis库存参数非法: skuId={}, quantity={}", skuId, quantity);
            return;
        }

        String key = SECKILL_STOCK_KEY + skuId;
        Long result = redisTemplate.execute(
                stockRestoreScript,
                Collections.singletonList(key),
                quantity);

        if (result != null && result >= 0) {
            log.debug("恢复Redis库存成功: skuId={}, quantity={}, newStock={}", skuId, quantity, result);
        } else {
            log.warn("恢复Redis库存失败(key不存在)，尝试从数据库同步: skuId={}, result={}", skuId, result);
            syncStockToRedis(skuId);
        }
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
    public void syncSeckillStockToRedis(Long seckillProductId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
        if (sp != null) {
            initSeckillStock(sp.getSkuId(), sp.getSeckillStock());
            log.info("从秒杀商品表同步库存到Redis: seckillProductId={}, skuId={}, stock={}",
                    seckillProductId, sp.getSkuId(), sp.getSeckillStock());
        } else {
            log.warn("秒杀商品不存在，无法同步库存: seckillProductId={}", seckillProductId);
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
