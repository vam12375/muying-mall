package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentReply;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CommentMapper;
import com.muyingmall.mapper.CommentReplyMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.CommentReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价回复服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentReplyServiceImpl extends ServiceImpl<CommentReplyMapper, CommentReply>
        implements CommentReplyService {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createCommentReply(CommentReply commentReply) {
        // 校验评价是否存在
        Comment comment = commentMapper.selectById(commentReply.getCommentId());
        if (comment == null) {
            throw new BusinessException("评价不存在");
        }

        // 校验用户是否存在
        User user = userMapper.selectById(commentReply.getReplyUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 设置默认值
        if (commentReply.getReplyType() == null) {
            commentReply.setReplyType(1); // 默认为商家回复
        }

        // 保存评价回复
        boolean saveResult = this.save(commentReply);

        if (saveResult) {
            // 更新评价的hasReplied状态为true
            // 使用傀儡更新评价的hasReplied状态
            Comment updateComment = new Comment();
            updateComment.setCommentId(commentReply.getCommentId());
            updateComment.setHasReplied(true); // 设置为已回复
            commentMapper.updateById(updateComment);

            // 记录日志
            log.debug("已更新评价[{}]的回复状态为已回复", commentReply.getCommentId());
        }

        return saveResult;
    }

    @Override
    public List<CommentReply> getCommentReplies(Integer commentId) {
        LambdaQueryWrapper<CommentReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentReply::getCommentId, commentId)
                .orderByAsc(CommentReply::getCreateTime);

        List<CommentReply> replies = this.list(queryWrapper);

        // 填充用户信息
        if (!replies.isEmpty()) {
            List<Integer> userIds = replies.stream()
                    .map(CommentReply::getReplyUserId)
                    .collect(Collectors.toList());

            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getUserId, userIds));
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            replies.forEach(reply -> {
                if (userMap.containsKey(reply.getReplyUserId())) {
                    reply.setReplyUser(userMap.get(reply.getReplyUserId()));
                }
            });
        }

        return replies;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCommentReply(Integer replyId) {
        return this.removeById(replyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCommentReply(CommentReply commentReply) {
        return this.updateById(commentReply);
    }

    @Override
    public List<CommentReply> getUserCommentReplies(Integer userId) {
        // 先获取用户的所有评价
        LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(Comment::getUserId, userId);
        List<Comment> userComments = commentMapper.selectList(commentQueryWrapper);

        if (userComments.isEmpty()) {
            return List.of();
        }

        // 获取这些评价的所有回复
        List<Integer> commentIds = userComments.stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<CommentReply> replyQueryWrapper = new LambdaQueryWrapper<>();
        replyQueryWrapper.in(CommentReply::getCommentId, commentIds)
                .orderByDesc(CommentReply::getCreateTime);

        List<CommentReply> replies = this.list(replyQueryWrapper);

        // 填充用户信息
        if (!replies.isEmpty()) {
            List<Integer> userIds = replies.stream()
                    .map(CommentReply::getReplyUserId)
                    .collect(Collectors.toList());

            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getUserId, userIds));
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            // 填充评价信息
            Map<Integer, Comment> commentMap = userComments.stream()
                    .collect(Collectors.toMap(Comment::getCommentId, comment -> comment));

            replies.forEach(reply -> {
                if (userMap.containsKey(reply.getReplyUserId())) {
                    reply.setReplyUser(userMap.get(reply.getReplyUserId()));
                }
                if (commentMap.containsKey(reply.getCommentId())) {
                    reply.setComment(commentMap.get(reply.getCommentId()));
                }
            });
        }

        return replies;
    }
}