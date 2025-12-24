-- stock_deduct.lua
-- 秒杀库存原子性扣减脚本
-- 功能：检查库存并扣减，保证操作的原子性，防止超卖
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- ARGV[1]: 扣减数量
-- ARGV[2]: 用户ID（用于防重复购买检查，可选）
-- ARGV[3]: 用户购买记录Key前缀（可选）
--
-- 返回值：
--   1: 扣减成功
--  -1: 库存不足
--  -2: 用户已购买过（防刷单）
--  -3: 库存Key不存在

local stockKey = KEYS[1]
local deductNum = tonumber(ARGV[1])
local userId = ARGV[2]
local userBuyKey = ARGV[3]

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

-- 可选：检查用户是否已购买（防刷单）
if (userId ~= nil and userId ~= '' and userBuyKey ~= nil and userBuyKey ~= '') then
    local userKey = userBuyKey .. userId
    if (redis.call('exists', userKey) == 1) then
        return -2
    end
    -- 标记用户已购买，设置过期时间24小时
    redis.call('setex', userKey, 86400, '1')
end

-- 执行库存扣减
local newStock = redis.call('decrby', stockKey, deductNum)

-- 返回成功
return 1
