package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.SearchStatistics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 搜索统计服务接口
 */
public interface SearchStatisticsService extends IService<SearchStatistics> {

    /**
     * 记录搜索统计
     * @param keyword 搜索关键词
     * @param resultCount 搜索结果数量
     * @param userId 用户ID
     * @param source 搜索来源
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param responseTime 响应时间
     */
    void recordSearch(String keyword, Long resultCount, Integer userId, 
                     String source, String ipAddress, String userAgent, Long responseTime);

    /**
     * 记录搜索点击
     * @param keyword 搜索关键词
     * @param productId 点击的商品ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void recordSearchClick(String keyword, Integer productId, Integer userId, String sessionId);

    /**
     * 获取热门搜索关键词
     * @param limit 返回数量
     * @param days 统计天数
     * @return 热门关键词列表
     */
    List<String> getHotKeywords(int limit, int days);

    /**
     * 获取搜索趋势数据
     * @param keyword 关键词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据
     */
    List<Map<String, Object>> getSearchTrend(String keyword, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取搜索统计概览
     * @param days 统计天数
     * @return 统计概览
     */
    Map<String, Object> getSearchOverview(int days);

    /**
     * 获取无结果搜索关键词
     * @param limit 返回数量
     * @param days 统计天数
     * @return 无结果关键词列表
     */
    List<Map<String, Object>> getNoResultKeywords(int limit, int days);

    /**
     * 获取用户搜索行为分析
     * @param userId 用户ID
     * @param days 统计天数
     * @return 用户搜索行为
     */
    Map<String, Object> getUserSearchBehavior(Integer userId, int days);

    /**
     * 清理过期统计数据
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredData(int days);

    /**
     * 生成搜索报告
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 搜索报告
     */
    Map<String, Object> generateSearchReport(LocalDateTime startTime, LocalDateTime endTime);
}
