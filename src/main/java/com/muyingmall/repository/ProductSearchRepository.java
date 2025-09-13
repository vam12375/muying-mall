package com.muyingmall.repository;

import com.muyingmall.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品搜索Repository
 * 定义基本的搜索操作方法
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {

    /**
     * 根据商品名称搜索
     */
    Page<ProductDocument> findByProductNameContaining(String productName, Pageable pageable);

    /**
     * 根据分类ID查找商品
     */
    Page<ProductDocument> findByCategoryId(Integer categoryId, Pageable pageable);

    /**
     * 根据品牌ID查找商品
     */
    Page<ProductDocument> findByBrandId(Integer brandId, Pageable pageable);

    /**
     * 查找热门商品
     */
    Page<ProductDocument> findByIsHotTrue(Pageable pageable);

    /**
     * 查找新品
     */
    Page<ProductDocument> findByIsNewTrue(Pageable pageable);

    /**
     * 查找推荐商品
     */
    Page<ProductDocument> findByIsRecommendTrue(Pageable pageable);

    /**
     * 根据价格范围查找商品
     */
    Page<ProductDocument> findByProductPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * 根据商品状态查找
     */
    Page<ProductDocument> findByProductStatus(String status, Pageable pageable);

    /**
     * 复合搜索 - 商品名称和描述
     */
    @Query("{\"bool\": {\"should\": [{\"match\": {\"productName\": \"?0\"}}, {\"match\": {\"productDetail\": \"?0\"}}]}}")
    Page<ProductDocument> findByProductNameOrProductDetail(String keyword, Pageable pageable);

    /**
     * 多字段搜索
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"productName^3\", \"productDetail^2\", \"categoryName\", \"brandName\", \"keywords\"]}}")
    Page<ProductDocument> multiFieldSearch(String keyword, Pageable pageable);

    /**
     * 根据分类和价格范围搜索
     */
    Page<ProductDocument> findByCategoryIdAndProductPriceBetween(
            Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * 根据品牌和价格范围搜索
     */
    Page<ProductDocument> findByBrandIdAndProductPriceBetween(
            Integer brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * 查找有库存的商品
     */
    Page<ProductDocument> findByProductStockGreaterThan(Integer stock, Pageable pageable);

    /**
     * 根据评分查找商品
     */
    Page<ProductDocument> findByRatingGreaterThanEqual(Double rating, Pageable pageable);

    /**
     * 查找指定分类的热门商品
     */
    Page<ProductDocument> findByCategoryIdAndIsHotTrue(Integer categoryId, Pageable pageable);

    /**
     * 查找指定品牌的推荐商品
     */
    Page<ProductDocument> findByBrandIdAndIsRecommendTrue(Integer brandId, Pageable pageable);

    /**
     * 根据标签搜索商品
     */
    @Query("{\"match\": {\"tags\": \"?0\"}}")
    Page<ProductDocument> findByTags(String tag, Pageable pageable);

    /**
     * 搜索建议 - 根据商品名称前缀
     */
    @Query("{\"bool\": {\"must\": [{\"prefix\": {\"productName.keyword\": \"?0\"}}], \"filter\": [{\"term\": {\"productStatus\": \"上架\"}}]}}")
    List<ProductDocument> findSuggestionsByProductNamePrefix(String prefix);

    /**
     * 获取热门搜索词 - 根据销量排序
     */
    @Query("{\"bool\": {\"filter\": [{\"term\": {\"productStatus\": \"上架\"}}]}}")
    Page<ProductDocument> findHotProducts(Pageable pageable);
}
