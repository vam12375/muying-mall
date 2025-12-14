package com.muyingmall.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.indices.*;
import com.muyingmall.document.ProductDocument;
import com.muyingmall.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索索引管理服务实现类
 *
 * 使用ES原生API创建索引，避免Spring Data ES与ES 9.x的兼容性问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexServiceImpl implements SearchIndexService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;

    private static final String PRODUCT_INDEX_NAME = "products";

    @Override
    public boolean createProductIndex() {
        try {
            // 检查索引是否已存在
            if (indexExists(PRODUCT_INDEX_NAME)) {
                log.info("商品索引已存在: {}", PRODUCT_INDEX_NAME);
                return true;
            }

            // 使用ES原生API创建索引（避免Spring Data ES兼容性问题）
            CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
                    .index(PRODUCT_INDEX_NAME)
                    .settings(s -> s
                            .numberOfShards("1")
                            .numberOfReplicas("0")
                            .refreshInterval(t -> t.time("1s"))
                            .analysis(a -> a
                                    .analyzer("ik_max_word", an -> an.custom(ca -> ca
                                            .tokenizer("ik_max_word")))
                                    .analyzer("ik_smart", an -> an.custom(ca -> ca
                                            .tokenizer("ik_smart")))))
                    .mappings(m -> m
                            .properties("productId", p -> p.integer(i -> i))
                            .properties("productName", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")
                                    .fields("keyword", f -> f.keyword(k -> k))))
                            .properties("productDetail", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")))
                            .properties("productSummary", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")))
                            .properties("productPrice", p -> p.double_(d -> d))
                            .properties("originalPrice", p -> p.double_(d -> d))
                            .properties("productStock", p -> p.integer(i -> i))
                            .properties("productStatus", p -> p.keyword(k -> k))
                            .properties("categoryId", p -> p.integer(i -> i))
                            .properties("categoryName", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")
                                    .fields("keyword", f -> f.keyword(k -> k))))
                            .properties("brandId", p -> p.integer(i -> i))
                            .properties("brandName", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")
                                    .fields("keyword", f -> f.keyword(k -> k))))
                            .properties("productImage", p -> p.keyword(k -> k.index(false)))
                            .properties("productImages", p -> p.keyword(k -> k.index(false)))
                            .properties("isHot", p -> p.boolean_(b -> b))
                            .properties("isNew", p -> p.boolean_(b -> b))
                            .properties("isRecommend", p -> p.boolean_(b -> b))
                            .properties("salesCount", p -> p.integer(i -> i))
                            .properties("rating", p -> p.double_(d -> d))
                            .properties("commentCount", p -> p.integer(i -> i))
                            .properties("tags", p -> p.keyword(k -> k))
                            .properties("specs", p -> p.object(o -> o.enabled(false)))
                            .properties("createTime", p -> p.long_(l -> l))
                            .properties("updateTime", p -> p.long_(l -> l))
                            .properties("searchWeight", p -> p.double_(d -> d))
                            .properties("keywords", p -> p.text(t -> t
                                    .analyzer("ik_max_word")
                                    .searchAnalyzer("ik_smart")))));

            CreateIndexResponse response = elasticsearchClient.indices().create(createRequest);

            if (response.acknowledged()) {
                log.info("商品索引创建成功: {}", PRODUCT_INDEX_NAME);
                return true;
            } else {
                log.error("商品索引创建失败: {}", PRODUCT_INDEX_NAME);
                return false;
            }

        } catch (Exception e) {
            log.error("创建商品索引失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteProductIndex() {
        try {
            if (!indexExists(PRODUCT_INDEX_NAME)) {
                log.info("商品索引不存在，无需删除: {}", PRODUCT_INDEX_NAME);
                return true;
            }

            boolean deleted = elasticsearchOperations.indexOps(ProductDocument.class).delete();
            if (deleted) {
                log.info("商品索引删除成功: {}", PRODUCT_INDEX_NAME);
                return true;
            } else {
                log.error("商品索引删除失败: {}", PRODUCT_INDEX_NAME);
                return false;
            }

        } catch (Exception e) {
            log.error("删除商品索引失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean indexExists(String indexName) {
        try {
            ExistsRequest request = ExistsRequest.of(e -> e.index(indexName));
            return elasticsearchClient.indices().exists(request).value();
        } catch (Exception e) {
            log.error("检查索引是否存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getIndexInfo(String indexName) {
        Map<String, Object> info = new HashMap<>();
        try {
            if (!indexExists(indexName)) {
                info.put("exists", false);
                return info;
            }

            info.put("exists", true);
            info.put("indexName", indexName);

            // 获取索引设置
            GetIndexRequest request = GetIndexRequest.of(g -> g.index(indexName));
            GetIndexResponse response = elasticsearchClient.indices().get(request);

            // ES 9.x: 使用 get 方法获取索引状态
            IndexState indexState = response.get(indexName);
            if (indexState != null) {
                info.put("settings", indexState.settings());
                info.put("mappings", indexState.mappings());
                info.put("aliases", indexState.aliases());
            }

        } catch (Exception e) {
            log.error("获取索引信息失败: {}", e.getMessage(), e);
            info.put("error", e.getMessage());
        }

        return info;
    }

    @Override
    public Map<String, Object> getIndexStats(String indexName) {
        Map<String, Object> stats = new HashMap<>();
        try {
            if (!indexExists(indexName)) {
                stats.put("exists", false);
                return stats;
            }

            stats.put("exists", true);
            stats.put("indexName", indexName);

            // 获取索引统计信息
            IndicesStatsRequest request = IndicesStatsRequest.of(s -> s.index(indexName));
            IndicesStatsResponse response = elasticsearchClient.indices().stats(request);

            if (response.indices().containsKey(indexName)) {
                stats.put("indexStats", "available");
                // 简化统计信息，避免复杂的类型转换
                stats.put("hasStats", true);
            }

        } catch (Exception e) {
            log.error("获取索引统计信息失败: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @Override
    public boolean refreshIndex(String indexName) {
        try {
            RefreshRequest request = RefreshRequest.of(r -> r.index(indexName));
            elasticsearchClient.indices().refresh(request);

            log.info("索引刷新成功: {}", indexName);
            return true;

        } catch (Exception e) {
            log.error("刷新索引失败: {}", e.getMessage(), e);
            return false;
        }
    }
    @Override
    public boolean optimizeIndex(String indexName) {
        try {
            // 使用forcemerge API优化索引
            ForcemergeRequest request = ForcemergeRequest.of(f -> f
                    .index(indexName)
                    .maxNumSegments(1L)
                    .onlyExpungeDeletes(false));

            elasticsearchClient.indices().forcemerge(request);

            log.info("索引优化成功: {}", indexName);
            return true;

        } catch (Exception e) {
            log.error("优化索引失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setIndexAlias(String indexName, String aliasName) {
        try {
            PutAliasRequest request = PutAliasRequest.of(p -> p
                    .index(indexName)
                    .name(aliasName));

            PutAliasResponse response = elasticsearchClient.indices().putAlias(request);

            log.info("设置索引别名成功: {} -> {}", indexName, aliasName);
            return response.acknowledged();

        } catch (Exception e) {
            log.error("设置索引别名失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean removeIndexAlias(String indexName, String aliasName) {
        try {
            DeleteAliasRequest request = DeleteAliasRequest.of(d -> d
                    .index(indexName)
                    .name(aliasName));

            DeleteAliasResponse response = elasticsearchClient.indices().deleteAlias(request);

            log.info("删除索引别名成功: {} -> {}", indexName, aliasName);
            return response.acknowledged();

        } catch (Exception e) {
            log.error("删除索引别名失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
