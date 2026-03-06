-- stock_pre_deduct.lua
-- 库存预扣减脚本
-- 功能：原子性检查并扣减库存，成功时返回剩余库存，防止超卖
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- ARGV[1]: 扣减数量
--
-- 返回值：
--   >= 0: 扣减成功，返回剩余库存
--   -1: 库存不足
--   -3: 库存Key不存在
--   -4: 参数非法（扣减数量无法解析或 <= 0）

local stockKey = KEYS[1]
local deductNum = tonumber(ARGV[1])

if (deductNum == nil or deductNum <= 0) then
    return -4
end

if (redis.call('exists', stockKey) == 0) then
    return -3
end

local currentStock = tonumber(redis.call('get', stockKey))

if (currentStock == nil or currentStock < deductNum) then
    return -1
end

return redis.call('decrby', stockKey, deductNum)
