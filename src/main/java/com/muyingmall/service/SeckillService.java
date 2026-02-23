package com.muyingmall.service;

/**
 * 秒杀服务接口
 */
public interface SeckillService {

    /**
     * 初始化秒杀库存到Redis
     * 
     * @param skuId SKU ID
     * @param stock 库存数量
     */
    void initSeckillStock(Long skuId, Integer stock);

    /**
     * Redis预减库存
     * 
     * @param skuId SKU ID
     * @param quantity 购买数量
     * @return true表示预减成功，false表示库存不足
     */
    boolean preDeductStock(Long skuId, Integer quantity);

    /**
     * 恢复Redis库存（订单取消时）
     * 
     * @param skuId SKU ID
     * @param quantity 恢复数量
     */
    void restoreRedisStock(Long skuId, Integer quantity);

    /**
     * 获取Redis中的库存
     * 
     * @param skuId SKU ID
     * @return 库存数量，不存在返回null
     */
    Integer getRedisStock(Long skuId);

    /**
     * 同步数据库库存到Redis
     * 
     * @param skuId SKU ID
     */
    void syncStockToRedis(Long skuId);
    
    /**
     * 同步Redis库存到数据库
     *
     * @return 同步的商品数量
     */
    int syncRedisStockToDatabase();

    /**
     * 使用Lua脚本原子性扣减库存（推荐方式，包含用户防重复购买检查）
     *
     * @param seckillProductId 秒杀商品ID
     * @param skuId            SKU ID
     * @param quantity         扣减数量
     * @param userId           用户ID（用于防重复购买，可传null跳过检查）
     * @param expireSeconds    用户购买记录过期时间（秒），与活动结束时间对齐，传null则不设置过期
     * @return 扣减结果：1成功，-1库存不足，-2用户已购买，-3库存Key不存在
     */
    int deductStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId, Long expireSeconds);

    /**
     * 使用Lua脚本原子性恢复库存
     *
     * @param seckillProductId 秒杀商品ID
     * @param skuId            SKU ID
     * @param quantity         恢复数量
     * @param userId           用户ID（用于清除购买记录，可传null跳过）
     * @return 恢复结果：1成功，-1库存Key不存在
     */
    int restoreStockWithLua(Long seckillProductId, Long skuId, Integer quantity, Integer userId);
}
