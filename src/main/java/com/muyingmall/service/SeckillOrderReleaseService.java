package com.muyingmall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.entity.SeckillOrder;
import com.muyingmall.mapper.SeckillOrderMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 秒杀订单释放服务。
 * 用于在订单取消/超时时，统一释放秒杀占用资源（秒杀库存与秒杀资格）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderReleaseService {

    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillService seckillService;

    /**
     * 释放待支付秒杀订单占用资源。
     * 仅当秒杀订单状态为“待支付(0)”时执行，保证幂等。
     *
     * @param orderId 业务订单ID
     * @param scene   调用场景（日志追踪）
     */
    @Transactional(rollbackFor = Exception.class)
    public void releasePendingSeckillOrder(Integer orderId, String scene) {
        if (orderId == null) {
            return;
        }

        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getOrderId, orderId));

        // 非秒杀订单直接返回，不影响普通订单流程。
        if (seckillOrder == null) {
            return;
        }

        // 已支付订单不释放资格，符合“支付成功才算秒杀成功”的规则。
        if (Objects.equals(seckillOrder.getStatus(), 1)) {
            log.info("跳过释放：秒杀订单已支付，保持成功记录。scene={}, orderId={}, seckillOrderId={}",
                    scene, orderId, seckillOrder.getId());
            return;
        }

        // 已取消订单直接返回，避免重复回补库存。
        if (Objects.equals(seckillOrder.getStatus(), 2)) {
            return;
        }

        // 原子更新状态，确保只有一次线程能执行后续库存回补。
        int changed = seckillOrderMapper.updateStatusToCancelledIfPending(Long.valueOf(orderId));
        if (changed <= 0) {
            log.info("秒杀订单状态已变化，跳过重复释放。scene={}, orderId={}, seckillOrderId={}, status={}",
                    scene, orderId, seckillOrder.getId(), seckillOrder.getStatus());
            return;
        }

        // 回补秒杀库（数据库）。
        int restoreRows = seckillProductMapper.restoreStock(
                seckillOrder.getSeckillProductId(),
                seckillOrder.getQuantity());
        if (restoreRows <= 0) {
            throw new IllegalStateException("回补秒杀数据库库存失败: orderId=" + orderId);
        }

        // 回补秒杀库（Redis/Lua），同时兼容历史版本用户去重集合清理。
        int restoreLuaResult = seckillService.restoreStockWithLua(
                seckillOrder.getSeckillProductId(),
                seckillOrder.getSkuId(),
                seckillOrder.getQuantity(),
                seckillOrder.getUserId());

        // Redis key 可能过期，先重建再重试一次，尽量保证释放成功。
        if (restoreLuaResult != 1) {
            seckillService.syncSeckillStockToRedis(seckillOrder.getSeckillProductId());
            restoreLuaResult = seckillService.restoreStockWithLua(
                    seckillOrder.getSeckillProductId(),
                    seckillOrder.getSkuId(),
                    seckillOrder.getQuantity(),
                    seckillOrder.getUserId());
        }

        if (restoreLuaResult != 1) {
            // 降级处理：Redis库存回补失败不阻塞秒杀订单状态更新，避免事务回滚导致用户永久锁定。
            // Redis库存偏差可通过管理端手动同步或下次初始化时自动修正。
            log.error("回补秒杀Redis库存失败（已降级，不回滚）: orderId={}, seckillProductId={}, skuId={}, result={}",
                    orderId, seckillOrder.getSeckillProductId(), seckillOrder.getSkuId(), restoreLuaResult);
        }

        log.info("秒杀订单释放成功: scene={}, orderId={}, seckillOrderId={}, seckillProductId={}, quantity={}",
                scene, orderId, seckillOrder.getId(), seckillOrder.getSeckillProductId(), seckillOrder.getQuantity());
    }
}
