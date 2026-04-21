package com.muyingmall.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.*;
import com.muyingmall.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Tag(name = "用户-圈子社区", description = "孕产社区内容浏览与互动")
public class CircleController {

    private final CirclePostService postService;
    private final CircleCommentService commentService;
    private final CircleLikeService likeService;
    private final CircleTopicService topicService;
    private final CircleFollowService followService;
    private final CircleMessageService messageService;
    private final UserService userService;

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        User user = userService.findByUsername(auth.getName());
        return user != null ? user.getUserId() : null;
    }

    // ==================== 话题相关 ====================

    @GetMapping("/topics")
    @Operation(summary = "获取话题列表")
    public Result<List<CircleTopic>> getTopics() {
        return Result.success(topicService.getActiveTopics());
    }

    @GetMapping("/topics/hot")
    @Operation(summary = "获取热门话题")
    public Result<List<CircleTopic>> getHotTopics(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(topicService.getHotTopics(limit));
    }

    // ==================== 帖子相关 ====================

    @PostMapping("/posts")
    @Operation(summary = "发布帖子")
    public Result<CirclePost> createPost(@RequestBody Map<String, Object> params) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Integer topicId = params.get("topicId") != null ? Integer.valueOf(params.get("topicId").toString()) : null;
        String content = (String) params.get("content");
        String images = (String) params.get("images");
        Integer productId = params.get("productId") != null ? Integer.valueOf(params.get("productId").toString()) : null;
        Integer skuId = params.get("skuId") != null ? Integer.valueOf(params.get("skuId").toString()) : null;
        String skuSpecs = (String) params.get("skuSpecs");
        
        CirclePost post = postService.createPost(userId, topicId, content, images, productId, skuId, skuSpecs);
        return Result.success(post);
    }

    @GetMapping("/posts")
    @Operation(summary = "分页获取帖子列表")
    public Result<Page<CirclePost>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) Integer userId) {
        return Result.success(postService.getPostPage(page, size, topicId, userId, getCurrentUserId()));
    }

    @GetMapping("/posts/following")
    @Operation(summary = "获取关注用户的帖子")
    public Result<Page<CirclePost>> getFollowingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(postService.getFollowingPosts(userId, page, size));
    }

    @GetMapping("/posts/hot")
    @Operation(summary = "获取热门帖子")
    public Result<Page<CirclePost>> getHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.getHotPosts(page, size, getCurrentUserId()));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "获取帖子详情")
    public Result<CirclePost> getPostDetail(@PathVariable Long postId) {
        postService.incrementViewCount(postId);
        CirclePost post = postService.getPostDetail(postId, getCurrentUserId());
        if (post == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "删除自己的帖子")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (postService.deletePost(postId, userId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }


    // ==================== 评论相关 ====================

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "发表评论")
    public Result<CircleComment> createComment(@PathVariable Long postId, @RequestBody Map<String, Object> params) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String content = (String) params.get("content");
        Long parentId = params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null;
        Integer replyUserId = params.get("replyUserId") != null ? Integer.valueOf(params.get("replyUserId").toString()) : null;
        
        CircleComment comment = commentService.createComment(postId, userId, content, parentId, replyUserId);
        return Result.success(comment);
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "分页获取帖子评论")
    public Result<Page<CircleComment>> getComments(@PathVariable Long postId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getCommentPage(postId, page, size, getCurrentUserId()));
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "分页获取评论回复")
    public Result<Page<CircleComment>> getReplies(@PathVariable Long commentId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getReplyPage(commentId, page, size, getCurrentUserId()));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (commentService.deleteComment(commentId, userId)) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    // ==================== 点赞相关 ====================

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "点赞/取消点赞帖子")
    public Result<Map<String, Object>> togglePostLike(@PathVariable Long postId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isLiked = likeService.isPostLiked(userId, postId);
        if (isLiked) {
            likeService.unlikePost(userId, postId);
        } else {
            likeService.likePost(userId, postId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", !isLiked);
        return Result.success(result);
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "点赞/取消点赞评论")
    public Result<Map<String, Object>> toggleCommentLike(@PathVariable Long commentId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isLiked = likeService.isCommentLiked(userId, commentId);
        if (isLiked) {
            likeService.unlikeComment(userId, commentId);
        } else {
            likeService.likeComment(userId, commentId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", !isLiked);
        return Result.success(result);
    }

    // ==================== 关注相关 ====================

    @PostMapping("/users/{targetUserId}/follow")
    @Operation(summary = "关注/取消关注用户")
    public Result<Map<String, Object>> toggleFollow(@PathVariable Integer targetUserId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isFollowing = followService.isFollowing(userId, targetUserId);
        if (isFollowing) {
            followService.unfollowUser(userId, targetUserId);
        } else {
            followService.followUser(userId, targetUserId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", !isFollowing);
        return Result.success(result);
    }

    @GetMapping("/users/{targetUserId}/following")
    @Operation(summary = "获取用户关注列表")
    public Result<Page<User>> getFollowingList(@PathVariable Integer targetUserId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowingList(targetUserId, page, size));
    }

    @GetMapping("/users/{targetUserId}/followers")
    @Operation(summary = "获取用户粉丝列表")
    public Result<Page<User>> getFollowerList(@PathVariable Integer targetUserId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowerList(targetUserId, page, size));
    }

    @GetMapping("/users/{targetUserId}/stats")
    @Operation(summary = "获取关注与粉丝统计")
    public Result<Map<String, Object>> getUserStats(@PathVariable Integer targetUserId) {
        Integer currentUserId = getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("followingCount", followService.getFollowingCount(targetUserId));
        stats.put("followerCount", followService.getFollowerCount(targetUserId));
        stats.put("isFollowing", currentUserId != null && followService.isFollowing(currentUserId, targetUserId));
        return Result.success(stats);
    }

    // ==================== 用户个人主页相关 ====================

    /**
     * 获取用户个人主页信息
     */
    @GetMapping("/users/{targetUserId}/profile")
    @Operation(summary = "获取用户社区主页信息")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Integer targetUserId) {
        Integer currentUserId = getCurrentUserId();
        User user = userService.getById(targetUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("followingCount", followService.getFollowingCount(targetUserId));
        profile.put("followerCount", followService.getFollowerCount(targetUserId));
        profile.put("isFollowing", currentUserId != null && followService.isFollowing(currentUserId, targetUserId));
        profile.put("isSelf", targetUserId.equals(currentUserId));
        
        // 统计帖子数
        Page<CirclePost> postPage = postService.getPostPage(1, 1, null, targetUserId, currentUserId);
        profile.put("postCount", postPage.getTotal());
        
        return Result.success(profile);
    }

    /**
     * 获取用户发布的帖子列表
     */
    @GetMapping("/users/{targetUserId}/posts")
    @Operation(summary = "获取用户发布的帖子")
    public Result<Page<CirclePost>> getUserPosts(@PathVariable Integer targetUserId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.getPostPage(page, size, null, targetUserId, getCurrentUserId()));
    }

    /**
     * 编辑帖子
     */
    @PutMapping("/posts/{postId}")
    @Operation(summary = "编辑帖子")
    public Result<CirclePost> updatePost(@PathVariable Long postId, @RequestBody Map<String, Object> params) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        CirclePost post = postService.getById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.error(403, "无权编辑此帖子");
        }
        
        // 更新内容
        if (params.containsKey("content")) {
            post.setContent((String) params.get("content"));
        }
        if (params.containsKey("images")) {
            post.setImages((String) params.get("images"));
        }
        if (params.containsKey("topicId")) {
            post.setTopicId(params.get("topicId") != null ? Integer.valueOf(params.get("topicId").toString()) : null);
        }
        
        postService.updateById(post);
        return Result.success(postService.getPostDetail(postId, userId));
    }

    // ==================== 消息通知相关 ====================

    /**
     * 获取消息列表
     */
    @GetMapping("/messages")
    @Operation(summary = "分页获取社区消息")
    public Result<Page<CircleMessage>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer type) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(messageService.getMessagePage(userId, page, size, type));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/messages/unread-count")
    @Operation(summary = "获取未读消息数量")
    public Result<Map<String, Integer>> getUnreadCount() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(messageService.getUnreadCount(userId));
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/messages/{messageId}/read")
    @Operation(summary = "标记指定消息已读")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markAsRead(messageId, userId);
        return Result.success();
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/messages/read-all")
    @Operation(summary = "一键标记全部消息已读")
    public Result<Void> markAllAsRead() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markAllAsRead(userId);
        return Result.success();
    }
}
