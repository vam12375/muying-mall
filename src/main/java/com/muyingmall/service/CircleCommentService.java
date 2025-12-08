package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleComment;

/**
 * 育儿圈评论服务接口
 */
public interface CircleCommentService extends IService<CircleComment> {

    /**
     * 发表评论
     */
    CircleComment createComment(Long postId, Integer userId, String content, Long parentId, Integer replyUserId);

    /**
     * 分页获取帖子评论
     */
    Page<CircleComment> getCommentPage(Long postId, int page, int size, Integer currentUserId);

    /**
     * 获取评论的回复列表
     */
    Page<CircleComment> getReplyPage(Long parentId, int page, int size, Integer currentUserId);

    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Integer userId);

    /**
     * 获取评论数量
     */
    int getCommentCount(Long postId);
}
