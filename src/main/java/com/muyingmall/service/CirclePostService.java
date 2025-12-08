package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CirclePost;

/**
 * 育儿圈帖子服务接口
 */
public interface CirclePostService extends IService<CirclePost> {

    /**
     * 发布帖子
     */
    CirclePost createPost(Integer userId, Integer topicId, String content, String images, Integer productId, Integer skuId, String skuSpecs);

    /**
     * 获取帖子详情
     */
    CirclePost getPostDetail(Long postId, Integer currentUserId);

    /**
     * 分页获取帖子列表
     */
    Page<CirclePost> getPostPage(int page, int size, Integer topicId, Integer userId, Integer currentUserId);

    /**
     * 获取关注用户的帖子
     */
    Page<CirclePost> getFollowingPosts(Integer userId, int page, int size);

    /**
     * 获取热门帖子
     */
    Page<CirclePost> getHotPosts(int page, int size, Integer currentUserId);

    /**
     * 删除帖子
     */
    boolean deletePost(Long postId, Integer userId);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long postId);

    // ==================== 后台管理接口 ====================

    /**
     * 后台管理-分页获取帖子列表
     */
    Page<CirclePost> getAdminPostPage(int page, int size, Integer topicId, Integer userId, 
                                       Integer status, Integer isTop, Integer isHot, String keyword);

    /**
     * 按状态统计帖子数量
     */
    long countByStatus(Integer status);

    /**
     * 统计置顶帖子数量
     */
    long countTopPosts();

    /**
     * 统计热门帖子数量
     */
    long countHotPosts();

    /**
     * 统计今日帖子数量
     */
    long countTodayPosts();

    /**
     * 更新帖子状态
     */
    boolean updatePostStatus(Long postId, Integer status);

    /**
     * 设置帖子置顶
     */
    boolean updatePostTop(Long postId, Integer isTop);

    /**
     * 设置帖子热门
     */
    boolean updatePostHot(Long postId, Integer isHot);

    /**
     * 管理员删除帖子
     */
    boolean adminDeletePost(Long postId);
}
