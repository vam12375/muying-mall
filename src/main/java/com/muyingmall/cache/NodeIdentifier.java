package com.muyingmall.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 节点唯一标识
 * 启动时生成，用于 Pub/Sub 消息去重（避免本节点处理自己发出的失效消息）
 */
@Component
@Getter
public class NodeIdentifier {

    private final String nodeId = UUID.randomUUID().toString();
}
