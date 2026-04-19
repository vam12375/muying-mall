package com.muyingmall.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 本地缓存失效广播消息
 * 通过 Redis Pub/Sub 在多节点间同步 Caffeine (L1) 失效事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEvictMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 失效类型：KEY - 精确 key；PREFIX - 前缀匹配；ALL - 清空
     */
    public enum EvictType {
        KEY, PREFIX, ALL
    }

    /**
     * 失效类型
     */
    private EvictType type;

    /**
     * 缓存 key 或前缀（type=ALL 时忽略）
     */
    private String target;

    /**
     * 源节点 ID，用于避免自己处理自己发出的消息
     */
    private String sourceNodeId;

    /**
     * 时间戳
     */
    private long timestamp;
}
