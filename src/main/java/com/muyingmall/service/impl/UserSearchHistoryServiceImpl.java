package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.UserSearchHistory;
import com.muyingmall.mapper.UserSearchHistoryMapper;
import com.muyingmall.service.UserSearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户搜索历史服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchHistoryServiceImpl extends ServiceImpl<UserSearchHistoryMapper, UserSearchHistory>
        implements UserSearchHistoryService {

    @Override
    @Async
    public void recordSearchHistory(Integer userId, String keyword) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }

        try {
            // 清理关键词（去除首尾空格，限制长度）
            keyword = keyword.trim();
            if (keyword.length() > 200) {
                keyword = keyword.substring(0, 200);
            }

            // 查找是否已存在相同关键词的记录
            LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserSearchHistory::getUserId, userId)
                       .eq(UserSearchHistory::getKeyword, keyword);

            UserSearchHistory existing = getOne(queryWrapper);

            if (existing != null) {
                // 更新现有记录
                existing.setSearchCount(existing.getSearchCount() + 1);
                existing.setLastSearchTime(LocalDateTime.now());
                updateById(existing);
            } else {
                // 创建新记录
                UserSearchHistory history = new UserSearchHistory();
                history.setUserId(userId);
                history.setKeyword(keyword);
                history.setSearchCount(1);
                history.setLastSearchTime(LocalDateTime.now());
                save(history);

                // 限制用户搜索历史数量（最多保留50条）
                cleanOldHistory(userId, 50);
            }

            log.debug("记录用户搜索历史: userId={}, keyword={}", userId, keyword);

        } catch (Exception e) {
            log.error("记录用户搜索历史失败: userId={}, keyword={}, error={}", userId, keyword, e.getMessage());
        }
    }

    @Override
    public List<String> getUserSearchHistory(Integer userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            return baseMapper.getUserSearchKeywords(userId, limit);
        } catch (Exception e) {
            log.error("获取用户搜索历史失败: userId={}, error={}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deleteSearchHistory(Integer userId, String keyword) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return false;
        }

        try {
            LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserSearchHistory::getUserId, userId)
                       .eq(UserSearchHistory::getKeyword, keyword);
            return remove(queryWrapper);
        } catch (Exception e) {
            log.error("删除用户搜索历史失败: userId={}, keyword={}, error={}", userId, keyword, e.getMessage());
            return false;
        }
    }

    @Override
    public int clearSearchHistory(Integer userId) {
        if (userId == null) {
            return 0;
        }

        try {
            LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserSearchHistory::getUserId, userId);
            
            long count = count(queryWrapper);
            remove(queryWrapper);
            
            log.debug("清空用户搜索历史: userId={}, count={}", userId, count);
            return (int) count;
        } catch (Exception e) {
            log.error("清空用户搜索历史失败: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 清理用户旧的搜索历史（保留最近的N条）
     * @param userId 用户ID
     * @param keepCount 保留数量
     */
    private void cleanOldHistory(Integer userId, int keepCount) {
        try {
            // 获取用户搜索历史总数
            LambdaQueryWrapper<UserSearchHistory> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(UserSearchHistory::getUserId, userId);
            long totalCount = count(countWrapper);

            if (totalCount > keepCount) {
                // 获取需要删除的记录ID
                LambdaQueryWrapper<UserSearchHistory> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserSearchHistory::getUserId, userId)
                           .orderByAsc(UserSearchHistory::getLastSearchTime)
                           .last("LIMIT " + (totalCount - keepCount));

                List<UserSearchHistory> oldRecords = list(queryWrapper);
                if (!oldRecords.isEmpty()) {
                    List<Long> idsToDelete = oldRecords.stream()
                            .map(UserSearchHistory::getId)
                            .toList();
                    removeByIds(idsToDelete);
                    log.debug("清理用户旧搜索历史: userId={}, deleted={}", userId, idsToDelete.size());
                }
            }
        } catch (Exception e) {
            log.error("清理用户旧搜索历史失败: userId={}, error={}", userId, e.getMessage());
        }
    }
}
