package com.muyingmall.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Map;

/**
 * Elasticsearch索引配置
 * 
 * 优化策略：
 * 1. 分片数量优化：根据数据量设置合理的分片数
 * 2. 刷新间隔优化：从1s提升到30s，减少刷新开销
 * 3. 副本数量：设置1个副本，平衡可用性和性能
 * 4. 分析器优化：使用IK分词器，提高中文搜索准确性
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig implements CommandLineRunner {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    @Value("${elasticsearch.index.number-of-shards:3}")
    private int numberOfShards;

    @Value("${elasticsearch.index.number-of-replicas:1}")
    private int numberOfReplicas;

    @Value("${elasticsearch.index.refresh-interval:30s}")
    private String refreshInterval;

    private static final String PRODUCT_INDEX = "products";

    @Override
    public void run(String... args) {
        try {
            // 检查索引是否存在
            boolean indexExists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(PRODUCT_INDEX)))
                    .value();

            if (!indexExists) {
                log.info("商品索引不存在，开始创建优化的索引配置...");
                createOptimizedIndex();
            } else {
                log.info("商品索引已存在，跳过配置更新（避免超时）");
                // 注释掉自动更新，避免启动时超时
                // updateIndexSettings();
            }
        } catch (Exception e) {
            log.warn("初始化Elasticsearch索引配置失败: {}，应用将继续启动", e.getMessage());
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 创建优化的索引
     */
    private void createOptimizedIndex() {
        try {
            // 读取索引配置文件
            ClassPathResource resource = new ClassPathResource("elasticsearch/product-index-settings.json");
            
            if (!resource.exists()) {
                log.warn("索引配置文件不存在，使用默认配置");
                createDefaultIndex();
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> indexConfig = objectMapper.readValue(inputStream, Map.class);
                
                // 覆盖配置文件中的分片和副本设置
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = (Map<String, Object>) indexConfig.get("settings");
                if (settings != null) {
                    settings.put("number_of_shards", numberOfShards);
                    settings.put("number_of_replicas", numberOfReplicas);
                    settings.put("refresh_interval", refreshInterval);
                }

                // 创建索引
                elasticsearchClient.indices().create(c -> c
                        .index(PRODUCT_INDEX)
                        .settings(s -> s
                                .numberOfShards(String.valueOf(numberOfShards))
                                .numberOfReplicas(String.valueOf(numberOfReplicas))
                                .refreshInterval(t -> t.time(refreshInterval))
                                .maxResultWindow(10000))
                );

                log.info("✓ 商品索引创建成功，分片数: {}, 副本数: {}, 刷新间隔: {}", 
                        numberOfShards, numberOfReplicas, refreshInterval);
            }

        } catch (Exception e) {
            log.error("创建优化索引失败: {}", e.getMessage(), e);
            // 降级：创建默认索引
            createDefaultIndex();
        }
    }

    /**
     * 创建默认索引（降级方案）
     */
    private void createDefaultIndex() {
        try {
            elasticsearchClient.indices().create(c -> c
                    .index(PRODUCT_INDEX)
                    .settings(s -> s
                            .numberOfShards(String.valueOf(numberOfShards))
                            .numberOfReplicas(String.valueOf(numberOfReplicas))
                            .refreshInterval(t -> t.time(refreshInterval))
                            .maxResultWindow(10000))
            );
            log.info("✓ 使用默认配置创建商品索引成功");
        } catch (Exception e) {
            log.error("创建默认索引失败: {}", e.getMessage());
        }
    }

    /**
     * 更新索引设置
     */
    private void updateIndexSettings() {
        try {
            // 更新动态设置（不需要关闭索引）
            elasticsearchClient.indices().putSettings(p -> p
                    .index(PRODUCT_INDEX)
                    .settings(s -> s
                            .numberOfReplicas(String.valueOf(numberOfReplicas))
                            .refreshInterval(t -> t.time(refreshInterval))
                            .maxResultWindow(10000))
            );

            log.info("✓ 索引设置更新成功，副本数: {}, 刷新间隔: {}", numberOfReplicas, refreshInterval);
        } catch (Exception e) {
            log.warn("更新索引设置失败: {}", e.getMessage());
        }
    }
}
