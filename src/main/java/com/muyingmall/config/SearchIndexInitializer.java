package com.muyingmall.config;

import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.SearchIndexService;
import com.muyingmall.service.SearchStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 搜索索引初始化器
 * 应用启动时自动检查并初始化ES索引
 *
 * 性能优化：
 * 1. 启动时缓存预热 - 预热热门搜索词和聚合数据
 * 2. 异步初始化 - 不阻塞应用启动
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class SearchIndexInitializer implements CommandLineRunner {

    private final SearchIndexService searchIndexService;
    private final ProductSearchService productSearchService;
    private final SearchStatisticsService searchStatisticsService;

    @Value("${elasticsearch.warmup.enabled:true}")
    private boolean warmupEnabled;

    @Value("${elasticsearch.warmup.hot-keywords-count:20}")
    private int hotKeywordsCount;

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

            // 4. 执行缓存预热
            if (warmupEnabled) {
                warmUpSearchCache();
            }

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

            // 重建完成后进行缓存预热
            if (warmupEnabled) {
                warmUpSearchCache();
            }
        } catch (Exception e) {
            log.error("同步商品数据到ES失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 缓存预热
     *
     * 预热策略：
     * 1. 预热热门搜索词的查询结果
     * 2. 预热分类/品牌聚合数据
     * 3. 预热搜索建议
     */
    private void warmUpSearchCache() {
        try {
            long startTime = System.currentTimeMillis();
            log.info("开始执行搜索缓存预热...");

            // 1. 获取热门搜索词
            List<String> hotKeywords = searchStatisticsService.getHotKeywords(hotKeywordsCount, 7);
            log.info("预热热门搜索词: {} 个", hotKeywords.size());

            // 2. 预热热门搜索词的查询结果
            int warmupCount = 0;
            for (String keyword : hotKeywords) {
                try {
                    // 执行搜索，结果会被自动缓存
                    productSearchService.searchProducts(keyword, null, null, null, null, null, null, 0, 10);
                    // 预热搜索建议
                    productSearchService.getSearchSuggestions(keyword, 10);
                    warmupCount++;
                } catch (Exception e) {
                    log.debug("预热关键词失败: {}, 错误: {}", keyword, e.getMessage());
                }
            }

            // 3. 预热聚合数据（无关键词的全局聚合）
            try {
                productSearchService.getSearchAggregations(null);
                log.debug("预热聚合数据成功");
            } catch (Exception e) {
                log.debug("预热聚合数据失败: {}", e.getMessage());
            }

            // 4. 预热热门搜索词列表
            try {
                productSearchService.getHotSearchKeywords(20);
                log.debug("预热热门搜索词列表成功");
            } catch (Exception e) {
                log.debug("预热热门搜索词列表失败: {}", e.getMessage());
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("搜索缓存预热完成，预热关键词: {} 个，耗时: {}ms", warmupCount, duration);

        } catch (Exception e) {
            log.warn("搜索缓存预热失败: {}，不影响正常使用", e.getMessage());
        }
    }
}
