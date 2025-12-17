package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserMessage;
import com.muyingmall.enums.MessageType;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.mapper.UserMessageMapper;
import com.muyingmall.service.UserMessageService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户消息服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {

    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserMessage createMessage(Integer userId, String type, String title, String content, String extra) {
        // 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("创建消息失败，用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }

        // 校验消息类型是否合法
        MessageType messageType = MessageType.getByCode(type);
        if (messageType == null) {
            log.warn("创建消息失败，消息类型不合法: type={}", type);
            throw new BusinessException("消息类型不合法");
        }

        // 创建消息对象
        UserMessage message = new UserMessage();
        message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0); // 默认未读
        message.setCreateTime(LocalDateTime.now());
        message.setStatus(1); // 默认正常状态

        // 设置额外信息（如果有）
        if (StringUtils.hasText(extra)) {
            message.setExtra(extra);
        }

        // 保存消息
        boolean success = save(message);
        if (!success) {
            log.error("保存用户消息失败: userId={}, type={}", userId, type);
            throw new BusinessException("保存消息失败");
        }

        // 清除该用户的消息计数缓存
        clearMessageCountCache(userId);

        log.debug("成功创建用户消息: userId={}, messageId={}, type={}", userId, message.getMessageId(), type);
        return message;
    }

    @Override
    public IPage<UserMessage> getUserMessages(Integer userId, String type, Integer isRead, int page, int size) {
        // 构建缓存键
        StringBuilder cacheKeyBuilder = new StringBuilder(CacheConstants.USER_MESSAGES_KEY)
                .append(userId)
                .append(":page_").append(page)
                .append(":size_").append(size);

        if (StringUtils.hasText(type)) {
            cacheKeyBuilder.append(":type_").append(type);
        }

        if (isRead != null) {
            cacheKeyBuilder.append(":read_").append(isRead);
        }

        String cacheKey = cacheKeyBuilder.toString();

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户消息列表: userId={}, type={}, isRead={}, page={}, size={}",
                    userId, type, isRead, page, size);
            return (IPage<UserMessage>) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户消息列表: userId={}, type={}, isRead={}, page={}, size={}",
                userId, type, isRead, page, size);

        // 构建查询条件
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getStatus, 1); // 只查询正常状态的消息

        // 过滤系统管理员消息（userId = -1的消息只在管理员后台显示）
        queryWrapper.ne(UserMessage::getUserId, -1); // 不等于-1的消息

        // 根据类型筛选
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(UserMessage::getType, type);
        }

        // 根据是否已读筛选
        if (isRead != null) {
            queryWrapper.eq(UserMessage::getIsRead, isRead);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(UserMessage::getCreateTime);

        // 执行分页查询
        Page<UserMessage> messagePage = new Page<>(page, size);
        IPage<UserMessage> result = page(messagePage, queryWrapper);

        // 缓存结果
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            redisUtil.set(cacheKey, result, CacheConstants.MESSAGE_LIST_EXPIRE_TIME);
            log.debug("将用户消息列表缓存到Redis: userId={}, type={}, isRead={}, 缓存键={}, 过期时间={}秒",
                    userId, type, isRead, cacheKey, CacheConstants.MESSAGE_LIST_EXPIRE_TIME);
        }

        return result;
    }

    @Override
    public int getUnreadCount(Integer userId) {
        // 构建缓存键
        String cacheKey = CacheConstants.USER_UNREAD_COUNT_KEY + userId;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户未读消息数: userId={}", userId);
            return (int) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户未读消息数: userId={}", userId);

        // 构建查询条件
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getIsRead, 0); // 未读
        queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态

        int count = Math.toIntExact(count(queryWrapper));

        // 缓存结果
        redisUtil.set(cacheKey, count, CacheConstants.MESSAGE_COUNT_EXPIRE_TIME);
        log.debug("将用户未读消息数缓存到Redis: userId={}, count={}, 过期时间={}秒",
                userId, count, CacheConstants.MESSAGE_COUNT_EXPIRE_TIME);

        return count;
    }

    @Override
    public Map<String, Integer> getUnreadCountByType(Integer userId) {
        // 构建缓存键
        String cacheKey = CacheConstants.USER_UNREAD_TYPE_KEY + userId;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户未读消息类型统计: userId={}", userId);
            return (Map<String, Integer>) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户未读消息类型统计: userId={}", userId);

        // 查询所有未读消息
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getIsRead, 0); // 未读
        queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态

        List<UserMessage> unreadMessages = list(queryWrapper);

        // 按类型分组并计数
        Map<String, Integer> result = new HashMap<>();

        // 初始化所有消息类型为0
        for (MessageType type : MessageType.values()) {
            result.put(type.getCode(), 0);
        }

        // 计算各类型未读消息数量
        for (UserMessage message : unreadMessages) {
            String type = message.getType();
            result.put(type, result.getOrDefault(type, 0) + 1);
        }

        // 缓存结果
        redisUtil.set(cacheKey, result, CacheConstants.MESSAGE_COUNT_EXPIRE_TIME);
        log.debug("将用户未读消息类型统计缓存到Redis: userId={}, 过期时间={}秒",
                userId, CacheConstants.MESSAGE_COUNT_EXPIRE_TIME);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(String messageId) {
        // 查询消息是否存在
        UserMessage message = getById(messageId);
        if (message == null || message.getStatus() == 0) {
            return false;
        }

        // 如果已经是已读状态，直接返回成功
        if (message.getIsRead() == 1) {
            return true;
        }

        // 更新为已读状态
        UserMessage updateMessage = new UserMessage();
        updateMessage.setMessageId(messageId);
        updateMessage.setIsRead(1);
        updateMessage.setReadTime(LocalDateTime.now());

        boolean result = updateById(updateMessage);

        // 更新成功后，清除相关缓存
        if (result) {
            clearMessageCountCache(message.getUserId());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Integer userId) {
        // 构建更新条件
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getIsRead, 0); // 未读
        updateWrapper.eq(UserMessage::getStatus, 1); // 正常状态

        // 更新字段
        UserMessage updateMessage = new UserMessage();
        updateMessage.setIsRead(1);
        updateMessage.setReadTime(LocalDateTime.now());

        // 执行批量更新
        boolean success = update(updateMessage, updateWrapper);

        // 清除相关缓存
        clearMessageCountCache(userId);

        // 获取更新的记录数
        if (success) {
            // 由于MyBatis Plus没有直接返回影响行数的方法，所以这里再查一次已读数量
            LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserMessage::getUserId, userId);
            queryWrapper.eq(UserMessage::getIsRead, 1); // 已读
            queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态

            return Math.toIntExact(count(queryWrapper));
        }

        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markTypeAsRead(Integer userId, String type) {
        // 校验消息类型是否合法
        MessageType messageType = MessageType.getByCode(type);
        if (messageType == null) {
            log.warn("标记消息已读失败，消息类型不合法: type={}", type);
            throw new BusinessException("消息类型不合法");
        }

        // 构建更新条件
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getType, type);
        updateWrapper.eq(UserMessage::getIsRead, 0); // 未读
        updateWrapper.eq(UserMessage::getStatus, 1); // 正常状态

        // 更新字段
        UserMessage updateMessage = new UserMessage();
        updateMessage.setIsRead(1);
        updateMessage.setReadTime(LocalDateTime.now());

        // 执行批量更新
        boolean success = update(updateMessage, updateWrapper);

        // 清除相关缓存
        clearMessageCountCache(userId);

        // 获取更新的记录数
        if (success) {
            // 由于MyBatis Plus没有直接返回影响行数的方法，所以这里再查一次已读数量
            LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserMessage::getUserId, userId);
            queryWrapper.eq(UserMessage::getType, type);
            queryWrapper.eq(UserMessage::getIsRead, 1); // 已读
            queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态

            return Math.toIntExact(count(queryWrapper));
        }

        return 0;
    }

    /**
     * 清除用户消息计数相关缓存
     *
     * @param userId 用户ID
     */
    private void clearMessageCountCache(Integer userId) {
        if (userId == null) {
            return;
        }

        try {
            // 清除未读消息计数缓存
            String countCacheKey = CacheConstants.USER_UNREAD_COUNT_KEY + userId;
            redisUtil.del(countCacheKey);

            // 清除未读消息类型统计缓存
            String typeCacheKey = CacheConstants.USER_UNREAD_TYPE_KEY + userId;
            redisUtil.del(typeCacheKey);

            // 清除消息列表缓存
            // 使用模式匹配删除所有与该用户相关的消息列表缓存
            Set<String> messageListKeys = redisUtil.keys(CacheConstants.USER_MESSAGES_KEY + userId + ":*");
            if (messageListKeys != null && !messageListKeys.isEmpty()) {
                redisUtil.del(messageListKeys.toArray(new String[0]));
                log.debug("已清除用户消息列表缓存，共{}个键: userId={}", messageListKeys.size(), userId);
            }

            log.debug("已清除用户消息计数缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户消息计数缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(String messageId) {
        // 查询消息是否存在
        UserMessage message = getById(messageId);
        if (message == null) {
            return false;
        }

        // 逻辑删除（将状态设为0）
        UserMessage updateMessage = new UserMessage();
        updateMessage.setMessageId(messageId);
        updateMessage.setStatus(0);

        return updateById(updateMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllMessages(Integer userId) {
        // 构建更新条件
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getStatus, 1); // 正常状态

        // 更新字段
        UserMessage updateMessage = new UserMessage();
        updateMessage.setStatus(0);

        // 执行批量更新
        return update(updateMessage, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSystemMessage(String title, String content, String extra) {
        // 查询所有用户
        List<User> users = userMapper.selectList(null);

        // 为每个用户创建相同内容的系统消息
        for (User user : users) {
            try {
                createMessage(user.getUserId(), MessageType.SYSTEM.getCode(), title, content, extra);
            } catch (Exception e) {
                log.error("为用户创建系统消息失败: userId={}, error={}", user.getUserId(), e.getMessage(), e);
                // 继续处理下一个用户，不中断整个流程
            }
        }

        return true;
    }

    @Override
    public List<UserMessage> getLatestMessages(Integer userId, String type, int limit) {
        // 校验消息类型是否合法
        if (!StringUtils.hasText(type)) {
            throw new BusinessException("消息类型不能为空");
        }

        MessageType messageType = MessageType.getByCode(type);
        if (messageType == null) {
            log.warn("获取最新消息失败，消息类型不合法: type={}", type);
            throw new BusinessException("消息类型不合法");
        }

        // 构建查询条件
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getType, type);
        queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态
        queryWrapper.orderByDesc(UserMessage::getCreateTime);
        queryWrapper.last("LIMIT " + limit);

        return list(queryWrapper);
    }

    @Override
    public Page<UserMessage> getShippingReminderMessages(int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getType, MessageType.SHIPPING_REMINDER.getCode());
        queryWrapper.eq(UserMessage::getStatus, 1); // 正常状态
        queryWrapper.orderByDesc(UserMessage::getCreateTime);

        // 执行分页查询
        Page<UserMessage> messagePage = new Page<>(page, size);
        Page<UserMessage> resultPage = page(messagePage, queryWrapper);

        // 补充用户信息从原始用户ID（存在extra字段或其他地方）
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            // 处理额外信息，提取原始用户ID
            for (UserMessage message : resultPage.getRecords()) {
                try {
                    // 从extra字段解析原始用户信息
                    String extra = message.getExtra();
                    if (extra != null && !extra.isEmpty()) {
                        // 假设extra格式为JSON，包含orderId字段
                        JsonNode extraJson = new ObjectMapper().readTree(extra);
                        if (extraJson.has("orderId")) {
                            int orderId = extraJson.get("orderId").asInt();
                            // 可以根据orderId查询订单获取用户信息
                            // 这里省略实际查询逻辑，根据实际情况实现
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析催发货消息额外信息失败: {}", e.getMessage());
                }
            }
        }

        return resultPage;
    }
}