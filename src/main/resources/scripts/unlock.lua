-- 分布式锁释放脚本
-- 与 lock.lua 配套，锁数据使用 String 类型存储 requestId
-- 验证持有者身份后才删除，防止误删其他线程的锁
--
-- KEYS[1]: 锁的key
-- ARGV[1]: 请求标识（用于验证锁的持有者）
--
-- 返回值：
--   1: 释放成功
--   0: 锁不存在或不属于当前请求者

if (redis.call('get', KEYS[1]) == ARGV[1]) then
    redis.call('del', KEYS[1])
    return 1
end
return 0
