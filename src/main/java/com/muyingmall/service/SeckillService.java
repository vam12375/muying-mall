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
}
