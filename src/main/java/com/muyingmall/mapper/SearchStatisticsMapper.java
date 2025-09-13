package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.SearchStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 搜索统计Mapper接口
 */
@Mapper
public interface SearchStatisticsMapper extends BaseMapper<SearchStatistics> {

    /**
     * 获取热门搜索关键词
     * @param limit 返回数量
     * @param days 统计天数
     * @return 热门关键词列表
     */
    @Select("SELECT keyword, SUM(search_count) as total_count " +
            "FROM search_statistics " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY keyword " +
            "ORDER BY total_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotKeywords(@Param("limit") int limit, @Param("days") int days);

    /**
     * 获取搜索趋势数据
     * @param keyword 关键词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据
     */
    @Select("SELECT DATE(search_time) as search_date, SUM(search_count) as daily_count " +
            "FROM search_statistics " +
            "WHERE keyword = #{keyword} " +
            "AND search_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(search_time) " +
            "ORDER BY search_date")
    List<Map<String, Object>> getSearchTrend(@Param("keyword") String keyword, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 获取搜索统计概览
     * @param days 统计天数
     * @return 统计概览
     */
    @Select("SELECT " +
            "COUNT(DISTINCT keyword) as unique_keywords, " +
            "SUM(search_count) as total_searches, " +
            "AVG(result_count) as avg_results, " +
            "AVG(response_time) as avg_response_time " +
            "FROM search_statistics " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Map<String, Object> getSearchOverview(@Param("days") int days);

    /**
     * 获取无结果搜索关键词
     * @param limit 返回数量
     * @param days 统计天数
     * @return 无结果关键词列表
     */
    @Select("SELECT keyword, SUM(search_count) as search_count " +
            "FROM search_statistics " +
            "WHERE result_count = 0 " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY keyword " +
            "ORDER BY search_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getNoResultKeywords(@Param("limit") int limit, @Param("days") int days);

    /**
     * 获取用户搜索行为分析
     * @param userId 用户ID
     * @param days 统计天数
     * @return 用户搜索行为
     */
    @Select("SELECT " +
            "COUNT(*) as search_times, " +
            "COUNT(DISTINCT keyword) as unique_keywords, " +
            "AVG(result_count) as avg_results, " +
            "SUM(CASE WHEN has_click = 1 THEN 1 ELSE 0 END) as click_count " +
            "FROM search_statistics " +
            "WHERE user_id = #{userId} " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Map<String, Object> getUserSearchBehavior(@Param("userId") Integer userId, @Param("days") int days);
}
