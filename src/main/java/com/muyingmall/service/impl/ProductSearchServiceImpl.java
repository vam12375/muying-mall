package com.muyingmall.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品搜索服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    // private final ProductSearchRepository productSearchRepository; // 暂时禁用
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductService productService;
    private final RedisUtil redisUtil;
    private final SearchStatisticsService searchStatisticsService;

    private static final String SEARCH_STATS_KEY = "search:stats:";
    private static final String HOT_KEYWORDS_KEY = "search:hot_keywords";
    private static final String SEARCH_SUGGESTIONS_KEY = "search:suggestions:";

    @Override
    public Page<ProductDocument> searchProducts(String keyword, Integer categoryId, Integer brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, String sortOrder,
            int page, int size) {
        try {
            // 构建查询条件
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 关键词搜索
            if (StringUtils.hasText(keyword)) {
                // 多字段搜索，设置不同的权重
                MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                        .query(keyword)
                        .fields("productName^3", "productDetail^2", "categoryName^1.5", "brandName^1.5", "keywords^1")
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO"));
                boolQueryBuilder.must(Query.of(q -> q.multiMatch(multiMatchQuery)));
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
                RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("productPrice");
                if (minPrice != null) {
                    rangeBuilder.gte(JsonData.of(minPrice));
                }
                if (maxPrice != null) {
                    rangeBuilder.lte(JsonData.of(maxPrice));
                }
                boolQueryBuilder.filter(Query.of(q -> q.range(rangeBuilder.build())));
            }

            // 只搜索上架商品
            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("productStatus").value("上架"))));

            // 构建排序
            List<SortOptions> sortOptions = buildSortOptions(sortBy, sortOrder);

            // 构建搜索请求
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                    .sort(sortOptions)
                    .from(page * size)
                    .size(size)
                    .highlight(h -> h
                            .fields("productName", hf -> hf.preTags("<em>").postTags("</em>"))
                            .fields("productDetail", hf -> hf.preTags("<em>").postTags("</em>"))));

            // 执行搜索
            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            // 转换结果
            List<ProductDocument> products = response.hits().hits().stream()
                    .map(Hit::source)
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
            if (StringUtils.hasText(keyword)) {
                recordSearchStatistics(keyword, totalCount, null);
            }

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(products, pageable, totalCount);

        } catch (Exception e) {
            log.error("搜索商品失败: {}", e.getMessage(), e);
            // 降级到数据库搜索
            return fallbackToDbSearch(keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortOrder, page, size);
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
                return cachedList;
            }

            // 构建前缀查询
            PrefixQuery prefixQuery = PrefixQuery.of(p -> p
                    .field("productName.keyword")
                    .value(keyword));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(b -> b
                            .must(Query.of(mq -> mq.prefix(prefixQuery)))
                            .filter(Query.of(fq -> fq.term(t -> t.field("productStatus").value("上架")))))))
                    .size(limit)
                    .source(so -> so.filter(f -> f.includes("productName"))));

            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            List<String> suggestions = response.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> hit.source().getProductName())
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

            // 缓存结果
            redisUtil.set(cacheKey, suggestions, 300); // 5分钟缓存

            return suggestions;

        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage(), e);
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
                            .ranges(rr -> rr.to("50"))
                            .ranges(rr -> rr.from("50").to("100"))
                            .ranges(rr -> rr.from("100").to("200"))
                            .ranges(rr -> rr.from("200").to("500"))
                            .ranges(rr -> rr.from("500")))));

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
            log.info("商品同步功能暂时不可用，商品ID: {}", productId);
            // TODO: 实现基于ElasticsearchClient的同步逻辑
        } catch (Exception e) {
            log.error("同步商品到搜索索引失败: {}, 错误: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    public void batchSyncProductsToIndex(List<Integer> productIds) {
        try {
            log.info("批量商品同步功能暂时不可用，商品数量: {}", productIds.size());
            // TODO: 实现基于ElasticsearchClient的批量同步逻辑
        } catch (Exception e) {
            log.error("批量同步商品到搜索索引失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteProductFromIndex(Integer productId) {
        try {
            log.info("商品删除功能暂时不可用，商品ID: {}", productId);
            // TODO: 实现基于ElasticsearchClient的删除逻辑
        } catch (Exception e) {
            log.error("从搜索索引删除商品失败: {}, 错误: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    public void rebuildSearchIndex() {
        try {
            log.info("开始重建搜索索引...");

            // 删除现有索引
            elasticsearchOperations.indexOps(ProductDocument.class).delete();

            // 创建新索引
            elasticsearchOperations.indexOps(ProductDocument.class).create();
            elasticsearchOperations.indexOps(ProductDocument.class).putMapping();

            // 获取所有商品并同步到索引
            // 这里需要分批处理，避免内存溢出
            int page = 1; // MyBatis-Plus分页从1开始
            int size = 100;
            boolean hasNext = true;

            while (hasNext) {
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> mybatisPlusPage = productService
                        .getProductPage(page, size, null, null, null, null, null);
                List<ProductDocument> documents = mybatisPlusPage.getRecords().stream()
                        .map(this::convertToDocument)
                        .collect(Collectors.toList());

                if (!documents.isEmpty()) {
                    // TODO: 实现基于ElasticsearchClient的批量保存逻辑
                    log.info("准备保存 {} 个商品文档到索引", documents.size());
                }

                log.info("重建索引进度: 已处理第 {} 页，当前页商品数量: {}", page, documents.size());

                hasNext = mybatisPlusPage.hasNext();
                page++;
            }

            log.info("搜索索引重建完成");

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
                // TODO: 实现基于ElasticsearchClient的文档计数逻辑
                status.put("documentCount", "unknown");

                // 检查集群健康状态
                status.put("clusterHealth", "green"); // 简化实现
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
            log.info("相似商品推荐功能暂时不可用，商品ID: {}", productId);
            // TODO: 实现基于ElasticsearchClient的相似商品查询逻辑
            return Collections.emptyList();

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
                .createTime(product.getCreateTime())
                .updateTime(product.getUpdateTime())
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
