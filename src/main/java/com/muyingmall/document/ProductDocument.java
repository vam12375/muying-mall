package com.muyingmall.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品搜索文档实体
 * 用于Elasticsearch索引的商品数据结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products", createIndex = true)
@Setting(settingPath = "elasticsearch/product-settings.json")
@Mapping(mappingPath = "elasticsearch/product-mapping.json")
public class ProductDocument {

    /**
     * 商品ID
     */
    @Id
    private Integer productId;

    /**
     * 商品名称 - 支持中文分词搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
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
     * 商品价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal productPrice;

    /**
     * 商品原价
     */
    @Field(type = FieldType.Double)
    private BigDecimal originalPrice;

    /**
     * 商品库存
     */
    @Field(type = FieldType.Integer)
    private Integer productStock;

    /**
     * 商品状态 - 精确匹配
     */
    @Field(type = FieldType.Keyword)
    private String productStatus;

    /**
     * 商品分类ID
     */
    @Field(type = FieldType.Integer)
    private Integer categoryId;

    /**
     * 商品分类名称 - 支持搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String categoryName;

    /**
     * 品牌ID
     */
    @Field(type = FieldType.Integer)
    private Integer brandId;

    /**
     * 品牌名称 - 支持搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String brandName;

    /**
     * 商品主图
     */
    @Field(type = FieldType.Keyword, index = false)
    private String productImage;

    /**
     * 商品图片列表
     */
    @Field(type = FieldType.Keyword, index = false)
    private List<String> productImages;

    /**
     * 是否热门商品
     */
    @Field(type = FieldType.Boolean)
    private Boolean isHot;

    /**
     * 是否新品
     */
    @Field(type = FieldType.Boolean)
    private Boolean isNew;

    /**
     * 是否推荐商品
     */
    @Field(type = FieldType.Boolean)
    private Boolean isRecommend;

    /**
     * 销量
     */
    @Field(type = FieldType.Integer)
    private Integer salesCount;

    /**
     * 评分
     */
    @Field(type = FieldType.Double)
    private Double rating;

    /**
     * 评论数量
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 商品标签 - 支持多标签搜索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private List<String> tags;

    /**
     * 商品规格信息 - 用于筛选
     */
    @Field(type = FieldType.Object)
    private Object specs;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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
