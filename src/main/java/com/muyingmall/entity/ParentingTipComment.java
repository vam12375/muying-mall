package com.muyingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 育儿知识评论实体类
 */
@Data
@TableName("parenting_tip_comment")
public class ParentingTipComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 知识ID */
    private Long tipId;

    /** 用户ID */
    private Integer userId;

    /** 评论内容 */
    private String content;

    /** 状态：0-删除，1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatar;
}
