package com.muyingmall.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.muyingmall.document.ProductDocument;
import com.muyingmall.entity.Product;
import com.muyingmall.repository.ProductSearchRepository;
import com.muyingmall.service.ProductSearchService;
import com.muyingmall.service.ProductService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品搜索服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductService productService;
    private final RedisUtil redisUtil;

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
                        .fuzziness("AUTO")
                );
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
                            .fields("productDetail", hf -> hf.preTags("<em>").postTags("</em>"))
                    )
            );

            // 执行搜索
            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            // 转换结果
            List<ProductDocument> products = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            // 记录搜索统计
            if (StringUtils.hasText(keyword)) {
                recordSearchStatistics(keyword, response.hits().total().value(), null);
            }

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(products, pageable, response.hits().total().value());

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
            if (cached != null) {
                return (List<String>) cached;
            }

            // 构建前缀查询
            PrefixQuery prefixQuery = PrefixQuery.of(p -> p
                    .field("productName.keyword")
                    .value(keyword)
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(b -> b
                            .must(Query.of(mq -> mq.prefix(prefixQuery)))
                            .filter(Query.of(fq -> fq.term(t -> t.field("productStatus").value("上架"))))
                    )))
                    .size(limit)
                    .source(so -> so.filter(f -> f.includes("productName")))
            );

            SearchResponse<ProductDocument> response = elasticsearchClient.search(searchRequest, ProductDocument.class);

            List<String> suggestions = response.hits().hits().stream()
                    .map(hit -> hit.source().getProductName())
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
            // 从Redis获取热门搜索词
            Object cached = redisUtil.get(HOT_KEYWORDS_KEY);
            if (cached != null) {
                List<String> hotKeywords = (List<String>) cached;
                return hotKeywords.stream().limit(limit).collect(Collectors.toList());
            }

            // 如果缓存为空，返回默认热门词
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
                        .fields("productName^3", "productDetail^2", "categoryName", "brandName", "keywords")
                );
                boolQueryBuilder.must(Query.of(q -> q.multiMatch(multiMatchQuery)));
            }

            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("productStatus").value("上架"))));

            // 构建聚合
            Map<String, Aggregation> aggregations = new HashMap<>();
            
            // 分类聚合
            aggregations.put("categories", Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t.field("categoryId").size(20)))
            ));
            
            // 品牌聚合
            aggregations.put("brands", Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t.field("brandId").size(20)))
            ));

            // 价格区间聚合
            aggregations.put("priceRanges", Aggregation.of(a -> a
                    .range(r -> r
                            .field("productPrice")
                            .ranges(rr -> rr.to(JsonData.of(50)))
                            .ranges(rr -> rr.from(JsonData.of(50)).to(JsonData.of(100)))
                            .ranges(rr -> rr.from(JsonData.of(100)).to(JsonData.of(200)))
                            .ranges(rr -> rr.from(JsonData.of(200)).to(JsonData.of(500)))
                            .ranges(rr -> rr.from(JsonData.of(500)))
                    )
            ));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                    .aggregations(aggregations)
                    .size(0)
            );

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
        
        // 这里可以调用原有的数据库搜索逻辑
        // 然后将结果转换为ProductDocument
        
        return Page.empty();
    }

    /**
     * 获取默认热门搜索词
     */
    private List<String> getDefaultHotKeywords(int limit) {
        List<String> defaultKeywords = Arrays.asList(
                "奶粉", "纸尿裤", "婴儿车", "奶瓶", "玩具", 
                "辅食", "童装", "安全座椅", "洗护用品", "益智玩具"
        );
        return defaultKeywords.stream().limit(limit).collect(Collectors.toList());
    }
}
