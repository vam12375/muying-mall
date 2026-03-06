-- stock_restore.lua
-- 秒杀库存原子性恢复脚本（含用户去重清理）
-- 功能：参数校验 → 恢复库存 → 移除用户去重记录
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- KEYS[2]: 用户去重集合Key，如 seckill:user:{seckillProductId}（可选）
-- ARGV[1]: 恢复数量
-- ARGV[2]: 用户ID（可选，与KEYS[2]配合使用）
--
-- 返回值：
--   恢复后的库存值（>= 0）: 恢复成功
--  -1: 库存Key不存在
--  -4: 参数非法（恢复数量无法解析或 <= 0）

local stockKey = KEYS[1]
local restoreNum = tonumber(ARGV[1])

if (restoreNum == nil or restoreNum <= 0) then
    return -4
end

if (redis.call('exists', stockKey) == 0) then
    return -1
end

local newStock = redis.call('incrby', stockKey, restoreNum)

-- 移除用户去重记录（当提供用户集合Key和用户ID时生效）
if #KEYS >= 2 and ARGV[2] then
    redis.call('srem', KEYS[2], tostring(ARGV[2]))
end

return newStock
