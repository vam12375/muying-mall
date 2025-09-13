package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.AdminLoginRecord;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员登录记录服务接口
 */
public interface AdminLoginRecordService extends IService<AdminLoginRecord> {

    /**
     * 记录登录信息
     *
     * @param adminId       管理员ID
     * @param adminName     管理员用户名
     * @param request       HTTP请求对象
     * @param loginStatus   登录状态
     * @param failureReason 失败原因（可选）
     * @return 登录记录ID
     */
    Long recordLogin(Integer adminId, String adminName, HttpServletRequest request,
            String loginStatus, String failureReason);

    /**
     * 记录登出信息
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean recordLogout(String sessionId);

    /**
     * 更新会话时长
     *
     * @param sessionId       会话ID
     * @param durationSeconds 会话时长（秒）
     * @return 是否成功
     */
    boolean updateSessionDuration(String sessionId, Integer durationSeconds);

    /**
     * 分页查询登录记录
     *
     * @param page        页码
     * @param size        每页大小
     * @param adminId     管理员ID（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param loginStatus 登录状态（可选）
     * @param ipAddress   IP地址（可选）
     * @return 分页结果
     */
    IPage<AdminLoginRecord> getLoginRecordsPage(Integer page, Integer size, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String loginStatus, String ipAddress);

    /**
     * 获取登录统计信息
     *
     * @param adminId 管理员ID（可选）
     * @param days    统计天数
     * @return 统计信息
     */
    Map<String, Object> getLoginStatistics(Integer adminId, Integer days);

    /**
     * 获取最近登录记录
     *
     * @param adminId 管理员ID
     * @param limit   记录数量
     * @return 登录记录列表
     */
    List<AdminLoginRecord> getRecentLogins(Integer adminId, Integer limit);

    /**
     * 获取24小时活跃度统计
     *
     * @param adminId 管理员ID（可选）
     * @param days    统计天数
     * @return 24小时活跃度数据
     */
    int[] getHourlyActiveStats(Integer adminId, Integer days);

    /**
     * 从请求中解析设备信息
     *
     * @param userAgent 用户代理字符串
     * @return 设备信息Map
     */
    Map<String, String> parseDeviceInfo(String userAgent);

    /**
     * 根据IP获取地理位置
     *
     * @param ipAddress IP地址
     * @return 地理位置
     */
    String getLocationByIp(String ipAddress);

    /**
     * 获取客户端真实IP
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    String getClientIpAddress(HttpServletRequest request);
}
