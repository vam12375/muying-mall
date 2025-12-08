package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleComment;
import com.muyingmall.entity.CircleLike;
import com.muyingmall.entity.CircleMessage;
import com.muyingmall.entity.CirclePost;
import com.muyingmall.mapper.CircleCommentMapper;
import com.muyingmall.mapper.CircleLikeMapper;
import com.muyingmall.mapper.CirclePostMapper;
import com.muyingmall.service.CircleLikeService;
import com.muyingmall.service.CircleMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 育儿圈点赞服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CircleLikeServiceImpl extends ServiceImpl<CircleLikeMapper, CircleLike> implements CircleLikeService {

    private final CirclePostMapper postMapper;
    private final CircleCommentMapper commentMapper;
    @Lazy
    private final CircleMessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Integer userId, Long postId) {
        // 检查是否已点赞
        if (isPostLiked(userId, postId)) {
            return true;
        }
        // 创建点赞记录
        CircleLike like = new CircleLike();
        like.setUserId(userId);
        like.setTargetId(postId);
        like.setTargetType(CircleLike.TARGET_TYPE_POST);
        save(like);
        // 更新帖子点赞数
        postMapper.incrementLikeCount(postId);
        
        // 发送消息通知给帖子作者
        CirclePost post = postMapper.selectById(postId);
        if (post != null) {
            String content = post.getContent();
            messageService.createMessage(post.getUserId(), userId, CircleMessage.TYPE_LIKE_POST, postId, content);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikePost(Integer userId, Long postId) {
        // 删除点赞记录
        boolean removed = remove(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .eq(CircleLike::getTargetId, postId)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_POST));
        if (removed) {
            postMapper.decrementLikeCount(postId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Integer userId, Long commentId) {
        if (isCommentLiked(userId, commentId)) {
            return true;
        }
        CircleLike like = new CircleLike();
        like.setUserId(userId);
        like.setTargetId(commentId);
        like.setTargetType(CircleLike.TARGET_TYPE_COMMENT);
        save(like);
        commentMapper.incrementLikeCount(commentId);
        
        // 发送消息通知给评论作者
        CircleComment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            messageService.createMessage(comment.getUserId(), userId, CircleMessage.TYPE_LIKE_COMMENT, commentId, comment.getContent());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Integer userId, Long commentId) {
        boolean removed = remove(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .eq(CircleLike::getTargetId, commentId)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_COMMENT));
        if (removed) {
            commentMapper.decrementLikeCount(commentId);
        }
        return true;
    }

    @Override
    public boolean isPostLiked(Integer userId, Long postId) {
        if (userId == null) return false;
        return count(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .eq(CircleLike::getTargetId, postId)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_POST)) > 0;
    }

    @Override
    public boolean isCommentLiked(Integer userId, Long commentId) {
        if (userId == null) return false;
        return count(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .eq(CircleLike::getTargetId, commentId)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_COMMENT)) > 0;
    }

    @Override
    public Set<Long> getLikedPostIds(Integer userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return new HashSet<>();
        }
        return list(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .in(CircleLike::getTargetId, postIds)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_POST))
                .stream()
                .map(CircleLike::getTargetId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getLikedCommentIds(Integer userId, List<Long> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return new HashSet<>();
        }
        return list(new LambdaQueryWrapper<CircleLike>()
                .eq(CircleLike::getUserId, userId)
                .in(CircleLike::getTargetId, commentIds)
                .eq(CircleLike::getTargetType, CircleLike.TARGET_TYPE_COMMENT))
                .stream()
                .map(CircleLike::getTargetId)
                .collect(Collectors.toSet());
    }
}
