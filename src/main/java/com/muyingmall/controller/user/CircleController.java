package com.muyingmall.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.Result;
import com.muyingmall.entity.*;
import com.muyingmall.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 育儿圈控制器
 */
@Slf4j
@RestController
@RequestMapping("/circle")
@RequiredArgsConstructor
public class CircleController {

    private final CirclePostService postService;
    private final CircleCommentService commentService;
    private final CircleLikeService likeService;
    private final CircleTopicService topicService;
    private final CircleFollowService followService;

    // ==================== 话题相关 ====================

    /**
     * 获取所有话题
     */
    @GetMapping("/topics")
    public Result<List<CircleTopic>> getTopics() {
        return Result.success(topicService.getActiveTopics());
    }

    /**
     * 获取热门话题
     */
    @GetMapping("/topics/hot")
    public Result<List<CircleTopic>> getHotTopics(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(topicService.getHotTopics(limit));
    }

    // ==================== 帖子相关 ====================

    /**
     * 发布帖子
     */
    @PostMapping("/posts")
    public Result<CirclePost> createPost(@RequestBody Map<String, Object> params,
                                         @RequestAttribute("userId") Integer userId) {
        Integer topicId = params.get("topicId") != null ? (Integer) params.get("topicId") : null;
        String content = (String) params.get("content");
        String images = (String) params.get("images");
        Integer productId = params.get("productId") != null ? (Integer) params.get("productId") : null;
        
        CirclePost post = postService.createPost(userId, topicId, content, images, productId);
        return Result.success(post);
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public Result<Page<CirclePost>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) Integer userId,
            @RequestAttribute(value = "userId", required = false) Integer currentUserId) {
        return Result.success(postService.getPostPage(page, size, topicId, userId, currentUserId));
    }

    /**
     * 获取关注用户的帖子
     */
    @GetMapping("/posts/following")
    public Result<Page<CirclePost>> getFollowingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Integer userId) {
        return Result.success(postService.getFollowingPosts(userId, page, size));
    }

    /**
     * 获取热门帖子
     */
    @GetMapping("/posts/hot")
    public Result<Page<CirclePost>> getHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Integer currentUserId) {
        return Result.success(postService.getHotPosts(page, size, currentUserId));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{postId}")
    public Result<CirclePost> getPostDetail(@PathVariable Long postId,
                                            @RequestAttribute(value = "userId", required = false) Integer currentUserId) {
        // 增加浏览量
        postService.incrementViewCount(postId);
        CirclePost post = postService.getPostDetail(postId, currentUserId);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId,
                                   @RequestAttribute("userId") Integer userId) {
        if (postService.deletePost(postId, userId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }
