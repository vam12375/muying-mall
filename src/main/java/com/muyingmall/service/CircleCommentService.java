package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleComment;

import java.util.List;

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

    // ==================== 后台管理接口 ====================

    /**
     * 后台管理-分页获取评论列表
     */
    Page<CircleComment> getAdminCommentPage(int page, int size, Long postId, Integer userId, 
                                             Integer status, String keyword);

    /**
     * 统计今日评论数量
     */
    long countTodayComments();

    /**
     * 更新评论状态
     */
    boolean updateCommentStatus(Long commentId, Integer status);

    /**
     * 管理员删除评论
     */
    boolean adminDeleteComment(Long commentId);

    /**
     * 批量删除评论
     */
    boolean batchDeleteComments(List<Long> commentIds);
}
