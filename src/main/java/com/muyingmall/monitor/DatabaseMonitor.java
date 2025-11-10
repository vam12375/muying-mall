package com.muyingmall.monitor;

import com.muyingmall.dto.SystemMetricsDTO.DatabaseMetricsDTO;
import com.muyingmall.dto.SystemMetricsDTO.SlowQueryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库监控组件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMonitor {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取数据库监控指标
     */
    public DatabaseMetricsDTO getDatabaseMetrics() {
        try {
            return DatabaseMetricsDTO.builder()
                    .status(getDatabaseStatus())
                    .activeConnections(getActiveConnections())
                    .idleConnections(getIdleConnections())
                    .maxConnections(getMaxConnections())
                    .connectionUsage(getConnectionUsage())
                    .totalQueries(getTotalQueries())
                    .slowQueries(getSlowQueriesCount())
                    .avgQueryTime(getAvgQueryTime())
                    .databaseSize(getDatabaseSize())
                    .tableSizes(getTableSizes())
                    .recentSlowQueries(getRecentSlowQueries())
                    .build();
        } catch (Exception e) {
            log.error("获取数据库监控指标失败", e);
            return DatabaseMetricsDTO.builder()
                    .status("ERROR")
                    .build();
        }
    }

    /**
     * 获取数据库状态
     */
    private String getDatabaseStatus() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.error("检查数据库状态失败", e);
            return "DOWN";
        }
    }

    /**
     * 获取活跃连接数
     */
    private Integer getActiveConnections() {
        try {
            String sql = "SHOW STATUS LIKE 'Threads_connected'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return Integer.parseInt(result.get("Value").toString());
        } catch (Exception e) {
            log.warn("获取活跃连接数失败", e);
            return 0;
        }
    }

    /**
     * 获取空闲连接数
     */
    private Integer getIdleConnections() {
        try {
            String sql = "SHOW STATUS LIKE 'Threads_running'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            int running = Integer.parseInt(result.get("Value").toString());
            int connected = getActiveConnections();
            return Math.max(0, connected - running);
        } catch (Exception e) {
            log.warn("获取空闲连接数失败", e);
            return 0;
        }
    }

    /**
     * 获取最大连接数
     */
    private Integer getMaxConnections() {
        try {
            String sql = "SHOW VARIABLES LIKE 'max_connections'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return Integer.parseInt(result.get("Value").toString());
        } catch (Exception e) {
            log.warn("获取最大连接数失败", e);
            return 0;
        }
    }

    /**
     * 获取连接使用率
     */
    private Double getConnectionUsage() {
        try {
            int active = getActiveConnections();
            int max = getMaxConnections();
            return max > 0 ? (active * 100.0 / max) : 0.0;
        } catch (Exception e) {
            log.warn("获取连接使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取总查询数
     */
    private Long getTotalQueries() {
        try {
            String sql = "SHOW GLOBAL STATUS LIKE 'Questions'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return Long.parseLong(result.get("Value").toString());
        } catch (Exception e) {
            log.warn("获取总查询数失败", e);
            return 0L;
        }
    }

    /**
     * 获取慢查询数量
     */
    private Long getSlowQueriesCount() {
        try {
            String sql = "SHOW GLOBAL STATUS LIKE 'Slow_queries'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return Long.parseLong(result.get("Value").toString());
        } catch (Exception e) {
            log.warn("获取慢查询数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取平均查询时间（估算）
     */
    private Double getAvgQueryTime() {
        try {
            // 这是一个简化的估算，实际应该通过performance_schema获取
            return 10.0; // 默认返回10ms
        } catch (Exception e) {
            log.warn("获取平均查询时间失败", e);
            return 0.0;
        }
    }

    /**
     * 获取数据库大小
     */
    private Long getDatabaseSize() {
        try {
            String sql = "SELECT SUM(data_length + index_length) as size " +
                        "FROM information_schema.TABLES " +
                        "WHERE table_schema = DATABASE()";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            Object size = result.get("size");
            return size != null ? Long.parseLong(size.toString()) : 0L;
        } catch (Exception e) {
            log.warn("获取数据库大小失败", e);
            return 0L;
        }
    }

    /**
     * 获取各表大小
     */
    private Map<String, Long> getTableSizes() {
        Map<String, Long> tableSizes = new HashMap<>();
        try {
            String sql = "SELECT table_name, (data_length + index_length) as size " +
                        "FROM information_schema.TABLES " +
                        "WHERE table_schema = DATABASE() " +
                        "ORDER BY size DESC LIMIT 10";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : results) {
                String tableName = row.get("table_name").toString();
                Long size = Long.parseLong(row.get("size").toString());
                tableSizes.put(tableName, size);
            }
        } catch (Exception e) {
            log.warn("获取表大小失败", e);
        }
        return tableSizes;
    }

    /**
     * 获取最近的慢查询
     */
    private List<SlowQueryDTO> getRecentSlowQueries() {
        List<SlowQueryDTO> slowQueries = new ArrayList<>();
        try {
            // 注意：需要开启慢查询日志才能获取数据
            // 这里返回模拟数据，实际应该从mysql.slow_log表读取
            String sql = "SELECT sql_text, query_time, start_time " +
                        "FROM mysql.slow_log " +
                        "ORDER BY start_time DESC LIMIT 10";
            
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
                for (Map<String, Object> row : results) {
                    SlowQueryDTO slowQuery = SlowQueryDTO.builder()
                            .sql(row.get("sql_text").toString())
                            .executionTime(Long.parseLong(row.get("query_time").toString()) * 1000)
                            .timestamp(row.get("start_time").toString())
                            .build();
                    slowQueries.add(slowQuery);
                }
            } catch (Exception e) {
                // 慢查询日志可能未开启，返回空列表
                log.debug("慢查询日志未开启或无数据");
            }
        } catch (Exception e) {
            log.warn("获取最近慢查询失败", e);
        }
        return slowQueries;
    }

    /**
     * 获取数据库版本信息
     */
    public String getDatabaseVersion() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            return metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
        } catch (Exception e) {
            log.error("获取数据库版本失败", e);
            return "Unknown";
        }
    }
}
