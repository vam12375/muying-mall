package com.muyingmall.service.impl;

import com.muyingmall.mapper.ProductSkuMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SeckillServiceImpl 参数传递测试。
 * 目的：确保 Lua 脚本收到的是可被 tonumber 正确解析的数值参数，避免出现 number 与 nil 比较异常。
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private SeckillProductMapper seckillProductMapper;

    @Mock
    private DefaultRedisScript<Long> stockDeductScript;

    @Mock
    private DefaultRedisScript<Long> stockRestoreScript;

    @Mock
    private DefaultRedisScript<Long> stockPreDeductScript;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private SeckillServiceImpl seckillService;

    @BeforeEach
    void setUp() {
        // 直接构造被测对象，并通过反射注入 Lua 脚本对象，避免依赖 @PostConstruct。
        seckillService = new SeckillServiceImpl(redisTemplate, productSkuMapper, seckillProductMapper);
        ReflectionTestUtils.setField(seckillService, "stockDeductScript", stockDeductScript);
        ReflectionTestUtils.setField(seckillService, "stockRestoreScript", stockRestoreScript);
        ReflectionTestUtils.setField(seckillService, "stockPreDeductScript", stockPreDeductScript);
    }

    @Test
    void deductStockWithLua_shouldPassNumericQuantityToLuaScript() {
        when(redisTemplate.execute(eq(stockDeductScript), anyList(), any())).thenReturn(1L);

        seckillService.deductStockWithLua(96L, 11L, 1, 9, 120L);

        verify(redisTemplate).execute(
                eq(stockDeductScript),
                eq(Collections.singletonList("seckill:stock:11")),
                eq(1)
        );
    }

    @Test
    void preDeductStock_shouldPassNumericQuantityToLuaScript() {
        // 提供 Redis 库存，确保流程进入 Lua 预扣减分支，不走同步数据库分支。
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seckill:stock:11")).thenReturn(100);
        when(redisTemplate.execute(eq(stockPreDeductScript), anyList(), any())).thenReturn(99L);

        seckillService.preDeductStock(11L, 1);

        // quantity 必须以数值对象传递，确保 Lua 中 tonumber(ARGV[1]) 可解析。
        verify(redisTemplate).execute(
                eq(stockPreDeductScript),
                eq(Collections.singletonList("seckill:stock:11")),
                eq(1)
        );
    }

    @Test
    void restoreStockWithLua_shouldPassNumericQuantityToLuaScript() {
        when(redisTemplate.execute(eq(stockRestoreScript), anyList(), any())).thenReturn(10L);

        seckillService.restoreStockWithLua(96L, 11L, 1, 9);

        verify(redisTemplate).execute(
                eq(stockRestoreScript),
                eq(Collections.singletonList("seckill:stock:11")),
                eq(1)
        );
    }
}
