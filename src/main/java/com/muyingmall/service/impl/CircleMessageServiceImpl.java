package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleMessage;
import com.muyingmall.entity.CirclePost;
import com.muyingmall.entity.User;
import com.muyingmall.mapper.CircleMessageMapper;
import com.muyingmall.mapper.CirclePostMapper;
import com.muyingmall.service.CircleMessageService;
import com.muyingmall.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 育儿圈消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CircleMessageServiceImpl extends ServiceImpl<CircleMessageMapper, CircleMessage> implements CircleMessageService {

    private final UserService userService;
    private final CirclePostMapper postMapper;

    @Override
    public void createMessage(Integer userId, Integer fromUserId, Integer type, Long targetId, String content) {
        // 不给自己发消息
        if (userId.equals(fromUserId)) {
            return;
        }
        
        CircleMessage message = new CircleMessage();
        message.setUserId(userId);
        message.setFromUserId(fromUserId);
        message.setType(type);
        message.setTargetId(targetId);
        message.setContent(content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
    }

    @Override
    public Page<CircleMessage> getMessagePage(Integer userId, int page, int size, Integer type) {
        LambdaQueryWrapper<CircleMessage> wrapper = new LambdaQueryWrapper<CircleMessage>()
                .eq(CircleMessage::getUserId, userId)
                .eq(type != null, CircleMessage::getType, type)
                .orderByDesc(CircleMessage::getCreateTime);
        
        Page<CircleMessage> messagePage = page(new Page<>(page, size), wrapper);
        fillMessageInfo(messagePage.getRecords());
        return messagePage;
    }

    @Override
    public Map<String, Integer> getUnreadCount(Integer userId) {
        Map<String, Integer> result = new HashMap<>();
        
        // 总未读数
        long total = count(new LambdaQueryWrapper<CircleMessage>()
                .eq(CircleMessage::getUserId, userId)
                .eq(CircleMessage::getIsRead, 0));
        result.put("total", (int) total);
        
        // 点赞未读数（类型1和2）
        long likeCount = count(new LambdaQueryWrapper<CircleMessage>()
                .eq(CircleMessage::getUserId, userId)
                .eq(CircleMessage::getIsRead, 0)
                .in(CircleMessage::getType, CircleMessage.TYPE_LIKE_POST, CircleMessage.TYPE_LIKE_COMMENT));
        result.put("like", (int) likeCount);
        
        // 评论未读数（类型3和4）
        long commentCount = count(new LambdaQueryWrapper<CircleMessage>()
                .eq(CircleMessage::getUserId, userId)
                .eq(CircleMessage::getIsRead, 0)
                .in(CircleMessage::getType, CircleMessage.TYPE_COMMENT, CircleMessage.TYPE_REPLY));
        result.put("comment", (int) commentCount);
        
        // 关注未读数
        long followCount = count(new LambdaQueryWrapper<CircleMessage>()
                .eq(CircleMessage::getUserId, userId)
                .eq(CircleMessage::getIsRead, 0)
                .eq(CircleMessage::getType, CircleMessage.TYPE_FOLLOW));
        result.put("follow", (int) followCount);
        
        return result;
    }

    @Override
    public void markAsRead(Long messageId, Integer userId) {
        update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CircleMessage>()
                .eq(CircleMessage::getMessageId, messageId)
                .eq(CircleMessage::getUserId, userId)
                .set(CircleMessage::getIsRead, 1));
    }

    @Override
    public void markAllAsRead(Integer userId) {
        baseMapper.markAllAsRead(userId);
    }

    /**
     * 填充消息关联信息
     */
    private void fillMessageInfo(List<CircleMessage> messages) {
        if (messages.isEmpty()) return;
        
        // 收集用户ID和帖子ID
        Set<Integer> userIds = messages.stream().map(CircleMessage::getFromUserId).collect(Collectors.toSet());
        Set<Long> postIds = messages.stream()
                .filter(m -> m.getType() == CircleMessage.TYPE_LIKE_POST || 
                            m.getType() == CircleMessage.TYPE_COMMENT)
                .map(CircleMessage::getTargetId)
                .collect(Collectors.toSet());
        
        // 批量查询
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        Map<Long, CirclePost> postMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<CirclePost> posts = postMapper.selectBatchIds(postIds);
            postMap = posts.stream().collect(Collectors.toMap(CirclePost::getPostId, p -> p));
        }
        
        // 填充
        for (CircleMessage msg : messages) {
            msg.setFromUser(userMap.get(msg.getFromUserId()));
            if (msg.getType() == CircleMessage.TYPE_LIKE_POST || msg.getType() == CircleMessage.TYPE_COMMENT) {
                msg.setPost(postMap.get(msg.getTargetId()));
            }
        }
    }
}
