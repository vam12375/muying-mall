package com.muyingmall.service.impl;

import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.mapper.SeckillActivityMapper;
import com.muyingmall.mapper.SeckillOrderMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.SeckillService;
import com.muyingmall.service.SeckillStockTxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 秒杀订单服务规则测试。
 * 目标：确保“仅支付成功计入成功次数”以及“存在待支付订单时禁止重复占位”规则被严格执行。
 */
@ExtendWith(MockitoExtension.class)
class SeckillOrderServiceImplTest {

    @Mock
    private SeckillService seckillService;

    @Mock
    private SeckillProductMapper seckillProductMapper;

    @Mock
    private SeckillActivityMapper seckillActivityMapper;

    @Mock
    private SeckillOrderMapper seckillOrderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private SeckillStockTxService seckillStockTxService;

    private SeckillOrderServiceImpl seckillOrderService;

    @BeforeEach
    void setUp() {
        // 直接构造被测对象，所有外部依赖均由Mockito替身控制。
        seckillOrderService = new SeckillOrderServiceImpl(
                seckillService,
                seckillProductMapper,
                seckillActivityMapper,
                seckillOrderMapper,
                orderService,
                seckillStockTxService);
    }

    @Test
    void executeSeckill_shouldRejectWhenPaidQuantityReachedLimit() {
        SeckillProduct seckillProduct = buildSeckillProduct(96L, 11L, 66L, 2);
        SeckillActivity activity = buildActiveActivity(66L);
        SeckillRequestDTO request = buildRequest(96L, 1L, 2020L);

        when(seckillProductMapper.selectById(96L)).thenReturn(seckillProduct);
        when(seckillActivityMapper.selectById(66L)).thenReturn(activity);
        // 模拟该用户在当前秒杀商品下已支付2件，达到当前商品限购上限。
        when(seckillOrderMapper.countUserPurchase(9, 66L, 96L)).thenReturn(2);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> seckillOrderService.executeSeckill(9, request));

        assertTrue(exception.getMessage().contains("已达上限"));
        // 达到上限应在库存扣减前拦截，避免无意义的Redis/Lua开销。
        verify(seckillService, never()).deductStockWithLua(96L, 11L, 1, 9, null);
    }

    @Test
    void canUserParticipate_shouldBeFalseWhenPaidQuantityReachedLimit() {
        SeckillProduct seckillProduct = buildSeckillProduct(96L, 11L, 66L, 2);

        when(seckillProductMapper.selectById(96L)).thenReturn(seckillProduct);
        // 商品限购为2件时，用户已支付2件，应判定不可继续参与。
        when(seckillOrderMapper.countUserPurchase(9, 66L, 96L)).thenReturn(2);
        when(seckillOrderMapper.countUserPendingPurchase(9, 66L, 96L)).thenReturn(0);

        boolean canParticipate = seckillOrderService.canUserParticipate(9, 96L);

        assertFalse(canParticipate);
    }

    @Test
    void executeSeckill_shouldRejectWhenPendingOrderExists() {
        SeckillProduct seckillProduct = buildSeckillProduct(96L, 11L, 66L, 5);
        SeckillActivity activity = buildActiveActivity(66L);
        SeckillRequestDTO request = buildRequest(96L, 1L, 2020L);

        when(seckillProductMapper.selectById(96L)).thenReturn(seckillProduct);
        when(seckillActivityMapper.selectById(66L)).thenReturn(activity);
        when(seckillOrderMapper.countUserPurchase(9, 66L, 96L)).thenReturn(0);
        // 模拟存在待支付秒杀订单，必须拦截，防止无限占位下单。
        when(seckillOrderMapper.countUserPendingPurchase(9, 66L, 96L)).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> seckillOrderService.executeSeckill(9, request));

        assertTrue(exception.getMessage().contains("待支付"));
        verify(seckillService, never()).deductStockWithLua(96L, 11L, 1, 9, null);
    }

    private SeckillProduct buildSeckillProduct(Long id, Long skuId, Long activityId, Integer limitPerUser) {
        SeckillProduct seckillProduct = new SeckillProduct();
        seckillProduct.setId(id);
        seckillProduct.setSkuId(skuId);
        seckillProduct.setActivityId(activityId);
        seckillProduct.setLimitPerUser(limitPerUser);
        return seckillProduct;
    }

    private SeckillRequestDTO buildRequest(Long seckillProductId, Long quantity, Long addressId) {
        SeckillRequestDTO request = new SeckillRequestDTO();
        request.setSeckillProductId(seckillProductId);
        request.setQuantity(quantity.intValue());
        request.setAddressId(addressId);
        return request;
    }

    private SeckillActivity buildActiveActivity(Long activityId) {
        SeckillActivity activity = new SeckillActivity();
        activity.setId(activityId);
        activity.setStatus(1);
        activity.setStartTime(LocalDateTime.now().minusMinutes(10));
        activity.setEndTime(LocalDateTime.now().plusMinutes(10));
        return activity;
    }
}
