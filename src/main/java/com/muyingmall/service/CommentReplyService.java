package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CommentReply;

import java.util.List;

/**
 * 评价回复服务接口
 */
public interface CommentReplyService extends IService<CommentReply> {

    /**
     * 创建评价回复
     *
     * @param commentReply 回复信息
     * @return 是否成功
     */
    boolean createCommentReply(CommentReply commentReply);

    /**
     * 根据评价ID获取回复列表
     *
     * @param commentId 评价ID
     * @return 回复列表
     */
    List<CommentReply> getCommentReplies(Integer commentId);

    /**
     * 删除评价回复
     *
     * @param replyId 回复ID
     * @return 是否成功
     */
    boolean deleteCommentReply(Integer replyId);

    /**
     * 更新评价回复
     *
     * @param commentReply 回复信息
     * @return 是否成功
     */
    boolean updateCommentReply(CommentReply commentReply);

    /**
     * 获取用户的评价回复列表
     *
     * @param userId 用户ID
     * @return 回复列表
     */
    List<CommentReply> getUserCommentReplies(Integer userId);
}