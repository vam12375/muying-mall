package com.muyingmall.service;

import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.AdminOperationLog;

/**
 * 管理员实时通知服务接口
 */
public interface AdminRealtimeNotificationService {

    /**
     * 通知新的登录记录
     *
     * @param loginRecord 登录记录
     */
    void notifyNewLoginRecord(AdminLoginRecord loginRecord);

    /**
     * 通知新的操作日志
     *
     * @param operationLog 操作日志
     */
    void notifyNewOperationLog(AdminOperationLog operationLog);

    /**
     * 通知统计数据更新
     *
     * @param adminId 管理员ID（可选，为null时广播给所有在线管理员）
     */
    void notifyStatsUpdate(Integer adminId);

    /**
     * 通知管理员上线
     *
     * @param adminId 管理员ID
     * @param adminName 管理员用户名
     */
    void notifyAdminOnline(Integer adminId, String adminName);

    /**
     * 通知管理员下线
     *
     * @param adminId 管理员ID
     * @param adminName 管理员用户名
     */
    void notifyAdminOffline(Integer adminId, String adminName);

    /**
     * 发送系统通知
     *
     * @param message 通知消息
     * @param adminId 目标管理员ID（可选，为null时广播给所有在线管理员）
     */
    void sendSystemNotification(String message, Integer adminId);
}
