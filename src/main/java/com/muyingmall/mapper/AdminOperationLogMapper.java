package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员操作日志Mapper接口
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {

        /**
         * 分页查询操作日志
         */
        @Select("<script>" +
                        "SELECT * FROM admin_operation_logs " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='startTime != null'> AND create_time >= #{startTime} </if>" +
                        "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
                        "<if test='operationType != null and operationType != \"\"'> AND operation_type = #{operationType} </if>"
                        +
                        "<if test='module != null and module != \"\"'> AND module = #{module} </if>" +
                        "<if test='operationResult != null and operationResult != \"\"'> AND operation_result = #{operationResult} </if>"
                        +
                        "ORDER BY create_time DESC" +
                        "</script>")
        IPage<AdminOperationLog> selectOperationLogsPage(Page<AdminOperationLog> page,
                        @Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("operationType") String operationType,
                        @Param("module") String module,
                        @Param("operationResult") String operationResult);

        /**
         * 统计操作次数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM admin_operation_logs " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='startTime != null'> AND create_time >= #{startTime} </if>" +
                        "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
                        "<if test='operationType != null and operationType != \"\"'> AND operation_type = #{operationType} </if>"
                        +
                        "</script>")
        Long countOperations(@Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("operationType") String operationType);

        /**
         * 获取操作类型分布统计
         */
        @Select("<script>" +
                        "SELECT " +
                        "operation_type, " +
                        "COUNT(*) as count, " +
                        "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM admin_operation_logs " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND create_time >= DATE_ADD(CURDATE(), INTERVAL -#{days} DAY) </if>" +
                        "), 2) as percentage " +
                        "FROM admin_operation_logs " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND create_time >= DATE_ADD(CURDATE(), INTERVAL -#{days} DAY) </if>" +
                        "GROUP BY operation_type " +
                        "ORDER BY count DESC" +
                        "</script>")
        List<Map<String, Object>> selectOperationTypeStats(@Param("adminId") Integer adminId,
                        @Param("days") Integer days);

        /**
         * 获取模块操作统计
         */
        @Select("<script>" +
                        "SELECT " +
                        "module, " +
                        "COUNT(*) as count " +
                        "FROM admin_operation_logs " +
                        "WHERE 1=1 " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "<if test='days != null'> AND create_time >= DATE_ADD(CURDATE(), INTERVAL -#{days} DAY) </if>" +
                        "GROUP BY module " +
                        "ORDER BY count DESC" +
                        "</script>")
        List<Map<String, Object>> selectModuleStats(@Param("adminId") Integer adminId,
                        @Param("days") Integer days);

        /**
         * 获取最近操作记录
         */
        @Select("SELECT * FROM admin_operation_logs " +
                        "WHERE admin_id = #{adminId} " +
                        "ORDER BY create_time DESC LIMIT #{limit}")
        List<AdminOperationLog> selectRecentOperations(@Param("adminId") Integer adminId,
                        @Param("limit") Integer limit);

        /**
         * 获取操作统计数据
         */
        @Select("<script>" +
                        "SELECT " +
                        "DATE(create_time) as date, " +
                        "COUNT(*) as count, " +
                        "COUNT(CASE WHEN operation_result = 'success' THEN 1 END) as success_count, " +
                        "COUNT(CASE WHEN operation_result = 'failed' THEN 1 END) as failed_count " +
                        "FROM admin_operation_logs " +
                        "WHERE create_time >= #{startTime} AND create_time &lt;= #{endTime} " +
                        "<if test='adminId != null'> AND admin_id = #{adminId} </if>" +
                        "GROUP BY DATE(create_time) " +
                        "ORDER BY date DESC" +
                        "</script>")
        List<Map<String, Object>> selectOperationStatistics(@Param("adminId") Integer adminId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}
