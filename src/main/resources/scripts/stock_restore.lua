-- stock_restore.lua
-- 秒杀库存原子性恢复脚本
-- 功能：恢复库存（用于订单取消、支付失败等场景）
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- KEYS[2]: 用户购买记录Set Key，如 seckill:users:{seckillProductId}
-- ARGV[1]: 恢复数量
-- ARGV[2]: 用户ID（可选，用于清除购买记录）
--
-- 返回值：
--   1: 恢复成功
--  -1: 库存Key不存在

local stockKey = KEYS[1]
local userSetKey = KEYS[2]
local restoreNum = tonumber(ARGV[1])
local userId = ARGV[2]

-- 检查库存Key是否存在
if (redis.call('exists', stockKey) == 0) then
    return -1
end

-- 执行库存恢复
redis.call('incrby', stockKey, restoreNum)

-- 可选：从Set中移除用户（允许用户重新购买）
if (userId ~= nil and userId ~= '' and userSetKey ~= nil and userSetKey ~= '') then
    redis.call('srem', userSetKey, userId)
end

-- 返回成功
return 1
