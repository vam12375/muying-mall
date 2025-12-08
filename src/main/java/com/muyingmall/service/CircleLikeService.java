package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleLike;

import java.util.List;
import java.util.Set;

/**
 * 育儿圈点赞服务接口
 */
public interface CircleLikeService extends IService<CircleLike> {

    /**
     * 点赞帖子
     */
    boolean likePost(Integer userId, Long postId);

    /**
     * 取消点赞帖子
     */
    boolean unlikePost(Integer userId, Long postId);

    /**
     * 点赞评论
     */
    boolean likeComment(Integer userId, Long commentId);

    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Integer userId, Long commentId);

    /**
     * 检查是否已点赞帖子
     */
    boolean isPostLiked(Integer userId, Long postId);

    /**
     * 检查是否已点赞评论
     */
    boolean isCommentLiked(Integer userId, Long commentId);

    /**
     * 批量检查帖子点赞状态
     */
    Set<Long> getLikedPostIds(Integer userId, List<Long> postIds);

    /**
     * 批量检查评论点赞状态
     */
    Set<Long> getLikedCommentIds(Integer userId, List<Long> commentIds);
}
