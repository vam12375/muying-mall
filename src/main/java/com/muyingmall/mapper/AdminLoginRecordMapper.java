package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.AdminLoginRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员登录记录Mapper接口
 */
@Mapper
public interface AdminLoginRecordMapper extends BaseMapper<AdminLoginRecord> {

        /**
         * 分页查询登录记录
         */
        @Select("<script>" +
                        "SELECT * FROM admin_login_records " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='startTime != null'> AND login_time >= #{startTime} </if>" +
                        "<if test='endTime != null'> AND login_time &lt;= #{endTime} </if>" +
                        "<if test='loginStatus != null and loginStatus != \"\"'> AND login_status = #{loginStatus} </if>"
                        +
                        "<if test='ipAddress != null and ipAddress != \"\"'> AND ip_address LIKE CONCAT('%', #{ipAddress}, '%') </if>"
                        +
                        "ORDER BY login_time DESC" +
                        "</script>")
        IPage<AdminLoginRecord> selectLoginRecordsPage(Page<AdminLoginRecord> page,
                        @Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("loginStatus") String loginStatus,
                        @Param("ipAddress") String ipAddress);

        /**
         * 统计登录次数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM admin_login_records " +
                        "WHERE login_status = 'success' " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='startTime != null'> AND login_time >= #{startTime} </if>" +
                        "<if test='endTime != null'> AND login_time &lt;= #{endTime} </if>" +
                        "</script>")
        Long countSuccessLogins(@Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取最近登录记录
         */
        @Select("SELECT * FROM admin_login_records " +
                        "WHERE admin_id = #{adminId} AND login_status = 'success' " +
                        "ORDER BY login_time DESC LIMIT #{limit}")
        List<AdminLoginRecord> selectRecentLogins(@Param("adminId") Integer adminId,
                        @Param("limit") Integer limit);

        /**
         * 获取登录统计数据
         */
        @Select("SELECT " +
                        "DATE(login_time) as date, " +
                        "COUNT(*) as count, " +
                        "COUNT(CASE WHEN login_status = 'success' THEN 1 END) as success_count, " +
                        "COUNT(CASE WHEN login_status = 'failed' THEN 1 END) as failed_count " +
                        "FROM admin_login_records " +
                        "WHERE login_time >= #{startTime} AND login_time <= #{endTime} " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "GROUP BY DATE(login_time) " +
                        "ORDER BY date DESC")
        List<Map<String, Object>> selectLoginStatistics(@Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取24小时活跃度统计
         */
        @Select("<script>" +
                        "SELECT " +
                        "HOUR(login_time) as hour, " +
                        "COUNT(*) as count " +
                        "FROM admin_login_records " +
                        "WHERE login_status = 'success' " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND login_time >= DATE_ADD(CURDATE(), INTERVAL -#{days} DAY) </if>" +
                        "GROUP BY HOUR(login_time) " +
                        "ORDER BY hour" +
                        "</script>")
        List<Map<String, Object>> selectHourlyLoginStats(@Param("adminId") Integer adminId,
                        @Param("days") Integer days);

        /**
         * 获取平均在线时长
         */
        @Select("SELECT AVG(duration_seconds) as avg_duration " +
                        "FROM admin_login_records " +
                        "WHERE duration_seconds IS NOT NULL AND login_status = 'success' " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND login_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) </if>")
        Double selectAvgOnlineTime(@Param("adminId") Integer adminId,
                        @Param("days") Integer days);

        /**
         * 获取最长会话时长
         */
        @Select("SELECT MAX(duration_seconds) as max_duration " +
                        "FROM admin_login_records " +
                        "WHERE duration_seconds IS NOT NULL AND login_status = 'success' " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND login_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) </if>")
        Integer selectMaxSessionTime(@Param("adminId") Integer adminId,
                        @Param("days") Integer days);
}
