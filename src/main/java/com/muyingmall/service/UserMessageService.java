package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.UserMessage;

import java.util.List;
import java.util.Map;

/**
 * 用户消息服务接口
 */
public interface UserMessageService extends IService<UserMessage> {

    /**
     * 创建用户消息
     *
     * @param userId  用户ID
     * @param type    消息类型
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   附加信息（可选）
     * @return 创建的消息对象
     */
    UserMessage createMessage(Integer userId, String type, String title, String content, String extra);

    /**
     * 获取用户消息列表
     *
     * @param userId 用户ID
     * @param type   消息类型（可选）
     * @param isRead 是否已读（可选）
     * @param page   页码
     * @param size   每页大小
     * @return 分页消息列表
     */
    IPage<UserMessage> getUserMessages(Integer userId, String type, Integer isRead, int page, int size);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadCount(Integer userId);

    /**
     * 获取用户未读消息数量（按类型统计）
     *
     * @param userId 用户ID
     * @return 按类型统计的未读消息数量
     */
    Map<String, Integer> getUnreadCountByType(Integer userId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markAsRead(String messageId);

    /**
     * 标记用户所有消息为已读
     *
     * @param userId 用户ID
     * @return 标记的消息数量
     */
    int markAllAsRead(Integer userId);

    /**
     * 标记用户特定类型的所有消息为已读
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @return 标记的消息数量
     */
    int markTypeAsRead(Integer userId, String type);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean deleteMessage(String messageId);

    /**
     * 根据用户ID删除全部消息
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteAllMessages(Integer userId);

    /**
     * 创建管理员消息（系统消息发送给所有用户）
     *
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   附加信息（可选）
     * @return 是否成功
     */
    boolean createSystemMessage(String title, String content, String extra);

    /**
     * 获取特定类型的最新消息
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @param limit  获取条数
     * @return 消息列表
     */
    List<UserMessage> getLatestMessages(Integer userId, String type, int limit);

    /**
     * 获取催发货消息列表（管理员使用）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页催发货消息列表
     */
    Page<UserMessage> getShippingReminderMessages(int page, int size);
}