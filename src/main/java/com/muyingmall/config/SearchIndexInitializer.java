package com.muyingmall.config;

import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 搜索索引初始化器
 * 应用启动时自动检查并初始化ES索引
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class SearchIndexInitializer implements CommandLineRunner {

    private final SearchIndexService searchIndexService;
    private final ProductSearchService productSearchService;

    @Override
    public void run(String... args) {
        log.info("========== 开始检查Elasticsearch索引状态 ==========");
        
        try {
            // 1. 检查索引健康状态
            Map<String, Object> healthStatus = productSearchService.getIndexHealthStatus();
            log.info("ES索引健康状态: {}", healthStatus);
            
            boolean indexExists = Boolean.TRUE.equals(healthStatus.get("indexExists"));
            Object docCountObj = healthStatus.get("documentCount");
            long documentCount = 0;
            
            if (docCountObj instanceof Number) {
                documentCount = ((Number) docCountObj).longValue();
            }
            
            // 2. 如果索引不存在，创建索引
            if (!indexExists) {
                log.warn("ES商品索引不存在，正在创建...");
                boolean created = searchIndexService.createProductIndex();
                if (created) {
                    log.info("ES商品索引创建成功，开始同步数据...");
                    syncAllProducts();
                } else {
                    log.error("ES商品索引创建失败");
                }
                return;
            }
            
            // 3. 如果索引存在但没有数据，同步数据
            if (documentCount == 0) {
                log.warn("ES索引存在但无数据(documentCount=0)，开始同步商品数据...");
                syncAllProducts();
                return;
            }
            
            log.info("ES索引状态正常，当前文档数: {}", documentCount);
            
        } catch (Exception e) {
            log.error("检查ES索引状态失败: {}，搜索功能将降级到数据库查询", e.getMessage());
        }
        
        log.info("========== Elasticsearch索引检查完成 ==========");
    }
    
    /**
     * 同步所有商品到ES索引
     */
    private void syncAllProducts() {
        try {
            log.info("开始重建ES搜索索引...");
            productSearchService.rebuildSearchIndex();
            log.info("ES搜索索引重建完成");
        } catch (Exception e) {
            log.error("同步商品数据到ES失败: {}", e.getMessage(), e);
        }
    }
}
