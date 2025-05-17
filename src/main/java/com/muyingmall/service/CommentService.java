package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * 评价服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 创建商品评价
     *
     * @param comment 评价信息
     * @return 是否成功
     */
    boolean createComment(Comment comment);

    /**
     * 分页获取商品评价
     *
     * @param productId 商品ID
     * @param page      页码
     * @param size      每页大小
     * @return 评价分页
     */
    IPage<Comment> getProductCommentPage(Integer productId, Integer page, Integer size);

    /**
     * 获取商品所有评价
     *
     * @param productId 商品ID
     * @return 评价列表
     */
    List<Comment> getProductComments(Integer productId);

    /**
     * 获取用户的评价
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 评价分页
     */
    IPage<Comment> getUserCommentPage(Integer userId, Integer page, Integer size);

    /**
     * 更新评价状态
     *
     * @param commentId 评价ID
     * @param status    状态：0-隐藏，1-显示
     * @return 是否成功
     */
    boolean updateCommentStatus(Integer commentId, Integer status);

    /**
     * 删除评价
     *
     * @param commentId 评价ID
     * @return 是否成功
     */
    boolean deleteComment(Integer commentId);

    /**
     * 获取商品评分统计
     *
     * @param productId 商品ID
     * @return 评分统计信息
     */
    Map<String, Object> getProductRatingStats(Integer productId);

    /**
     * 管理员获取评价列表
     *
     * @param page      页码
     * @param size      每页大小
     * @param productId 商品ID，可为null
     * @param userId    用户ID，可为null
     * @param minRating 最低评分，可为null
     * @param maxRating 最高评分，可为null
     * @param status    状态，可为null
     * @return 评价分页
     */
    IPage<Comment> adminGetCommentPage(Integer page, Integer size, Integer productId,
            Integer userId, Integer minRating, Integer maxRating, Integer status);

    /**
     * 获取评价统计数据
     *
     * @param days 最近天数，如7表示最近7天
     * @return 统计数据
     */
    Map<String, Object> getCommentStats(Integer days);
}