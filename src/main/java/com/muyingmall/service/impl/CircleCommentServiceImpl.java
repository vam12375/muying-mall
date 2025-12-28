package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleComment;
import com.muyingmall.entity.CircleMessage;
import com.muyingmall.entity.CirclePost;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CircleCommentMapper;
import com.muyingmall.mapper.CirclePostMapper;
import com.muyingmall.service.CircleCommentService;
import com.muyingmall.service.CircleLikeService;
import com.muyingmall.service.CircleMessageService;
import com.muyingmall.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 育儿圈评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CircleCommentServiceImpl extends ServiceImpl<CircleCommentMapper, CircleComment> implements CircleCommentService {

    private final CirclePostMapper postMapper;
    private final UserService userService;
    private final CircleLikeService likeService;
    @Lazy
    private final CircleMessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CircleComment createComment(Long postId, Integer userId, String content, Long parentId, Integer replyUserId) {
        CircleComment comment = new CircleComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreateTime(java.time.LocalDateTime.now());
        save(comment);
        // 更新帖子评论数
        postMapper.incrementCommentCount(postId);
        // 填充用户信息
        comment.setUser(userService.getById(userId));
        
        // 发送消息通知
        if (parentId != null && replyUserId != null) {
            // 回复评论，通知被回复的用户
            messageService.createMessage(replyUserId, userId, CircleMessage.TYPE_REPLY, comment.getCommentId(), content);
        } else {
            // 评论帖子，通知帖子作者
            CirclePost post = postMapper.selectById(postId);
            if (post != null) {
                messageService.createMessage(post.getUserId(), userId, CircleMessage.TYPE_COMMENT, postId, content);
            }
        }
        return comment;
    }

    @Override
    public Page<CircleComment> getCommentPage(Long postId, int page, int size, Integer currentUserId) {
        // 查询所有评论（包括回复），前端处理嵌套关系
        Page<CircleComment> commentPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CircleComment>()
                        .eq(CircleComment::getPostId, postId)
                        .eq(CircleComment::getStatus, 1)
                        .orderByAsc(CircleComment::getCreateTime));
        
        fillCommentInfo(commentPage.getRecords(), currentUserId);
        return commentPage;
    }

    @Override
    public Page<CircleComment> getReplyPage(Long parentId, int page, int size, Integer currentUserId) {
        Page<CircleComment> replyPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<CircleComment>()
                        .eq(CircleComment::getParentId, parentId)
                        .eq(CircleComment::getStatus, 1)
                        .orderByAsc(CircleComment::getCreateTime));
        
        fillCommentInfo(replyPage.getRecords(), currentUserId);
        return replyPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Integer userId) {
        CircleComment comment = getById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return false;
        }
        // 软删除
        comment.setStatus(0);
        updateById(comment);
        // 更新帖子评论数
        postMapper.decrementCommentCount(comment.getPostId());
        return true;
    }

    @Override
    public int getCommentCount(Long postId) {
        return (int) count(new LambdaQueryWrapper<CircleComment>()
                .eq(CircleComment::getPostId, postId)
                .eq(CircleComment::getStatus, 1));
    }

    /**
     * 填充评论信息（用户、点赞状态）
     */
    private void fillCommentInfo(List<CircleComment> comments, Integer currentUserId) {
        if (comments.isEmpty()) return;
        
        // 收集用户ID
        Set<Integer> userIds = comments.stream()
                .map(CircleComment::getUserId)
                .collect(Collectors.toSet());
        comments.stream()
                .filter(c -> c.getReplyUserId() != null)
                .forEach(c -> userIds.add(c.getReplyUserId()));
        
        // 批量查询用户
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        
        // 批量查询点赞状态
        List<Long> commentIds = comments.stream()
                .map(CircleComment::getCommentId)
                .collect(Collectors.toList());
        Set<Long> likedIds = likeService.getLikedCommentIds(currentUserId, commentIds);
        
        // 填充信息
        for (CircleComment comment : comments) {
            comment.setUser(userMap.get(comment.getUserId()));
            if (comment.getReplyUserId() != null) {
                comment.setReplyUser(userMap.get(comment.getReplyUserId()));
            }
            comment.setIsLiked(likedIds.contains(comment.getCommentId()));
        }
    }

    // ==================== 后台管理接口实现 ====================

    @Override
    public Page<CircleComment> getAdminCommentPage(int page, int size, Long postId, Integer userId,
                                                    Integer status, String keyword) {
        LambdaQueryWrapper<CircleComment> wrapper = new LambdaQueryWrapper<CircleComment>()
                .eq(postId != null, CircleComment::getPostId, postId)
                .eq(userId != null, CircleComment::getUserId, userId)
                .eq(status != null, CircleComment::getStatus, status)
                .like(keyword != null && !keyword.isEmpty(), CircleComment::getContent, keyword)
                .orderByDesc(CircleComment::getCreateTime);
        
        Page<CircleComment> commentPage = page(new Page<>(page, size), wrapper);
        fillCommentInfo(commentPage.getRecords(), null);
        return commentPage;
    }

    @Override
    public long countTodayComments() {
        java.time.LocalDateTime today = java.time.LocalDate.now().atStartOfDay();
        return count(new LambdaQueryWrapper<CircleComment>()
                .ge(CircleComment::getCreateTime, today)
                .eq(CircleComment::getStatus, 1));
    }

    @Override
    public boolean updateCommentStatus(Long commentId, Integer status) {
        CircleComment comment = new CircleComment();
        comment.setCommentId(commentId);
        comment.setStatus(status);
        return updateById(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeleteComment(Long commentId) {
        CircleComment comment = getById(commentId);
        if (comment == null) {
            return false;
        }
        // 软删除
        comment.setStatus(0);
        updateById(comment);
        // 更新帖子评论数
        postMapper.decrementCommentCount(comment.getPostId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteComments(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return false;
        }
        
        // 性能优化：批量删除替代循环单条删除，避免N次数据库操作
        // 原代码：for循环调用adminDeleteComment() -> N次UPDATE + N次查询
        // 优化后：1次批量查询 + 1次批量UPDATE + 批量更新帖子评论数
        
        // 1. 批量查询评论信息（用于更新帖子评论数）
        List<CircleComment> comments = listByIds(commentIds);
        if (comments.isEmpty()) {
            return false;
        }
        
        // 2. 批量软删除评论（1次UPDATE）
        LambdaUpdateWrapper<CircleComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CircleComment::getCommentId, commentIds)
                .set(CircleComment::getStatus, 0);
        update(updateWrapper);
        
        // 3. 统计每个帖子需要减少的评论数，批量更新
        Map<Long, Long> postCommentCounts = comments.stream()
                .collect(Collectors.groupingBy(CircleComment::getPostId, Collectors.counting()));
        
        for (Map.Entry<Long, Long> entry : postCommentCounts.entrySet()) {
            Long postId = entry.getKey();
            Long count = entry.getValue();
            // 批量更新帖子评论数（按postId分组后批量更新）
            for (int i = 0; i < count; i++) {
                postMapper.decrementCommentCount(postId);
            }
        }
        
        log.info("批量删除评论完成: 删除数量={}, 影响帖子数={}", commentIds.size(), postCommentCounts.size());
        return true;
    }
}
