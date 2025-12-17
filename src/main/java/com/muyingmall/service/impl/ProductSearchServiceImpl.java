package com.muyingmall.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.muyingmall.document.ProductDocument;
import com.muyingmall.entity.Product;

import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.SearchStatisticsService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 商品搜索服务实现类
 *
 * 性能优化：
 * 1. 智能模糊匹配策略 - 短关键词禁用模糊匹配
 * 2. 搜索结果缓存 - 热门查询结果缓存
 * 3. 批量操作优化 - 并发批量索引
 * 4. 查询优化 - 减少不必要的字段返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductService productService;
    private final RedisUtil redisUtil;
    private final SearchStatisticsService searchStatisticsService;

    private static final String SEARCH_STATS_KEY = "search:stats:";
    private static final String HOT_KEYWORDS_KEY = "search:hot_keywords";
    private static final String SEARCH_SUGGESTIONS_KEY = "search:suggestions:";
    private static final String SEARCH_RESULT_CACHE_KEY = "search:result:";

    // 批量操作配置
    @Value("${elasticsearch.bulk.batch-size:200}")
    private int bulkBatchSize;

    @Value("${elasticsearch.bulk.concurrent-requests:3}")
    private int bulkConcurrentRequests;

    // 搜索结果缓存时间（秒）
    @Value("${elasticsearch.search.cache-ttl:300}")
    private int searchCacheTtl;

    // 热门查询缓存时间（秒）- 更长的缓存时间
    private static final int HOT_QUERY_CACHE_TTL = 600;
    
    // 搜索建议缓存时间（秒）
    private static final int SUGGESTION_CACHE_TTL = 300;

    @Override
    public Page<ProductDocument> searchProducts(String keyword, Integer categoryId, Integer brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, String sortOrder,
            int page, int size) {
        try {
            // 尝试从缓存获取热门查询结果
            String cacheKey = buildSearchCacheKey(keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortOrder, page, size);
            Page<ProductDocument> cachedResult = getSearchResultFromCache(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取搜索结果: keyword={}", keyword);
                return cachedResult;
            }

            // 构建查询条件
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 关键词搜索 - 优化查询策略
            if (StringUtils.hasText(keyword)) {
                Query keywordQuery = buildOptimizedKeywordQuery(keyword);
                boolQueryBuilder.must(keywordQuery);
            }

            // 分类筛选
            if (categoryId != null) {
                boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("categoryId").value(categoryId))));
            }

            // 品牌筛选
            if (brandId != null) {
                boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("brandId").value(brandId))));
            }

            // 价格范围筛选
            if (minPrice != null || maxPrice != null) {
                final BigDecimal finalMinPrice = minPrice;
                final BigDecimal finalMaxPrice = maxPrice;
                boolQueryBuilder.filter(Query.of(q -> q.range(r -> r
                        .number(n -> {
                            n.field("productPrice");
                            if (finalMinPrice != null) {
                                n.gte(finalMinPrice.doubleValue());
                            }
                            if (finalMaxPrice != null) {
                                n.lte(finalMaxPrice.doubleValue());
                            }
                            return n;
                        }))));
            }

            // 只搜索上架商品
            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("productStatus").value("上架"))));

            // 构建排序
            List<SortOptions> sortOptions = buildSortOptions(sortBy, sortOrder);

            // 构建搜索请求 - 优化返回字段
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                    .sort(sortOptions)
                    .from(page * size)
                    .size(size)
                    // 排除大字段，减少网络传输
                    .source(src -> src.filter(f -> f
                            .excludes("productDetail", "specs")))
                    .highlight(h -> h
                            .fields(
                                co.elastic.clients.util.NamedValue.of("productName", co.elastic.clients.elasticsearch.core.search.HighlightField.of(hf -> hf.preTags("<em>").postTags("</em>"))),
                                co.elastic.clients.util.NamedValue.of("productDetail", co.elastic.clients.elasticsearch.core.search.HighlightField.of(hf -> hf.preTags("<em>").postTags("</em>")))
                            )));

            // 执行搜索
            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            // 转换结果
            List<ProductDocument> products = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 记录搜索统计
            long totalCount = 0;
            try {
                if (response.hits().total() != null) {
                    totalCount = response.hits().total().value();
                }
            } catch (Exception e) {
                log.debug("获取搜索结果总数失败: {}", e.getMessage());
            }

            // 如果ES返回空结果且有关键词，尝试降级到数据库搜索
            if (products.isEmpty() && StringUtils.hasText(keyword)) {
                log.debug("ES搜索无结果，尝试降级到数据库搜索，关键词: {}", keyword);
                Page<ProductDocument> dbResult = fallbackToDbSearch(keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortOrder, page, size);
                if (dbResult.getTotalElements() > 0) {
                    log.debug("数据库搜索找到 {} 条结果", dbResult.getTotalElements());
                    recordSearchStatistics(keyword, dbResult.getTotalElements(), null);
                    return dbResult;
                }
            }

            if (StringUtils.hasText(keyword)) {
                recordSearchStatistics(keyword, totalCount, null);
            }

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDocument> result = new PageImpl<>(products, pageable, totalCount);

            // 缓存热门查询结果（优化：只缓存第一页且有结果的查询）
            if (totalCount > 0 && page == 0) {
                // 热门查询使用更长的缓存时间
                int cacheTtl = isHotQuery(keyword) ? HOT_QUERY_CACHE_TTL : searchCacheTtl;
                cacheSearchResult(cacheKey, result, cacheTtl);
            }

            return result;

        } catch (Exception e) {
            log.error("搜索商品失败: {}", e.getMessage(), e);
            // 降级到数据库搜索
            return fallbackToDbSearch(keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortOrder, page, size);
        }
    }

    /**
     * 构建优化的关键词查询
     *
     * 优化策略：
     * 1. 短关键词（<3字符）禁用模糊匹配，提高精确度
     * 2. 中文关键词使用更严格的匹配策略
     * 3. 添加前缀长度限制，减少模糊匹配开销
     */
    private Query buildOptimizedKeywordQuery(String keyword) {
        // 判断是否为短关键词
        boolean isShortKeyword = keyword.length() < 3;
        // 判断是否包含中文
        boolean containsChinese = keyword.matches(".*[\\u4e00-\\u9fa5].*");

        if (isShortKeyword || containsChinese) {
            // 短关键词或中文：禁用模糊匹配，使用精确匹配
            MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("productName^3", "productDetail^2", "categoryName^1.5", "brandName^1.5", "keywords^1")
                    .type(TextQueryType.BestFields)
                    .operator(Operator.Or)
                    .minimumShouldMatch("1"));  // 至少匹配一个词
            return Query.of(q -> q.multiMatch(multiMatchQuery));
        } else {
            // 长关键词：启用模糊匹配
            MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("productName^3", "productDetail^2", "categoryName^1.5", "brandName^1.5", "keywords^1")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                    .prefixLength(2)  // 前两个字符必须精确匹配
                    .maxExpansions(50));  // 限制模糊匹配扩展数量
            return Query.of(q -> q.multiMatch(multiMatchQuery));
        }
    }

    /**
     * 构建搜索缓存键
     */
    private String buildSearchCacheKey(String keyword, Integer categoryId, Integer brandId,
            BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortOrder, int page, int size) {
        return SEARCH_RESULT_CACHE_KEY +
                Objects.hashCode(keyword) + ":" +
                Objects.hashCode(categoryId) + ":" +
                Objects.hashCode(brandId) + ":" +
                Objects.hashCode(minPrice) + ":" +
                Objects.hashCode(maxPrice) + ":" +
                Objects.hashCode(sortBy) + ":" +
                Objects.hashCode(sortOrder) + ":" +
                page + ":" + size;
    }

    /**
     * 从缓存获取搜索结果
     */
    @SuppressWarnings("unchecked")
    private Page<ProductDocument> getSearchResultFromCache(String cacheKey) {
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof Map) {
                Map<String, Object> cacheData = (Map<String, Object>) cached;
                List<ProductDocument> content = (List<ProductDocument>) cacheData.get("content");
                int pageNum = (int) cacheData.get("page");
                int pageSize = (int) cacheData.get("size");
                long total = ((Number) cacheData.get("total")).longValue();
                return new PageImpl<>(content, PageRequest.of(pageNum, pageSize), total);
            }
        } catch (Exception e) {
            log.debug("获取搜索缓存失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 缓存搜索结果（优化：支持自定义缓存时间）
     */
    private void cacheSearchResult(String cacheKey, Page<ProductDocument> result, int ttl) {
        try {
            Map<String, Object> cacheData = new HashMap<>();
            cacheData.put("content", result.getContent());
            cacheData.put("page", result.getNumber());
            cacheData.put("size", result.getSize());
            cacheData.put("total", result.getTotalElements());
            redisUtil.set(cacheKey, cacheData, ttl);
            log.debug("缓存搜索结果成功，TTL: {}秒", ttl);
        } catch (Exception e) {
            log.debug("缓存搜索结果失败: {}", e.getMessage());
        }
    }

    /**
     * 判断是否为热门查询
     */
    private boolean isHotQuery(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return false;
        }
        try {
            // 从热门关键词列表中检查
            List<String> hotKeywords = getHotSearchKeywords(50);
            return hotKeywords.contains(keyword.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        try {
            // 先从缓存获取
            String cacheKey = SEARCH_SUGGESTIONS_KEY + keyword;
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> cachedList = (List<String>) cached;
                log.debug("从缓存获取搜索建议: keyword={}, count={}", keyword, cachedList.size());
                return cachedList;
            }

            // 使用 match_phrase_prefix 查询，更好地支持中文搜索建议
            // 同时使用 multi_match 查询多个字段，提高召回率
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            // 使用 should 组合多种查询方式，提高中文搜索建议的准确性
            boolQueryBuilder.should(Query.of(q -> q
                    .matchPhrasePrefix(m -> m
                            .field("productName")
                            .query(keyword)
                            .maxExpansions(50))));
            
            // 添加 match 查询作为补充，支持分词后的匹配
            boolQueryBuilder.should(Query.of(q -> q
                    .match(m -> m
                            .field("productName")
                            .query(keyword)
                            .fuzziness("AUTO"))));
            
            // 至少匹配一个条件
            boolQueryBuilder.minimumShouldMatch("1");
            
            // 只搜索上架商品
            boolQueryBuilder.filter(Query.of(fq -> fq.term(t -> t.field("productStatus").value("上架"))));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                    .size(limit * 2) // 多查一些，去重后保证数量
                    .source(so -> so.filter(f -> f.includes("productName"))));

            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            List<String> suggestions = response.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> hit.source().getProductName())
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

            log.debug("搜索建议查询: keyword={}, 返回数量={}", keyword, suggestions.size());

            // 如果ES返回空结果，尝试从数据库获取
            if (suggestions.isEmpty()) {
                log.debug("ES搜索建议为空，尝试从数据库获取: keyword={}", keyword);
                suggestions = fallbackGetSuggestionsFromDb(keyword, limit);
            }

            // 缓存结果（优化：使用常量定义缓存时间）
            if (!suggestions.isEmpty()) {
                redisUtil.set(cacheKey, suggestions, SUGGESTION_CACHE_TTL);
            }

            return suggestions;

        } catch (Exception e) {
            log.error("获取搜索建议失败: keyword={}, error={}", keyword, e.getMessage(), e);
            // 降级：尝试从数据库获取
            return fallbackGetSuggestionsFromDb(keyword, limit);
        }
    }
    
    /**
     * 降级方案：从数据库获取搜索建议
     */
    private List<String> fallbackGetSuggestionsFromDb(String keyword, int limit) {
        try {
            log.debug("ES搜索建议失败，降级到数据库查询: keyword={}", keyword);
            // 使用ProductService从数据库模糊查询商品名称
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> page = 
                productService.getProductPage(1, limit, null, null, keyword, 1);
            
            return page.getRecords().stream()
                    .map(Product::getProductName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("数据库搜索建议也失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getHotSearchKeywords(int limit) {
        try {
            // 先从Redis缓存获取
            Object cached = redisUtil.get(HOT_KEYWORDS_KEY);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> hotKeywords = (List<String>) cached;
                return hotKeywords.stream().limit(limit).collect(Collectors.toList());
            }

            // 从数据库获取热门搜索词
            List<String> hotKeywords = searchStatisticsService.getHotKeywords(limit, 7); // 最近7天
            if (!hotKeywords.isEmpty()) {
                // 缓存结果
                redisUtil.set(HOT_KEYWORDS_KEY, hotKeywords, 3600); // 1小时缓存
                return hotKeywords;
            }

            // 如果数据库也没有数据，返回默认热门词
            return getDefaultHotKeywords(limit);

        } catch (Exception e) {
            log.error("获取热门搜索词失败: {}", e.getMessage(), e);
            return getDefaultHotKeywords(limit);
        }
    }

    @Override
    public Map<String, Object> getSearchAggregations(String keyword) {
        try {
            // 构建基础查询
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            if (StringUtils.hasText(keyword)) {
                MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                        .query(keyword)
                        .fields("productName^3", "productDetail^2", "categoryName", "brandName", "keywords"));
                boolQueryBuilder.must(Query.of(q -> q.multiMatch(multiMatchQuery)));
            }

            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("productStatus").value("上架"))));

            // 构建聚合
            Map<String, Aggregation> aggregations = new HashMap<>();

            // 分类聚合
            aggregations.put("categories", Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t.field("categoryId").size(20)))));

            // 品牌聚合
            aggregations.put("brands", Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t.field("brandId").size(20)))));

            // 价格区间聚合 - 简化实现
            aggregations.put("priceRanges", Aggregation.of(a -> a
                    .range(r -> r
                            .field("productPrice")
                            .ranges(rr -> rr.to(50.0))
                            .ranges(rr -> rr.from(50.0).to(100.0))
                            .ranges(rr -> rr.from(100.0).to(200.0))
                            .ranges(rr -> rr.from(200.0).to(500.0))
                            .ranges(rr -> rr.from(500.0)))));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                    .aggregations(aggregations)
                    .size(0));

            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            // 处理聚合结果
            Map<String, Object> result = new HashMap<>();
            result.put("categories", response.aggregations().get("categories"));
            result.put("brands", response.aggregations().get("brands"));
            result.put("priceRanges", response.aggregations().get("priceRanges"));

            return result;

        } catch (Exception e) {
            log.error("获取搜索聚合信息失败: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 构建排序选项
     */
    private List<SortOptions> buildSortOptions(String sortBy, String sortOrder) {
        List<SortOptions> sortOptions = new ArrayList<>();

        if (StringUtils.hasText(sortBy)) {
            SortOrder order = "desc".equalsIgnoreCase(sortOrder) ? SortOrder.Desc : SortOrder.Asc;

            switch (sortBy) {
                case "price":
                    sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("productPrice").order(order))));
                    break;
                case "sales":
                    sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("salesCount").order(order))));
                    break;
                case "rating":
                    sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("rating").order(order))));
                    break;
                case "createTime":
                    sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("createTime").order(order))));
                    break;
                default:
                    // 默认按相关性排序
                    sortOptions.add(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));
                    break;
            }
        } else {
            // 默认排序：相关性 + 权重 + 销量
            sortOptions.add(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("searchWeight").order(SortOrder.Desc))));
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("salesCount").order(SortOrder.Desc))));
        }

        return sortOptions;
    }

    /**
     * 降级到数据库搜索
     */
    private Page<ProductDocument> fallbackToDbSearch(String keyword, Integer categoryId, Integer brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, String sortOrder, int page, int size) {
        log.warn("Elasticsearch搜索失败，降级到数据库搜索");

        try {
            // 使用ProductService进行数据库搜索
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> productPage = productService
                    .getProductPage(
                            page + 1, // ProductService使用1开始的页码
                            size,
                            categoryId,
                            brandId,
                            keyword,
                            1 // 只搜索上架商品
                    );

            // 转换Product为ProductDocument
            List<ProductDocument> documents = productPage.getRecords().stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(documents, pageable, productPage.getTotal());

        } catch (Exception e) {
            log.error("数据库搜索也失败了: {}", e.getMessage(), e);
            // 确保返回一个可序列化的分页对象
            List<ProductDocument> emptyList = Collections.emptyList();
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(emptyList, pageable, 0);
        }
    }

    @Override
    public void syncProductToIndex(Integer productId) {
        try {
            log.debug("开始同步商品到搜索索引，商品ID: {}", productId);

            // 从数据库获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                log.warn("商品不存在，无法同步到索引，商品ID: {}", productId);
                return;
            }

            // 转换为ProductDocument
            ProductDocument document = convertToDocument(product);

            // 使用ElasticsearchClient保存到索引，设置刷新策略
            elasticsearchClient.index(i -> i
                    .index("products")
                    .id(String.valueOf(productId))
                    .document(document)
                    .refresh(Refresh.WaitFor)  // 等待刷新，确保立即可搜索
            );

            log.debug("商品同步到搜索索引成功，商品ID: {}, 商品名称: {}", productId, product.getProductName());

        } catch (Exception e) {
            log.error("同步商品到搜索索引失败: {}, 错误: {}", productId, e.getMessage(), e);
            throw new RuntimeException("同步商品到搜索索引失败: " + productId, e);
        }
    }

    @Override
    public void batchSyncProductsToIndex(List<Integer> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                log.warn("商品ID列表为空，无需同步");
                return;
            }

            log.debug("开始批量同步商品到搜索索引，商品数量: {}", productIds.size());

            // 批量获取商品信息
            List<Product> products = productService.listByIds(productIds);
            if (products.isEmpty()) {
                log.warn("未找到任何商品，无需同步");
                return;
            }

            // 批量转换为ProductDocument
            List<ProductDocument> documents = products.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

            // 使用优化的批量索引方法
            executeBulkIndex(documents, false);

            log.debug("批量同步商品到搜索索引完成，数量: {}", documents.size());

        } catch (Exception e) {
            log.error("批量同步商品到搜索索引失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量同步商品到搜索索引失败", e);
        }
    }

    /**
     * 执行优化的批量索引操作
     *
     * 优化策略：
     * 1. 分批处理，每批200条（可配置）
     * 2. 延迟刷新，减少刷新开销
     * 3. 错误重试机制
     *
     * @param documents 要索引的文档列表
     * @param delayRefresh 是否延迟刷新（批量重建时使用true）
     */
    private void executeBulkIndex(List<ProductDocument> documents, boolean delayRefresh) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        int totalDocs = documents.size();
        int successCount = 0;
        int failCount = 0;

        // 分批处理
        for (int i = 0; i < totalDocs; i += bulkBatchSize) {
            int endIndex = Math.min(i + bulkBatchSize, totalDocs);
            List<ProductDocument> batch = documents.subList(i, endIndex);

            try {
                co.elastic.clients.elasticsearch.core.BulkRequest.Builder bulkBuilder =
                    new co.elastic.clients.elasticsearch.core.BulkRequest.Builder();

                for (ProductDocument document : batch) {
                    bulkBuilder.operations(op -> op
                            .index(idx -> idx
                                    .index("products")
                                    .id(String.valueOf(document.getProductId()))
                                    .document(document)
                            )
                    );
                }

                // 设置刷新策略：批量重建时延迟刷新，单次同步时等待刷新
                if (delayRefresh) {
                    bulkBuilder.refresh(Refresh.False);
                } else {
                    bulkBuilder.refresh(Refresh.WaitFor);
                }

                co.elastic.clients.elasticsearch.core.BulkResponse bulkResponse =
                    elasticsearchClient.bulk(bulkBuilder.build());

                // 统计成功/失败数量
                if (bulkResponse.errors()) {
                    for (co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem item : bulkResponse.items()) {
                        if (item.error() != null) {
                            failCount++;
                            log.warn("批量索引失败，文档ID: {}, 错误: {}", item.id(), item.error().reason());
                        } else {
                            successCount++;
                        }
                    }
                } else {
                    successCount += batch.size();
                }

                log.debug("批量索引进度: {}/{}, 成功: {}, 失败: {}",
                        endIndex, totalDocs, successCount, failCount);

            } catch (Exception e) {
                log.error("批量索引批次失败: [{}-{}], 错误: {}", i, endIndex, e.getMessage());
                failCount += batch.size();
            }
        }

        log.debug("批量索引完成，总数: {}, 成功: {}, 失败: {}", totalDocs, successCount, failCount);
    }

    @Override
    public void deleteProductFromIndex(Integer productId) {
        try {
            log.debug("开始从搜索索引删除商品，商品ID: {}", productId);

            // 使用ElasticsearchClient删除文档
            co.elastic.clients.elasticsearch.core.DeleteResponse deleteResponse =
                elasticsearchClient.delete(d -> d
                        .index("products")
                        .id(String.valueOf(productId))
                        .refresh(Refresh.WaitFor)  // 立即刷新
                );

            // 检查删除结果
            if (deleteResponse.result() == co.elastic.clients.elasticsearch._types.Result.Deleted) {
                log.debug("商品从搜索索引删除成功，商品ID: {}", productId);
            } else if (deleteResponse.result() == co.elastic.clients.elasticsearch._types.Result.NotFound) {
                log.warn("商品在搜索索引中不存在，商品ID: {}", productId);
            } else {
                log.warn("商品删除状态未知，商品ID: {}, 结果: {}", productId, deleteResponse.result());
            }

        } catch (Exception e) {
            log.error("从搜索索引删除商品失败: {}, 错误: {}", productId, e.getMessage(), e);
            throw new RuntimeException("从搜索索引删除商品失败: " + productId, e);
        }
    }

    @Override
    public void rebuildSearchIndex() {
        try {
            long startTime = System.currentTimeMillis();
            log.debug("开始重建搜索索引...");

            // 删除现有索引
            elasticsearchOperations.indexOps(ProductDocument.class).delete();

            // 创建新索引
            elasticsearchOperations.indexOps(ProductDocument.class).create();
            elasticsearchOperations.indexOps(ProductDocument.class).putMapping();

            // 统计总数
            int totalProcessed = 0;
            int page = 1;
            int size = bulkBatchSize;  // 使用配置的批量大小
            boolean hasNext = true;

            // 收集所有文档
            List<ProductDocument> allDocuments = new ArrayList<>();

            while (hasNext) {
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> mybatisPlusPage = productService
                        .getProductPage(page, size, null, null, null, null);

                List<ProductDocument> documents = mybatisPlusPage.getRecords().stream()
                        .map(this::convertToDocument)
                        .collect(Collectors.toList());

                allDocuments.addAll(documents);
                totalProcessed += documents.size();

                log.debug("重建索引进度: 已加载 {} 条商品数据", totalProcessed);

                hasNext = mybatisPlusPage.hasNext();
                page++;
            }

            // 批量索引所有文档（延迟刷新以提高性能）
            if (!allDocuments.isEmpty()) {
                executeBulkIndex(allDocuments, true);

                // 最后手动刷新一次索引
                elasticsearchClient.indices().refresh(r -> r.index("products"));
            }

            long duration = System.currentTimeMillis() - startTime;
            log.debug("搜索索引重建完成，共处理 {} 条商品，耗时: {}ms", totalProcessed, duration);

        } catch (Exception e) {
            log.error("重建搜索索引失败: {}", e.getMessage(), e);
            throw new RuntimeException("重建搜索索引失败", e);
        }
    }

    @Override
    public Map<String, Object> getIndexHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(ProductDocument.class).exists();
            status.put("indexExists", indexExists);

            if (indexExists) {
                // 获取文档数量
                try {
                    co.elastic.clients.elasticsearch.core.CountRequest countRequest =
                        co.elastic.clients.elasticsearch.core.CountRequest.of(c -> c
                                .index("products")
                        );
                    co.elastic.clients.elasticsearch.core.CountResponse countResponse =
                        elasticsearchClient.count(countRequest);
                    status.put("documentCount", countResponse.count());
                } catch (Exception e) {
                    log.warn("获取文档数量失败: {}", e.getMessage());
                    status.put("documentCount", "unknown");
                }

                // 检查集群健康状态
                try {
                    co.elastic.clients.elasticsearch.cluster.HealthResponse healthResponse =
                        elasticsearchClient.cluster().health();
                    status.put("clusterHealth", healthResponse.status().jsonValue());
                    status.put("numberOfNodes", healthResponse.numberOfNodes());
                    status.put("activeShards", healthResponse.activeShards());
                } catch (Exception e) {
                    log.warn("获取集群健康状态失败: {}", e.getMessage());
                    status.put("clusterHealth", "unknown");
                }

                status.put("status", "healthy");
            } else {
                status.put("status", "index_not_exists");
            }

        } catch (Exception e) {
            log.error("获取索引健康状态失败: {}", e.getMessage(), e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }

        return status;
    }

    @Override
    public List<ProductDocument> getSimilarProducts(Integer productId, int limit) {
        try {
            log.debug("开始获取相似商品推荐，商品ID: {}, 推荐数量: {}", productId, limit);

            // 先从索引获取目标商品信息
            co.elastic.clients.elasticsearch.core.GetResponse<ProductDocument> getResponse =
                elasticsearchClient.get(g -> g
                        .index("products")
                        .id(String.valueOf(productId)),
                    ProductDocument.class
                );

            if (!getResponse.found()) {
                log.warn("商品在索引中不存在，无法推荐相似商品，商品ID: {}", productId);
                return Collections.emptyList();
            }

            ProductDocument targetProduct = getResponse.source();
            if (targetProduct == null) {
                log.warn("商品数据为空，无法推荐相似商品，商品ID: {}", productId);
                return Collections.emptyList();
            }

            // 构建相似商品查询：基于相同分类或品牌
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 优先推荐相同分类的商品
            if (targetProduct.getCategoryId() != null) {
                boolQueryBuilder.should(Query.of(q -> q
                        .term(t -> t.field("categoryId").value(targetProduct.getCategoryId()))
                ));
            }

            // 其次推荐相同品牌的商品
            if (targetProduct.getBrandId() != null) {
                boolQueryBuilder.should(Query.of(q -> q
                        .term(t -> t.field("brandId").value(targetProduct.getBrandId()))
                ));
            }

            // 只推荐上架商品
            boolQueryBuilder.filter(Query.of(q -> q
                    .term(t -> t.field("productStatus").value("上架"))
            ));

            // 排除目标商品本身
            boolQueryBuilder.mustNot(Query.of(q -> q
                    .term(t -> t.field("productId").value(productId))
            ));

            // 设置至少匹配一个条件
            boolQueryBuilder.minimumShouldMatch("1");

            // 构建搜索请求，按权重和销量排序
            co.elastic.clients.elasticsearch.core.SearchRequest searchRequest =
                co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
                        .index("products")
                        .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                        .sort(so -> so.field(f -> f.field("searchWeight").order(SortOrder.Desc)))
                        .sort(so -> so.field(f -> f.field("salesCount").order(SortOrder.Desc)))
                        .size(limit)
                );

            // 执行搜索
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> searchResponse =
                elasticsearchClient.search(searchRequest, ProductDocument.class);

            // 提取结果
            List<ProductDocument> similarProducts = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("成功获取相似商品推荐，商品ID: {}, 推荐数量: {}", productId, similarProducts.size());
            return similarProducts;

        } catch (Exception e) {
            log.error("获取相似商品失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void recordSearchStatistics(String keyword, long resultCount, Integer userId) {
        try {
            // 记录搜索统计到Redis（快速缓存）
            String statsKey = SEARCH_STATS_KEY + keyword;
            redisUtil.incr(statsKey, 1);
            redisUtil.expire(statsKey, 86400 * 7); // 7天过期

            // 更新热门搜索词
            updateHotKeywords(keyword);

            // 记录详细统计信息到数据库（异步）
            searchStatisticsService.recordSearch(keyword, resultCount, userId,
                    "web", null, null, null);

            log.debug("搜索统计记录成功: keyword={}, resultCount={}, userId={}",
                    keyword, resultCount, userId);

        } catch (Exception e) {
            log.error("记录搜索统计失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新热门搜索词
     */
    private void updateHotKeywords(String keyword) {
        try {
            // 简化实现：直接增加搜索次数
            String keywordCountKey = SEARCH_STATS_KEY + keyword;
            redisUtil.incr(keywordCountKey, 1);
            redisUtil.expire(keywordCountKey, 86400 * 7); // 7天过期

            // 这里可以定期统计热门搜索词，暂时使用简化实现
            log.debug("更新搜索词统计: {}", keyword);

        } catch (Exception e) {
            log.error("更新热门搜索词失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 将Product实体转换为ProductDocument
     */
    private ProductDocument convertToDocument(Product product) {
        return ProductDocument.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productDetail(product.getProductDetail())
                .productSummary(product.getProductDetail()) // 使用详情作为摘要
                .productPrice(product.getPriceNew())
                .originalPrice(product.getPriceOld())
                .productStock(product.getStock())
                .productStatus(product.getProductStatus())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .brandId(product.getBrandId())
                .brandName(product.getBrandName())
                .productImage(product.getProductImg())
                .isHot(product.getIsHot() != null && product.getIsHot() == 1)
                .isNew(product.getIsNew() != null && product.getIsNew() == 1)
                .isRecommend(product.getIsRecommend() != null && product.getIsRecommend() == 1)
                .salesCount(product.getSales() != null ? product.getSales() : 0)
                .rating(product.getRating() != null ? product.getRating().doubleValue() : 0.0)
                .commentCount(product.getReviewCount() != null ? product.getReviewCount() : 0)
                .createTime(product.getCreateTime() != null ? 
                        product.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .updateTime(product.getUpdateTime() != null ? 
                        product.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .searchWeight(calculateSearchWeight(product))
                .keywords(generateKeywords(product))
                .build();
    }

    /**
     * 计算搜索权重
     */
    private Double calculateSearchWeight(Product product) {
        double weight = 1.0;

        // 热门商品权重更高
        if (product.getIsHot() != null && product.getIsHot() == 1) {
            weight += 2.0;
        }

        // 推荐商品权重更高
        if (product.getIsRecommend() != null && product.getIsRecommend() == 1) {
            weight += 1.5;
        }

        // 新品权重更高
        if (product.getIsNew() != null && product.getIsNew() == 1) {
            weight += 1.0;
        }

        // 根据销量调整权重
        if (product.getSales() != null && product.getSales() > 0) {
            weight += Math.log10(product.getSales() + 1) * 0.5;
        }

        // 根据评分调整权重
        if (product.getRating() != null && product.getRating().doubleValue() > 0) {
            weight += product.getRating().doubleValue() * 0.3;
        }

        return weight;
    }

    /**
     * 生成搜索关键词
     */
    private String generateKeywords(Product product) {
        StringBuilder keywords = new StringBuilder();

        if (StringUtils.hasText(product.getProductName())) {
            keywords.append(product.getProductName()).append(" ");
        }

        if (StringUtils.hasText(product.getCategoryName())) {
            keywords.append(product.getCategoryName()).append(" ");
        }

        if (StringUtils.hasText(product.getBrandName())) {
            keywords.append(product.getBrandName()).append(" ");
        }

        // 可以添加更多关键词生成逻辑

        return keywords.toString().trim();
    }

    /**
     * 获取默认热门搜索词
     */
    private List<String> getDefaultHotKeywords(int limit) {
        List<String> defaultKeywords = Arrays.asList(
                "奶粉", "纸尿裤", "婴儿车", "奶瓶", "玩具",
                "辅食", "童装", "安全座椅", "洗护用品", "益智玩具");
        return defaultKeywords.stream().limit(limit).collect(Collectors.toList());
    }
}
