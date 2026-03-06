-- stock_deduct.lua
-- 秒杀库存原子性扣减脚本（含用户去重）
-- 功能：参数校验 → 用户去重检查 → 库存检查 → 扣减 → 记录用户
--
-- KEYS[1]: 库存Key，如 seckill:stock:{skuId}
-- KEYS[2]: 用户去重集合Key，如 seckill:user:{seckillProductId}（可选）
-- ARGV[1]: 扣减数量
-- ARGV[2]: 用户ID（可选，与KEYS[2]配合使用）
-- ARGV[3]: 用户集合过期时间/秒（可选）
--
-- 返回值：
--   1: 扣减成功
--  -1: 库存不足
--  -2: 用户已参与过秒杀
--  -3: 库存Key不存在
--  -4: 参数非法（扣减数量无法解析或 <= 0）

local stockKey = KEYS[1]
local deductNum = tonumber(ARGV[1])

if (deductNum == nil or deductNum <= 0) then
    return -4
end

if (redis.call('exists', stockKey) == 0) then
    return -3
end

-- 用户去重检查（当提供用户集合Key和用户ID时生效）
if #KEYS >= 2 and ARGV[2] then
    local userSetKey = KEYS[2]
    local userId = tostring(ARGV[2])
    if redis.call('sismember', userSetKey, userId) == 1 then
        return -2
    end
end

local currentStock = tonumber(redis.call('get', stockKey))

if (currentStock == nil or currentStock < deductNum) then
    return -1
end

redis.call('decrby', stockKey, deductNum)

-- 记录用户到去重集合
if #KEYS >= 2 and ARGV[2] then
    redis.call('sadd', KEYS[2], tostring(ARGV[2]))
    if ARGV[3] then
        local ttl = tonumber(ARGV[3])
        if ttl and ttl > 0 then
            redis.call('expire', KEYS[2], ttl)
        end
    end
end

return 1
