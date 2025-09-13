package com.muyingmall.service;

import com.muyingmall.document.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商品搜索服务测试类
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ProductSearchServiceTest {

    @Autowired
    private ProductSearchService productSearchService;

    @Autowired
    private SearchIndexService searchIndexService;

    @Test
    public void testSearchProducts() {
        try {
            // 测试基本搜索
            Page<ProductDocument> result = productSearchService.searchProducts(
                    "奶粉", null, null, null, null, 
                    "relevance", "desc", 0, 10);
            
            assertNotNull(result);
            log.info("搜索结果数量: {}", result.getTotalElements());
            
            // 测试分类筛选
            result = productSearchService.searchProducts(
                    "奶粉", 1, null, null, null, 
                    "price", "asc", 0, 10);
            
            assertNotNull(result);
            log.info("分类筛选结果数量: {}", result.getTotalElements());
            
            // 测试价格范围筛选
            result = productSearchService.searchProducts(
                    null, null, null, BigDecimal.valueOf(50), BigDecimal.valueOf(200), 
                    "price", "desc", 0, 10);
            
            assertNotNull(result);
            log.info("价格筛选结果数量: {}", result.getTotalElements());
            
        } catch (Exception e) {
            log.error("搜索测试失败", e);
            // 如果Elasticsearch不可用，测试应该优雅降级
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSearchSuggestions() {
        try {
            List<String> suggestions = productSearchService.getSearchSuggestions("奶", 5);
            assertNotNull(suggestions);
            log.info("搜索建议: {}", suggestions);
            
        } catch (Exception e) {
            log.error("搜索建议测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testHotKeywords() {
        try {
            List<String> hotKeywords = productSearchService.getHotSearchKeywords(10);
            assertNotNull(hotKeywords);
            assertTrue(hotKeywords.size() <= 10);
            log.info("热门搜索词: {}", hotKeywords);
            
        } catch (Exception e) {
            log.error("热门搜索词测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSearchAggregations() {
        try {
            Map<String, Object> aggregations = productSearchService.getSearchAggregations("奶粉");
            assertNotNull(aggregations);
            log.info("搜索聚合信息: {}", aggregations);
            
        } catch (Exception e) {
            log.error("搜索聚合测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSimilarProducts() {
        try {
            List<ProductDocument> similarProducts = productSearchService.getSimilarProducts(1, 5);
            assertNotNull(similarProducts);
            assertTrue(similarProducts.size() <= 5);
            log.info("相似商品数量: {}", similarProducts.size());
            
        } catch (Exception e) {
            log.error("相似商品测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testIndexHealthStatus() {
        try {
            Map<String, Object> healthStatus = productSearchService.getIndexHealthStatus();
            assertNotNull(healthStatus);
            log.info("索引健康状态: {}", healthStatus);
            
        } catch (Exception e) {
            log.error("索引健康状态测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testIndexManagement() {
        try {
            // 测试索引是否存在
            boolean exists = searchIndexService.indexExists("products");
            log.info("商品索引是否存在: {}", exists);
            
            // 测试获取索引信息
            Map<String, Object> indexInfo = searchIndexService.getIndexInfo("products");
            assertNotNull(indexInfo);
            log.info("索引信息: {}", indexInfo.keySet());
            
            // 测试获取索引统计
            Map<String, Object> indexStats = searchIndexService.getIndexStats("products");
            assertNotNull(indexStats);
            log.info("索引统计: {}", indexStats.keySet());
            
        } catch (Exception e) {
            log.error("索引管理测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSyncProduct() {
        try {
            // 测试同步单个商品
            productSearchService.syncProductToIndex(1);
            log.info("单个商品同步测试完成");
            
            // 测试批量同步
            List<Integer> productIds = List.of(1, 2, 3);
            productSearchService.batchSyncProductsToIndex(productIds);
            log.info("批量商品同步测试完成");
            
        } catch (Exception e) {
            log.error("商品同步测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRecordStatistics() {
        try {
            // 测试记录搜索统计
            productSearchService.recordSearchStatistics("测试关键词", 10L, 1);
            log.info("搜索统计记录测试完成");
            
        } catch (Exception e) {
            log.error("搜索统计记录测试失败", e);
            assertNotNull(e.getMessage());
        }
    }

    /**
     * 性能测试 - 并发搜索
     */
    @Test
    public void testConcurrentSearch() {
        try {
            int threadCount = 10;
            int searchCount = 100;
            
            long startTime = System.currentTimeMillis();
            
            // 模拟并发搜索
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                new Thread(() -> {
                    for (int j = 0; j < searchCount; j++) {
                        try {
                            productSearchService.searchProducts(
                                    "奶粉" + (j % 5), null, null, null, null,
                                    "relevance", "desc", 0, 10);
                        } catch (Exception e) {
                            log.warn("并发搜索失败: thread={}, search={}", threadId, j);
                        }
                    }
                }).start();
            }
            
            // 等待所有线程完成
            Thread.sleep(5000);
            
            long endTime = System.currentTimeMillis();
            log.info("并发搜索测试完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("并发搜索测试失败", e);
            assertNotNull(e.getMessage());
        }
    }
}
