package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.*;
import com.muyingmall.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 育儿圈后台管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/circle")
@RequiredArgsConstructor
public class AdminCircleController {

    private final CirclePostService postService;
    private final CircleCommentService commentService;
    private final CircleTopicService topicService;
    private final CircleLikeService likeService;
    private final CircleFollowService followService;
    private final UserService userService;

    // ==================== 统计数据 ====================

    /**
     * 获取育儿圈统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 帖子统计
        stats.put("totalPosts", postService.count());
        stats.put("pendingPosts", postService.countByStatus(2)); // 审核中
        stats.put("normalPosts", postService.countByStatus(1));  // 正常
        stats.put("topPosts", postService.countTopPosts());      // 置顶
        stats.put("hotPosts", postService.countHotPosts());      // 热门
        
        // 话题统计
        stats.put("totalTopics", topicService.count());
        stats.put("activeTopics", topicService.countByStatus(1));
        
        // 评论统计
        stats.put("totalComments", commentService.count());
        
        // 今日数据
        stats.put("todayPosts", postService.countTodayPosts());
        stats.put("todayComments", commentService.countTodayComments());
        
        return Result.success(stats);
    }

    // ==================== 帖子管理 ====================

    /**
     * 获取帖子分页列表
     */
    @GetMapping("/posts")
    public Result<Page<CirclePost>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false) String keyword) {
        return Result.success(postService.getAdminPostPage(page, size, topicId, userId, status, isTop, isHot, keyword));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{postId}")
    public Result<CirclePost> getPostDetail(@PathVariable Long postId) {
        CirclePost post = postService.getPostDetail(postId, null);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 更新帖子状态（审核）
     */
    @PutMapping("/posts/{postId}/status")
    public Result<Void> updatePostStatus(@PathVariable Long postId, @RequestParam Integer status) {
        if (postService.updatePostStatus(postId, status)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    /**
     * 设置/取消置顶
     */
    @PutMapping("/posts/{postId}/top")
    public Result<Void> togglePostTop(@PathVariable Long postId, @RequestParam Integer isTop) {
        if (postService.updatePostTop(postId, isTop)) {
            return Result.success();
        }
        return Result.error("操作失败");
    }

    /**
     * 设置/取消热门
     */
    @PutMapping("/posts/{postId}/hot")
    public Result<Void> togglePostHot(@PathVariable Long postId, @RequestParam Integer isHot) {
        if (postService.updatePostHot(postId, isHot)) {
            return Result.success();
        }
        return Result.error("操作失败");
    }

    /**
     * 删除帖子（管理员）
     */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        if (postService.adminDeletePost(postId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    // ==================== 话题管理 ====================

    /**
     * 获取话题分页列表
     */
    @GetMapping("/topics")
    public Result<Page<CircleTopic>> getTopicList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(topicService.getAdminTopicPage(page, size, status, keyword));
    }

    /**
     * 获取话题详情
     */
    @GetMapping("/topics/{topicId}")
    public Result<CircleTopic> getTopicDetail(@PathVariable Integer topicId) {
        CircleTopic topic = topicService.getById(topicId);
        if (topic == null) {
            return Result.error("话题不存在");
        }
        return Result.success(topic);
    }

    /**
     * 创建话题
     */
    @PostMapping("/topics")
    public Result<CircleTopic> createTopic(@RequestBody CircleTopic topic) {
        if (topicService.createTopic(topic)) {
            return Result.success(topic);
        }
        return Result.error("创建失败");
    }

    /**
     * 更新话题
     */
    @PutMapping("/topics/{topicId}")
    public Result<Void> updateTopic(@PathVariable Integer topicId, @RequestBody CircleTopic topic) {
        topic.setTopicId(topicId);
        if (topicService.updateById(topic)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    /**
     * 更新话题状态
     */
    @PutMapping("/topics/{topicId}/status")
    public Result<Void> updateTopicStatus(@PathVariable Integer topicId, @RequestParam Integer status) {
        if (topicService.updateTopicStatus(topicId, status)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    /**
     * 删除话题
     */
    @DeleteMapping("/topics/{topicId}")
    public Result<Void> deleteTopic(@PathVariable Integer topicId) {
        if (topicService.removeById(topicId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    /**
     * 批量更新话题排序
     */
    @PutMapping("/topics/sort")
    public Result<Void> updateTopicSort(@RequestBody List<Map<String, Integer>> sortList) {
        if (topicService.batchUpdateSort(sortList)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    // ==================== 评论管理 ====================

    /**
     * 获取评论分页列表
     */
    @GetMapping("/comments")
    public Result<Page<CircleComment>> getCommentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(commentService.getAdminCommentPage(page, size, postId, userId, status, keyword));
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/comments/{commentId}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long commentId, @RequestParam Integer status) {
        if (commentService.updateCommentStatus(commentId, status)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    /**
     * 删除评论（管理员）
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        if (commentService.adminDeleteComment(commentId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/comments/batch")
    public Result<Void> batchDeleteComments(@RequestBody List<Long> commentIds) {
        if (commentService.batchDeleteComments(commentIds)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }
}
