package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.SearchStatistics;
import com.muyingmall.mapper.SearchStatisticsMapper;
import com.muyingmall.service.SearchStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchStatisticsServiceImpl extends ServiceImpl<SearchStatisticsMapper, SearchStatistics> 
        implements SearchStatisticsService {

    @Override
    @Async
    public void recordSearch(String keyword, Long resultCount, Integer userId, 
                           String source, String ipAddress, String userAgent, Long responseTime) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        try {
            // 查找是否已存在相同关键词的记录（当天）
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            LambdaQueryWrapper<SearchStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchStatistics::getKeyword, keyword)
                       .between(SearchStatistics::getCreateTime, startOfDay, endOfDay);

            if (userId != null) {
                queryWrapper.eq(SearchStatistics::getUserId, userId);
            }

            SearchStatistics existing = getOne(queryWrapper);

            if (existing != null) {
                // 更新现有记录
                existing.setSearchCount(existing.getSearchCount() + 1);
                existing.setResultCount(resultCount);
                existing.setSearchTime(LocalDateTime.now());
                if (responseTime != null) {
                    // 计算平均响应时间
                    long avgResponseTime = (existing.getResponseTime() + responseTime) / 2;
                    existing.setResponseTime(avgResponseTime);
                }
                updateById(existing);
            } else {
                // 创建新记录
                SearchStatistics statistics = new SearchStatistics();
                statistics.setKeyword(keyword);
                statistics.setSearchCount(1);
                statistics.setResultCount(resultCount);
                statistics.setUserId(userId);
                statistics.setSource(source != null ? source : "web");
                statistics.setIpAddress(ipAddress);
                statistics.setUserAgent(userAgent);
                statistics.setSearchTime(LocalDateTime.now());
                statistics.setResponseTime(responseTime);
                statistics.setHasClick(false);
                save(statistics);
            }

            log.debug("记录搜索统计: keyword={}, resultCount={}, userId={}", keyword, resultCount, userId);

        } catch (Exception e) {
            log.error("记录搜索统计失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void recordSearchClick(String keyword, Integer productId, Integer userId, String sessionId) {
        if (!StringUtils.hasText(keyword) || productId == null) {
            return;
        }

        try {
            // 查找最近的搜索记录并更新点击信息
            LambdaQueryWrapper<SearchStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchStatistics::getKeyword, keyword)
                       .orderByDesc(SearchStatistics::getSearchTime)
                       .last("LIMIT 1");

            if (userId != null) {
                queryWrapper.eq(SearchStatistics::getUserId, userId);
            }

            SearchStatistics statistics = getOne(queryWrapper);
            if (statistics != null) {
                statistics.setHasClick(true);
                statistics.setClickedProductId(productId);
                statistics.setSessionId(sessionId);
                updateById(statistics);
            }

            log.debug("记录搜索点击: keyword={}, productId={}, userId={}", keyword, productId, userId);

        } catch (Exception e) {
            log.error("记录搜索点击失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<String> getHotKeywords(int limit, int days) {
        try {
            List<Map<String, Object>> results = baseMapper.getHotKeywords(limit, days);
            return results.stream()
                    .map(map -> (String) map.get("keyword"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getSearchTrend(String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return baseMapper.getSearchTrend(keyword, startTime, endTime);
        } catch (Exception e) {
            log.error("获取搜索趋势数据失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getSearchOverview(int days) {
        try {
            return baseMapper.getSearchOverview(days);
        } catch (Exception e) {
            log.error("获取搜索统计概览失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getNoResultKeywords(int limit, int days) {
        try {
            return baseMapper.getNoResultKeywords(limit, days);
        } catch (Exception e) {
            log.error("获取无结果搜索关键词失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getUserSearchBehavior(Integer userId, int days) {
        try {
            return baseMapper.getUserSearchBehavior(userId, days);
        } catch (Exception e) {
            log.error("获取用户搜索行为分析失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public int cleanExpiredData(int days) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
            LambdaQueryWrapper<SearchStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(SearchStatistics::getCreateTime, expireTime);
            
            int count = Math.toIntExact(count(queryWrapper));
            remove(queryWrapper);
            
            log.info("清理过期搜索统计数据: {} 条记录", count);
            return count;
            
        } catch (Exception e) {
            log.error("清理过期搜索统计数据失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> generateSearchReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // 基础统计
            LambdaQueryWrapper<SearchStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(SearchStatistics::getCreateTime, startTime, endTime);
            
            List<SearchStatistics> statistics = list(queryWrapper);
            
            // 总搜索次数
            int totalSearches = statistics.stream().mapToInt(SearchStatistics::getSearchCount).sum();
            report.put("totalSearches", totalSearches);
            
            // 独特关键词数量
            long uniqueKeywords = statistics.stream().map(SearchStatistics::getKeyword).distinct().count();
            report.put("uniqueKeywords", uniqueKeywords);
            
            // 平均结果数量
            double avgResults = statistics.stream().mapToLong(SearchStatistics::getResultCount).average().orElse(0);
            report.put("avgResults", avgResults);
            
            // 平均响应时间
            double avgResponseTime = statistics.stream()
                    .filter(s -> s.getResponseTime() != null)
                    .mapToLong(SearchStatistics::getResponseTime)
                    .average().orElse(0);
            report.put("avgResponseTime", avgResponseTime);
            
            // 点击率
            long clickCount = statistics.stream().mapToLong(s -> s.getHasClick() ? 1 : 0).sum();
            double clickRate = totalSearches > 0 ? (double) clickCount / totalSearches * 100 : 0;
            report.put("clickRate", clickRate);
            
            // 无结果搜索率
            long noResultCount = statistics.stream().mapToLong(s -> s.getResultCount() == 0 ? 1 : 0).sum();
            double noResultRate = totalSearches > 0 ? (double) noResultCount / totalSearches * 100 : 0;
            report.put("noResultRate", noResultRate);
            
            report.put("reportTime", LocalDateTime.now());
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            
        } catch (Exception e) {
            log.error("生成搜索报告失败: {}", e.getMessage(), e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }
}
