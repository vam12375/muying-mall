-- 释放锁的Lua脚本
-- KEYS[1]: 锁的key
-- ARGV[1]: 请求标识（用于验证锁的持有者）

-- 如果锁存在且请求标识匹配，则释放锁
if (redis.call('exists', KEYS[1]) == 1) then
    local requestId = redis.call('hget', KEYS[1], 'requestId')
    if (requestId == ARGV[1]) then
        redis.call('del', KEYS[1])
        return 1
    end
    return 0
end
return 0 