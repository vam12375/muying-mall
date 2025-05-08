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

    @Qualifier("redisConnectionFactoryDb1")
    private final RedisConnectionFactory redisConnectionFactory;

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

            // 获取键统计信息
            Long totalKeys = redisTemplate.execute((RedisCallback<Long>) connection -> connection.dbSize());
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
     * 获取Redis缓存键列表
     * 
     * @param pattern 键名匹配模式，如 user*
     * @param page    页码，从1开始
     * @param size    每页大小
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
                    // 获取键类型
                    String type = Objects.requireNonNull(redisTemplate.type(key)).name().toLowerCase();
                    keyInfo.put("type", type);

                    // 获取TTL
                    Long ttl = redisTemplate.getExpire(key);
                    keyInfo.put("ttl", ttl);

                    // 获取大小（大致估算）
                    long keySize = 0;
                    switch (type) {
                        case "string":
                            Object value = redisTemplate.opsForValue().get(key);
                            keySize = (value != null) ? value.toString().length() : 0;
                            break;
                        case "list":
                            keySize = redisTemplate.opsForList().size(key) == null ? 0
                                    : redisTemplate.opsForList().size(key);
                            break;
                        case "hash":
                            keySize = redisTemplate.opsForHash().size(key) == null ? 0
                                    : redisTemplate.opsForHash().size(key);
                            break;
                        case "set":
                            keySize = redisTemplate.opsForSet().size(key) == null ? 0
                                    : redisTemplate.opsForSet().size(key);
                            break;
                        case "zset":
                            keySize = redisTemplate.opsForZSet().size(key) == null ? 0
                                    : redisTemplate.opsForZSet().size(key);
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
            // 检查键是否存在
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(hasKey) || hasKey == null) {
                return Result.error("键不存在");
            }

            // 获取键类型
            String type = Objects.requireNonNull(redisTemplate.type(key)).name().toLowerCase();

            // 获取TTL
            Long ttl = redisTemplate.getExpire(key);

            // 获取值
            Object value;
            long size;

            switch (type) {
                case "string":
                    value = redisTemplate.opsForValue().get(key);
                    size = (value != null) ? value.toString().length() : 0;
                    break;
                case "list":
                    Long listSize = redisTemplate.opsForList().size(key);
                    size = listSize == null ? 0 : listSize;
                    value = redisTemplate.opsForList().range(key, 0, size - 1);
                    break;
                case "hash":
                    value = redisTemplate.opsForHash().entries(key);
                    size = redisTemplate.opsForHash().size(key) == null ? 0 : redisTemplate.opsForHash().size(key);
                    break;
                case "set":
                    value = redisTemplate.opsForSet().members(key);
                    size = redisTemplate.opsForSet().size(key) == null ? 0 : redisTemplate.opsForSet().size(key);
                    break;
                case "zset":
                    Long zsetSize = redisTemplate.opsForZSet().size(key);
                    size = zsetSize == null ? 0 : zsetSize;

                    // 获取有序集合的分数和成员
                    Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples = redisTemplate
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

            Long deleted = redisTemplate.delete(new HashSet<>(keyList));
            return Result.success(deleted != null && deleted > 0);
        } catch (Exception e) {
            log.error("删除缓存键失败", e);
            return Result.error("删除缓存键失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     */
    @PostMapping("/redis/clear")
    public Result<Boolean> clearAllRedisCache() {
        try {
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.flushDb();
                return null;
            });
            return Result.success(true);
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return Result.error("清空缓存失败: " + e.getMessage());
        }
    }

    /**
     * 刷新Redis统计信息
     */
    @PostMapping("/redis/refresh")
    public Result<Map<String, Object>> refreshRedisStats() {
        return getRedisInfo();
    }
}