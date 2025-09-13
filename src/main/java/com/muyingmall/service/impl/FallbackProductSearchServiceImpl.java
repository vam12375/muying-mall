package com.muyingmall.service.impl;

import com.muyingmall.document.ProductDocument;
import com.muyingmall.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 降级搜索服务实现
 * 当Elasticsearch不可用时使用此实现
 */
@Slf4j
@Service
@ConditionalOnMissingBean(name = "productSearchServiceImpl")
public class FallbackProductSearchServiceImpl implements ProductSearchService {

    @Override
    public Page<ProductDocument> searchProducts(String keyword, Integer categoryId, Integer brandId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              String sortBy, String sortOrder,
                                              int page, int size) {
        log.warn("Elasticsearch不可用，搜索功能已降级");
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, int limit) {
        log.warn("Elasticsearch不可用，搜索建议功能已降级");
        return Collections.emptyList();
    }

    @Override
    public List<String> getHotSearchKeywords(int limit) {
        log.warn("Elasticsearch不可用，热门搜索词功能已降级");
        // 返回一些默认的热门搜索词
        return List.of("奶粉", "纸尿裤", "婴儿车", "奶瓶", "玩具");
    }

    @Override
    public Map<String, Object> getSearchAggregations(String keyword) {
        log.warn("Elasticsearch不可用，搜索聚合功能已降级");
        return new HashMap<>();
    }

    @Override
    public void syncProductToIndex(Integer productId) {
        log.warn("Elasticsearch不可用，商品同步功能已降级");
    }

    @Override
    public void batchSyncProductsToIndex(List<Integer> productIds) {
        log.warn("Elasticsearch不可用，批量商品同步功能已降级");
    }

    @Override
    public void deleteProductFromIndex(Integer productId) {
        log.warn("Elasticsearch不可用，商品删除功能已降级");
    }

    @Override
    public void rebuildSearchIndex() {
        log.warn("Elasticsearch不可用，索引重建功能已降级");
    }

    @Override
    public Map<String, Object> getIndexHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "elasticsearch_unavailable");
        status.put("message", "Elasticsearch服务不可用，搜索功能已降级");
        return status;
    }

    @Override
    public List<ProductDocument> getSimilarProducts(Integer productId, int limit) {
        log.warn("Elasticsearch不可用，相似商品推荐功能已降级");
        return Collections.emptyList();
    }

    @Override
    public void recordSearchStatistics(String keyword, long resultCount, Integer userId) {
        log.debug("Elasticsearch不可用，搜索统计功能已降级");
    }
}
