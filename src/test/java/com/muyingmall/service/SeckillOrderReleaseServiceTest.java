package com.muyingmall.service;

import com.muyingmall.entity.SeckillOrder;
import com.muyingmall.mapper.SeckillOrderMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 秒杀订单释放服务测试。
 * 目标：保证超时/取消只释放待支付秒杀订单，且释放流程包含数据库与Redis库存回补。
 */
@ExtendWith(MockitoExtension.class)
class SeckillOrderReleaseServiceTest {

    @Mock
    private SeckillOrderMapper seckillOrderMapper;

    @Mock
    private SeckillProductMapper seckillProductMapper;

    @Mock
    private SeckillService seckillService;

    private SeckillOrderReleaseService releaseService;

    @BeforeEach
    void setUp() {
        releaseService = new SeckillOrderReleaseService(seckillOrderMapper, seckillProductMapper, seckillService);
    }

    @Test
    void releasePendingSeckillOrder_shouldRestoreStocksAndMarkCancelled() {
        SeckillOrder pendingOrder = buildSeckillOrder(1001L, 9, 96L, 11L, 2, 0);

        when(seckillOrderMapper.selectOne(any())).thenReturn(pendingOrder);
        when(seckillOrderMapper.updateStatusToCancelledIfPending(1001L)).thenReturn(1);
        when(seckillProductMapper.restoreStock(96L, 2)).thenReturn(1);
        when(seckillService.restoreStockWithLua(96L, 11L, 2, 9)).thenReturn(1);

        releaseService.releasePendingSeckillOrder(1001, "UNIT_TEST");

        verify(seckillOrderMapper).updateStatusToCancelledIfPending(1001L);
        verify(seckillProductMapper).restoreStock(96L, 2);
        verify(seckillService).restoreStockWithLua(96L, 11L, 2, 9);
    }

    @Test
    void releasePendingSeckillOrder_shouldSkipWhenAlreadyPaid() {
        SeckillOrder paidOrder = buildSeckillOrder(1002L, 9, 96L, 11L, 1, 1);
        when(seckillOrderMapper.selectOne(any())).thenReturn(paidOrder);

        releaseService.releasePendingSeckillOrder(1002, "UNIT_TEST");

        // 已支付秒杀订单不应释放库存，也不应改为取消状态。
        verify(seckillOrderMapper, never()).updateStatusToCancelledIfPending(eq(1002L));
        verify(seckillProductMapper, never()).restoreStock(any(), any());
        verify(seckillService, never()).restoreStockWithLua(any(), any(), any(), any());
    }

    private SeckillOrder buildSeckillOrder(
            Long orderId,
            Integer userId,
            Long seckillProductId,
            Long skuId,
            Integer quantity,
            Integer status
    ) {
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(orderId);
        seckillOrder.setUserId(userId);
        seckillOrder.setSeckillProductId(seckillProductId);
        seckillOrder.setSkuId(skuId);
        seckillOrder.setQuantity(quantity);
        seckillOrder.setStatus(status);
        return seckillOrder;
    }
}
