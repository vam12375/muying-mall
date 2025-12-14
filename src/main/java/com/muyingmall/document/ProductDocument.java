package com.muyingmall.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品搜索文档实体
 * 用于Elasticsearch索引的商品数据结构
 *
 * 性能优化：
 * 1. 使用keyword子字段 - 支持精确匹配和排序
 * 2. 优化分词器配置 - 索引时使用ik_max_word，搜索时使用ik_smart
 * 3. 禁用不必要的索引 - 图片等纯展示字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products", createIndex = false)
public class ProductDocument {

    /**
     * 商品ID
     */
    @Id
    private Integer productId;

    /**
     * 商品名称 - 支持中文分词搜索
     * 使用MultiField支持精确匹配和全文搜索
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    @JsonProperty("name")
    private String productName;

    /**
     * 商品描述 - 支持中文分词搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productDetail;

    /**
     * 商品简介 - 用于搜索建议
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productSummary;

    /**
     * 商品价格 - 用于排序和范围筛选
     */
    @Field(type = FieldType.Double)
    @JsonProperty("price")
    private BigDecimal productPrice;

    /**
     * 商品原价
     */
    @Field(type = FieldType.Double)
    private BigDecimal originalPrice;

    /**
     * 商品库存 - 用于筛选
     */
    @Field(type = FieldType.Integer)
    private Integer productStock;

    /**
     * 商品状态 - 精确匹配
     */
    @Field(type = FieldType.Keyword)
    private String productStatus;

    /**
     * 商品分类ID - 用于筛选和聚合
     */
    @Field(type = FieldType.Integer)
    private Integer categoryId;

    /**
     * 商品分类名称 - 支持搜索
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String categoryName;

    /**
     * 品牌ID - 用于筛选和聚合
     */
    @Field(type = FieldType.Integer)
    private Integer brandId;

    /**
     * 品牌名称 - 支持搜索
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String brandName;

    /**
     * 商品主图 - 仅用于展示，不索引
     */
    @Field(type = FieldType.Keyword, index = false)
    @JsonProperty("image")
    private String productImage;

    /**
     * 商品图片列表 - 仅用于展示，不索引
     */
    @Field(type = FieldType.Keyword, index = false)
    private List<String> productImages;

    /**
     * 是否热门商品 - 用于筛选
     */
    @Field(type = FieldType.Boolean)
    private Boolean isHot;

    /**
     * 是否新品 - 用于筛选
     */
    @Field(type = FieldType.Boolean)
    private Boolean isNew;

    /**
     * 是否推荐商品 - 用于筛选
     */
    @Field(type = FieldType.Boolean)
    private Boolean isRecommend;

    /**
     * 销量 - 用于排序
     */
    @Field(type = FieldType.Integer)
    private Integer salesCount;

    /**
     * 评分 - 用于排序
     */
    @Field(type = FieldType.Double)
    private Double rating;

    /**
     * 评论数量 - 用于排序
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 商品标签 - 支持多标签搜索
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 商品规格信息 - 用于筛选
     */
    @Field(type = FieldType.Object)
    private Object specs;

    /**
     * 创建时间（时间戳，毫秒）- 用于排序
     */
    @Field(type = FieldType.Long)
    private Long createTime;

    /**
     * 更新时间（时间戳，毫秒）- 用于排序
     */
    @Field(type = FieldType.Long)
    private Long updateTime;

    /**
     * 搜索权重 - 用于排序
     */
    @Field(type = FieldType.Double)
    private Double searchWeight;

    /**
     * 商品关键词 - 用于搜索优化
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String keywords;
}
