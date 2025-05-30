package com.muyingmall.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价数据传输对象
 */
@Data
public class CommentDTO {

    /**
     * 评价ID
     */
    private Integer commentId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价图片列表
     */
    private List<String> images;

    /**
     * 是否匿名：0-否，1-是
     */
    private Integer isAnonymous;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 用户名称（非匿名时显示）
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 是否已回复
     */
    private Boolean hasReplied;

    /**
     * 评价回复列表
     */
    private List<CommentReplyDTO> replies;
}