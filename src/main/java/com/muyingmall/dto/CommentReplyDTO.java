package com.muyingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复数据传输对象
 */
@Data
@Schema(description = "评价回复DTO")
public class CommentReplyDTO {

    @Schema(description = "回复ID")
    private Integer replyId;

    @Schema(description = "评价ID")
    private Integer commentId;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "回复类型：1-商家回复，2-用户追评")
    private Integer replyType;

    @Schema(description = "回复用户ID")
    private Integer replyUserId;

    @Schema(description = "回复用户名")
    private String replyUserName;

    @Schema(description = "回复用户昵称")
    private String replyUserNickname;

    @Schema(description = "回复用户头像")
    private String replyUserAvatar;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}