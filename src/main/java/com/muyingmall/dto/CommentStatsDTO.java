package com.muyingmall.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 评价统计数据传输对象
 */
@Data
public class CommentStatsDTO {

    /**
     * 总评价数量
     */
    private Integer totalComments;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 评分分布 {1: 数量, 2: 数量, ...}
     */
    private Map<Integer, Integer> ratingDistribution;

    /**
     * 日期标签列表 ["01-01", "01-02", ...]
     */
    private List<String> dateLabels;

    /**
     * 按日期统计的评分数据
     * [{name: "1星", data: [1, 2, 3...]}, {name: "2星", data: [...]}, ...]
     */
    private List<Map<String, Object>> dailyRatingData;

    /**
     * 好评率 (百分比)
     */
    private Double positiveRate;

    /**
     * 中评率 (百分比)
     */
    private Double neutralRate;

    /**
     * 差评率 (百分比)
     */
    private Double negativeRate;

    /**
     * 有图片的评价数量
     */
    private Integer commentWithImages;

    /**
     * 匿名评价数量
     */
    private Integer anonymousComments;
}