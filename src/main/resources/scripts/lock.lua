-- 获取锁的Lua脚本
-- KEYS[1]: 锁的key
-- ARGV[1]: 请求标识（用于识别锁的持有者）
-- ARGV[2]: 锁的过期时间（毫秒）

-- 如果锁不存在，则获取锁并设置过期时间
if (redis.call('exists', KEYS[1]) == 0) then
    redis.call('hset', KEYS[1], 'requestId', ARGV[1])
    redis.call('pexpire', KEYS[1], ARGV[2])
    return 1
end
return 0 