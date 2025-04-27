package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 */
@Data
@TableName("member_level")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 最低积分要求
     */
    private Integer minPoints;

    /**
     * 折扣率（0.1-1之间）
     */
    private BigDecimal discount;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 等级描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}