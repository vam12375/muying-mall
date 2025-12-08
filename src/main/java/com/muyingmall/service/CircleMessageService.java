package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleMessage;

import java.util.Map;

/**
 * 育儿圈消息服务接口
 */
public interface CircleMessageService extends IService<CircleMessage> {

    /**
     * 创建消息通知
     */
    void createMessage(Integer userId, Integer fromUserId, Integer type, Long targetId, String content);

    /**
     * 获取用户消息列表
     */
    Page<CircleMessage> getMessagePage(Integer userId, int page, int size, Integer type);

    /**
     * 获取未读消息数量
     */
    Map<String, Integer> getUnreadCount(Integer userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Integer userId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Integer userId);
}
