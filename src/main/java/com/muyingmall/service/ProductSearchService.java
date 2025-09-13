package com.muyingmall.service;

import com.muyingmall.document.ProductDocument;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务接口
 */
public interface ProductSearchService {

    /**
     * 综合搜索商品
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param brandId 品牌ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    Page<ProductDocument> searchProducts(String keyword, Integer categoryId, Integer brandId,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       String sortBy, String sortOrder,
                                       int page, int size);

    /**
     * 搜索建议
     * @param keyword 关键词前缀
     * @param limit 建议数量限制
     * @return 搜索建议列表
     */
    List<String> getSearchSuggestions(String keyword, int limit);

    /**
     * 获取热门搜索词
     * @param limit 数量限制
     * @return 热门搜索词列表
     */
    List<String> getHotSearchKeywords(int limit);

    /**
     * 获取搜索聚合信息
     * @param keyword 搜索关键词
     * @return 聚合信息，包括分类、品牌、价格区间等
     */
    Map<String, Object> getSearchAggregations(String keyword);

    /**
     * 同步单个商品到搜索索引
     * @param productId 商品ID
     */
    void syncProductToIndex(Integer productId);

    /**
     * 批量同步商品到搜索索引
     * @param productIds 商品ID列表
     */
    void batchSyncProductsToIndex(List<Integer> productIds);

    /**
     * 从搜索索引中删除商品
     * @param productId 商品ID
     */
    void deleteProductFromIndex(Integer productId);

    /**
     * 重建搜索索引
     */
    void rebuildSearchIndex();

    /**
     * 检查搜索索引健康状态
     * @return 健康状态信息
     */
    Map<String, Object> getIndexHealthStatus();

    /**
     * 相似商品推荐
     * @param productId 商品ID
     * @param limit 推荐数量
     * @return 相似商品列表
     */
    List<ProductDocument> getSimilarProducts(Integer productId, int limit);

    /**
     * 记录搜索统计
     * @param keyword 搜索关键词
     * @param resultCount 搜索结果数量
     * @param userId 用户ID（可选）
     */
    void recordSearchStatistics(String keyword, long resultCount, Integer userId);
}
