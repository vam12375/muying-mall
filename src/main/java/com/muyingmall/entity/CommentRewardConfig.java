package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价奖励配置实体类
 */
@Data
@TableName("comment_reward_config")
public class CommentRewardConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;

    /**
     * 奖励类型：points-积分
     */
    private String rewardType;

    /**
     * 奖励名称
     */
    private String rewardName;

    /**
     * 奖励描述
     */
    private String rewardDescription;

    /**
     * 奖励等级：1-基础，2-进阶，3-高级
     */
    private Integer rewardLevel;

    /**
     * 奖励值
     */
    private Integer rewardValue;

    /**
     * 最小内容长度要求
     */
    private Integer minContentLength;

    /**
     * 最低评分要求
     */
    private Integer minRating;

    /**
     * 是否要求图片：0-不要求，1-要求
     */
    private Integer requireImage;

    /**
     * 最低图片数量要求
     */
    private Integer minImages;

    /**
     * 是否首次评价奖励：0-否，1-是
     */
    private Integer isFirstComment;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}