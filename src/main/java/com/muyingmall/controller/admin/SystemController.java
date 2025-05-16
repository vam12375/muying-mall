package com.muyingmall.controller.admin;

import com.muyingmall.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统管理控制器
 * 提供系统监控、缓存管理等功能
 */
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final RedisTemplate<String, Object> redisTemplate;

    // 注入专用于处理Spring Session数据的RedisTemplate
    private final RedisTemplate<String, Object> sessionRedisTemplate;

    @Qualifier("redisConnectionFactoryDb1")
    private final RedisConnectionFactory redisConnectionFactory;

    // Spring Session的键名前缀，用于识别Session相关的键
    private static final String SESSION_KEY_PREFIX = "spring:session";

    /**
     * 获取Redis服务器信息
     */
    @GetMapping("/redis/info")
    public Result<Map<String, Object>> getRedisInfo() {
        try {
            // 使用RedisCallback获取Redis服务器信息
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> {
                Properties properties = connection.info();
                properties.putAll(connection.info("server"));
                properties.putAll(connection.info("clients"));
                properties.putAll(connection.info("memory"));
                properties.putAll(connection.info("stats"));
                return properties;
            });

            if (info == null) {
                return Result.error("无法获取Redis信息");
            }

            Map<String, Object> redisInfo = new HashMap<>();

            // 提取关键信息
            redisInfo.put("version", info.getProperty("redis_version"));
            redisInfo.put("mode", info.getProperty("redis_mode"));
            redisInfo.put("os", info.getProperty("os"));
            redisInfo.put("connectedClients", info.getProperty("connected_clients"));
            redisInfo.put("uptime", info.getProperty("uptime_in_seconds"));
            redisInfo.put("uptimeInDays", info.getProperty("uptime_in_days"));
            redisInfo.put("usedMemory", info.getProperty("used_memory"));
            redisInfo.put("usedMemoryHuman", info.getProperty("used_memory_human"));
            redisInfo.put("usedMemoryPeakHuman", info.getProperty("used_memory_peak_human"));
            redisInfo.put("totalCommands", info.getProperty("total_commands_processed"));
            redisInfo.put("keyspaceHits", info.getProperty("keyspace_hits"));
            redisInfo.put("keyspaceMisses", info.getProperty("keyspace_misses"));
            redisInfo.put("keyspaceHitRate", calculateHitRate(info));

            // 获取键统计信息
            Long totalKeys = redisTemplate
                    .execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
            redisInfo.put("totalKeys", totalKeys);

            // 获取数据库统计信息
            String dbStats = info.getProperty("db0");
            if (dbStats != null) {
                Map<String, Object> keyspaceStats = new HashMap<>();
                Map<String, Object> db0Stats = new HashMap<>();

                String[] stats = dbStats.split(",");
                for (String stat : stats) {
                    String[] keyValue = stat.split("=");
                    if (keyValue.length == 2) {
                        db0Stats.put(keyValue[0], keyValue[1]);
                    }
                }
                keyspaceStats.put("db0", db0Stats);
                redisInfo.put("keyspaceStats", keyspaceStats);
            }

            return Result.success(redisInfo);
        } catch (Exception e) {
            log.error("获取Redis信息失败", e);
            return Result.error("获取Redis信息失败: " + e.getMessage());
        }
    }

    /**
     * 计算缓存命中率
     */
    private String calculateHitRate(Properties info) {
        String hitsStr = info.getProperty("keyspace_hits");
        String missesStr = info.getProperty("keyspace_misses");

        if (hitsStr == null || missesStr == null) {
            return "0%";
        }

        long hits = Long.parseLong(hitsStr);
        long misses = Long.parseLong(missesStr);
        long total = hits + misses;

        if (total == 0) {
            return "0%";
        }

        double hitRate = (double) hits / total * 100;
        return String.format("%.2f%%", hitRate);
    }

    /**
     * 获取Redis缓存键列表
     */
    @GetMapping("/redis/keys")
    public Result<Map<String, Object>> getRedisCacheKeys(
            @RequestParam(value = "pattern", defaultValue = "*") String pattern,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        try {
            // 安全检查
            if (pattern.contains(" ") || pattern.contains("\n")) {
                return Result.error("模式中包含无效字符");
            }

            // 使用scan命令获取匹配的键
            Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<String> keySet = new HashSet<>();
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
                try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                    while (cursor.hasNext()) {
                        keySet.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    log.error("扫描键失败", e);
                }
                return keySet;
            });

            if (keys == null) {
                return Result.error("获取键列表失败");
            }

            // 计算分页
            List<String> keyList = new ArrayList<>(keys);
            keyList.sort(String::compareTo);

            int total = keyList.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);

            if (start >= total) {
                start = 0;
                end = Math.min(size, total);
            }

            List<String> pageKeys = (start < end) ? keyList.subList(start, end) : Collections.emptyList();

            // 获取每个键的类型和其他信息
            List<Map<String, Object>> items = pageKeys.stream().map(key -> {
                Map<String, Object> keyInfo = new HashMap<>();
                keyInfo.put("key", key);

                try {
                    // 根据键名选择合适的RedisTemplate
                    RedisTemplate<String, Object> template = isSessionKey(key) ? sessionRedisTemplate : redisTemplate;

                    // 获取键类型
                    String type = Objects.requireNonNull(template.type(key)).name().toLowerCase();
                    keyInfo.put("type", type);

                    // 获取TTL
                    Long ttl = template.getExpire(key);
                    keyInfo.put("ttl", ttl);

                    // 获取大小（大致估算）
                    long keySize = 0;
                    switch (type) {
                        case "string":
                            Object value = template.opsForValue().get(key);
                            keySize = (value != null) ? value.toString().length() : 0;
                            break;
                        case "list":
                            keySize = template.opsForList().size(key) == null ? 0
                                    : template.opsForList().size(key);
                            break;
                        case "hash":
                            keySize = template.opsForHash().size(key) == null ? 0
                                    : template.opsForHash().size(key);
                            break;
                        case "set":
                            keySize = template.opsForSet().size(key) == null ? 0
                                    : template.opsForSet().size(key);
                            break;
                        case "zset":
                            keySize = template.opsForZSet().size(key) == null ? 0
                                    : template.opsForZSet().size(key);
                            break;
                    }
                    keyInfo.put("size", keySize);

                } catch (Exception e) {
                    // 如果在获取键信息时出错，记录错误但继续处理其他键
                    log.error("获取键 {} 信息失败: {}", key, e.getMessage());
                    keyInfo.put("type", "unknown");
                    keyInfo.put("ttl", -1L);
                    keyInfo.put("size", 0L);
                }

                return keyInfo;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("items", items);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取Redis键列表失败", e);
            return Result.error("获取Redis键列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定键的值
     * 
     * @param key 缓存键名
     */
    @GetMapping("/redis/key")
    public Result<Map<String, Object>> getRedisCacheValue(@RequestParam("key") String key) {
        if (key == null || key.trim().isEmpty()) {
            return Result.error("键名不能为空");
        }

        try {
            // 根据键名选择合适的RedisTemplate
            RedisTemplate<String, Object> template = isSessionKey(key) ? sessionRedisTemplate : redisTemplate;

            // 检查键是否存在
            Boolean hasKey = template.hasKey(key);
            if (Boolean.FALSE.equals(hasKey) || hasKey == null) {
                return Result.error("键不存在");
            }

            // 获取键类型
            String type = Objects.requireNonNull(template.type(key)).name().toLowerCase();

            // 获取TTL
            Long ttl = template.getExpire(key);

            // 获取值
            Object value;
            long size;

            switch (type) {
                case "string":
                    value = template.opsForValue().get(key);
                    size = (value != null) ? value.toString().length() : 0;
                    break;
                case "list":
                    Long listSize = template.opsForList().size(key);
                    size = listSize == null ? 0 : listSize;
                    value = template.opsForList().range(key, 0, size - 1);
                    break;
                case "hash":
                    value = template.opsForHash().entries(key);
                    size = template.opsForHash().size(key) == null ? 0 : template.opsForHash().size(key);
                    break;
                case "set":
                    value = template.opsForSet().members(key);
                    size = template.opsForSet().size(key) == null ? 0 : template.opsForSet().size(key);
                    break;
                case "zset":
                    Long zsetSize = template.opsForZSet().size(key);
                    size = zsetSize == null ? 0 : zsetSize;

                    // 获取有序集合的分数和成员
                    Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples = template
                            .opsForZSet().rangeWithScores(key, 0, size - 1);

                    List<Map<String, Object>> zsetValues = new ArrayList<>();
                    if (tuples != null) {
                        for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> tuple : tuples) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("score", tuple.getScore());
                            item.put("member", tuple.getValue());
                            zsetValues.add(item);
                        }
                    }
                    value = zsetValues;
                    break;
                default:
                    return Result.error("不支持的键类型: " + type);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("type", type);
            result.put("ttl", ttl);
            result.put("size", size);
            result.put("value", value);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取键值失败", e);
            return Result.error("获取键值失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定的缓存键
     * 
     * @param keys 缓存键名或键名数组
     */
    @PostMapping("/redis/delete")
    public Result<Boolean> deleteRedisCache(@RequestBody Map<String, Object> param) {
        try {
            Object keysObj = param.get("keys");
            List<String> keyList;

            if (keysObj instanceof List) {
                keyList = (List<String>) keysObj;
            } else if (keysObj instanceof String) {
                keyList = Collections.singletonList((String) keysObj);
            } else {
                return Result.error("参数错误");
            }

            if (keyList.isEmpty()) {
                return Result.error("键列表不能为空");
            }

            // 分别处理普通键和Session键
            List<String> sessionKeys = new ArrayList<>();
            List<String> normalKeys = new ArrayList<>();

            for (String key : keyList) {
                if (isSessionKey(key)) {
                    sessionKeys.add(key);
                } else {
                    normalKeys.add(key);
                }
            }

            long deletedCount = 0;

            if (!normalKeys.isEmpty()) {
                Long deleted = redisTemplate.delete(new HashSet<>(normalKeys));
                deletedCount += (deleted != null) ? deleted : 0;
            }

            if (!sessionKeys.isEmpty()) {
                Long deleted = sessionRedisTemplate.delete(new HashSet<>(sessionKeys));
                deletedCount += (deleted != null) ? deleted : 0;
            }

            return Result.success(deletedCount > 0);
        } catch (Exception e) {
            log.error("删除缓存键失败", e);
            return Result.error("删除缓存键失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     */
    @PostMapping("/redis/flush")
    public Result<Boolean> flushRedisCache() {
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.serverCommands().flushDb();
                return true;
            });
            return Result.success(true);
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return Result.error("清空缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有缓存 (别名)
     * 提供与/redis/flush相同的功能，但使用不同的端点名称
     */
    @PostMapping("/redis/clear")
    public Result<Boolean> clearRedisCache() {
        log.debug("接收到清除缓存请求");
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                connection.serverCommands().flushDb();
                return true;
            });
            log.info("缓存清除成功");
            return Result.success(true);
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 刷新Redis服务器状态
     * 清除部分缓存并返回最新的服务器信息
     */
    @PostMapping("/redis/refresh")
    public Result<Map<String, Object>> refreshRedisStats() {
        log.info("接收到刷新Redis状态请求");
        try {
            // 清除部分系统缓存，但不清除用户会话和重要数据
            Set<String> keys = redisTemplate.keys("product:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除商品相关缓存, 共{}个键", keys.size());
            }
            
            // 返回最新的Redis服务器信息
            return getRedisInfo();
        } catch (Exception e) {
            log.error("刷新Redis状态失败", e);
            return Result.error("刷新Redis状态失败: " + e.getMessage());
        }
    }

    /**
     * 判断键是否为Spring Session相关的键
     * 
     * @param key 键名
     * @return 如果是Spring Session相关的键则返回true，否则返回false
     */
    private boolean isSessionKey(String key) {
        return key != null && key.startsWith(SESSION_KEY_PREFIX);
    }
}