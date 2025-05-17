package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentTag;
import com.muyingmall.entity.Order;

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
         * 创建商品评价（带标签）
         *
         * @param comment 评价信息
         * @param tagIds  标签ID列表
         * @return 是否成功
         */
        boolean createCommentWithTags(Comment comment, List<Integer> tagIds);

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
         * 获取用户的评价（支持排序）
         *
         * @param userId 用户ID
         * @param page   页码
         * @param size   每页大小
         * @param sort   排序字段
         * @param order  排序方式(asc/desc)
         * @return 评价分页
         */
        IPage<Comment> getUserCommentPage(Integer userId, Integer page, Integer size, String sort, String order);

        /**
         * 搜索用户的评价（支持排序、搜索和筛选）
         *
         * @param userId       用户ID
         * @param page         页码
         * @param size         每页大小
         * @param sort         排序字段
         * @param order        排序方式(asc/desc)
         * @param keyword      搜索关键词
         * @param ratingFilter 评分筛选(good/neutral/bad)
         * @return 评价分页
         */
        IPage<Comment> searchUserCommentPage(Integer userId, Integer page, Integer size, String sort, String order,
                        String keyword, String ratingFilter);

        /**
         * 根据标签筛选用户评价
         *
         * @param userId 用户ID
         * @param tagId  标签ID
         * @param page   页码
         * @param size   每页大小
         * @param sort   排序字段
         * @param order  排序方式(asc/desc)
         * @return 评价分页
         */
        IPage<Comment> getUserCommentsByTag(Integer userId, Integer tagId, Integer page, Integer size, String sort,
                        String order);

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
         * @param orderId   订单ID，可为null
         * @return 评价分页
         */
        IPage<Comment> adminGetCommentPage(Integer page, Integer size, Integer productId,
                        Integer userId, Integer minRating, Integer maxRating, Integer status, Integer orderId);

        /**
         * 获取评价统计数据
         *
         * @param days 最近天数，如7表示最近7天
         * @return 统计数据
         */
        Map<String, Object> getCommentStats(Integer days);

        /**
         * 获取评价的标签列表
         *
         * @param commentId 评价ID
         * @return 标签列表
         */
        List<CommentTag> getCommentTags(Integer commentId);

        /**
         * 为评价添加标签
         *
         * @param commentId 评价ID
         * @param tagIds    标签ID列表
         * @return 是否成功
         */
        boolean addCommentTags(Integer commentId, List<Integer> tagIds);

        /**
         * 更新评价标签
         *
         * @param commentId 评价ID
         * @param tagIds    标签ID列表
         * @return 是否成功
         */
        boolean updateCommentTags(Integer commentId, List<Integer> tagIds);

        /**
         * 删除评价标签
         *
         * @param commentId 评价ID
         * @param tagId     标签ID
         * @return 是否成功
         */
        boolean removeCommentTag(Integer commentId, Integer tagId);

        /**
         * 删除评价的所有标签
         *
         * @param commentId 评价ID
         * @return 是否成功
         */
        boolean removeAllCommentTags(Integer commentId);

        /**
         * 创建评价并发放奖励
         *
         * @param comment 评价对象
         * @return 评价结果，包含评价ID和奖励信息
         */
        Map<String, Object> createCommentWithReward(Comment comment);

        /**
         * 获取用户未评价的订单
         *
         * @param userId 用户ID
         * @param page   页码
         * @param size   每页数量
         * @return 未评价订单分页
         */
        IPage<Order> getUnratedOrders(Integer userId, int page, int size);

        /**
         * 获取用户评价统计数据
         *
         * @param userId 用户ID
         * @return 统计数据，包含评价总数、平均评分、各评分数量等
         */
        Map<String, Object> getUserCommentStats(Integer userId);

        /**
         * 获取用户评价趋势数据
         *
         * @param userId 用户ID
         * @param days   天数
         * @return 趋势数据，包含日期和评价数量
         */
        Map<String, Object> getUserCommentTrend(Integer userId, int days);

        /**
         * 获取评价关键词
         *
         * @param productId 商品ID，可为null
         * @param minRating 最低评分，可为null
         * @param maxRating 最高评分，可为null
         * @param limit     关键词数量
         * @return 关键词数据，包含关键词和权重
         */
        Map<String, Object> getCommentKeywords(Integer productId, Integer minRating, Integer maxRating, Integer limit);

        /**
         * 获取评价情感分析数据
         *
         * @param productId 商品ID，可为null
         * @param days      天数
         * @return 情感分析数据，包含正面、中性、负面评价比例和趋势
         */
        Map<String, Object> getCommentSentimentAnalysis(Integer productId, Integer days);
}