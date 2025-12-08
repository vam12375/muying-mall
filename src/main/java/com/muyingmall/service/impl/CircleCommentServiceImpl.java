package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        for (Long commentId : commentIds) {
            adminDeleteComment(commentId);
        }
        return true;
    }
}
