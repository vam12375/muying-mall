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
     * 奖励值
     */
    private Integer rewardValue;

    /**
     * 最小内容长度要求
     */
    private Integer minContentLength;

    /**
     * 是否要求图片：0-不要求，1-要求
     */
    private Integer requireImage;

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