package com.muyingmall.service;

import com.muyingmall.mapper.SeckillProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀库存事务服务。
 *
 * <p>
 * 将秒杀库存数据库操作放入独立短事务中，避免在创建订单、扣减SKU库存、发送消息等耗时流程中
 * 持有 seckill_product 行锁过久，降低高并发下的锁等待超时概率。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillStockTxService {

    private final SeckillProductMapper seckillProductMapper;

    /**
     * 在独立事务中扣减秒杀数据库库存。
     *
     * @param seckillProductId 秒杀商品ID
     * @param quantity         扣减数量
     * @return 影响行数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int deductStock(Long seckillProductId, Integer quantity) {
        int rows = seckillProductMapper.deductStock(seckillProductId, quantity);
        log.debug("独立事务扣减秒杀数据库库存: seckillProductId={}, quantity={}, rows={}", seckillProductId, quantity, rows);
        return rows;
    }

    /**
     * 在独立事务中回补秒杀数据库库存。
     *
     * @param seckillProductId 秒杀商品ID
     * @param quantity         回补数量
     * @return 影响行数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int restoreStock(Long seckillProductId, Integer quantity) {
        int rows = seckillProductMapper.restoreStock(seckillProductId, quantity);
        log.debug("独立事务回补秒杀数据库库存: seckillProductId={}, quantity={}, rows={}", seckillProductId, quantity, rows);
        return rows;
    }
}
