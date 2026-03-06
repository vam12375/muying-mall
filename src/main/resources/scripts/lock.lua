-- 分布式锁获取脚本
-- 使用 SET NX PX 一条命令完成原子性加锁，取代 exists + hset + pexpire 三步操作
--
-- KEYS[1]: 锁的key
-- ARGV[1]: 请求标识（用于识别锁的持有者）
-- ARGV[2]: 锁的过期时间（毫秒）
--
-- 返回值：
--   1: 获取锁成功
--   0: 锁已被其他持有者占用

local ok = redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', tonumber(ARGV[2]))
if ok then
    return 1
end
return 0
