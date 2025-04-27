package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分历史记录实体类
 */
@Data
@TableName("points_history")
public class PointsHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分变动数量（正为增加，负为减少）
     */
    private Integer points;

    /**
     * 类型：earn(获得), spend(消费)
     */
    private String type;

    /**
     * 来源：order(订单), signin(签到), review(评论), register(注册), exchange(兑换)等
     */
    private String source;

    /**
     * 关联ID（如订单ID等）
     */
    private String referenceId;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}