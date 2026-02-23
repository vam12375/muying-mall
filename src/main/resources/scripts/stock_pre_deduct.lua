-- stock_pre_deduct.lua
-- 简单模式库存预扣减脚本（不含用户去重检查）
-- 功能：原子性检查并扣减库存，防止超卖
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- ARGV[1]: 扣减数量
--
-- 返回值：
--   >= 0: 扣减成功，返回剩余库存
--   -1: 库存不足
--   -3: 库存Key不存在

local stockKey = KEYS[1]
local deductNum = tonumber(ARGV[1])

-- 检查库存Key是否存在
if (redis.call('exists', stockKey) == 0) then
    return -3
end

-- 获取当前库存
local currentStock = tonumber(redis.call('get', stockKey))

-- 检查库存是否充足
if (currentStock == nil or currentStock < deductNum) then
    return -1
end

-- 执行库存扣减并返回剩余库存
local newStock = redis.call('decrby', stockKey, deductNum)
return newStock
