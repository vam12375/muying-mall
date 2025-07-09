package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.AdminOnlineStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员在线状态Mapper接口
 */
@Mapper
public interface AdminOnlineStatusMapper extends BaseMapper<AdminOnlineStatus> {

    /**
     * 根据管理员ID查询在线状态
     */
    @Select("SELECT * FROM admin_online_status WHERE admin_id = #{adminId}")
    AdminOnlineStatus selectByAdminId(@Param("adminId") Integer adminId);

    /**
     * 根据会话ID查询在线状态
     */
    @Select("SELECT * FROM admin_online_status WHERE session_id = #{sessionId}")
    AdminOnlineStatus selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 更新最后活动时间
     */
    @Update("UPDATE admin_online_status SET last_activity_time = #{lastActivityTime} " +
            "WHERE admin_id = #{adminId}")
    int updateLastActivityTime(@Param("adminId") Integer adminId,
                              @Param("lastActivityTime") LocalDateTime lastActivityTime);

    /**
     * 设置离线状态
     */
    @Update("UPDATE admin_online_status SET is_online = 0 WHERE admin_id = #{adminId}")
    int setOffline(@Param("adminId") Integer adminId);

    /**
     * 设置离线状态（根据会话ID）
     */
    @Update("UPDATE admin_online_status SET is_online = 0 WHERE session_id = #{sessionId}")
    int setOfflineBySessionId(@Param("sessionId") String sessionId);

    /**
     * 清理超时的在线状态
     */
    @Update("UPDATE admin_online_status SET is_online = 0 " +
            "WHERE is_online = 1 AND last_activity_time < #{timeoutTime}")
    int clearTimeoutStatus(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 获取当前在线管理员列表
     */
    @Select("SELECT * FROM admin_online_status WHERE is_online = 1 ORDER BY last_activity_time DESC")
    List<AdminOnlineStatus> selectOnlineAdmins();

    /**
     * 统计在线管理员数量
     */
    @Select("SELECT COUNT(*) FROM admin_online_status WHERE is_online = 1")
    Long countOnlineAdmins();
}
