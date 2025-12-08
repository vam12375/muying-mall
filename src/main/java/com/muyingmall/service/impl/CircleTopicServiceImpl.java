package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleTopic;
import com.muyingmall.mapper.CircleTopicMapper;
import com.muyingmall.service.CircleTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 育儿圈话题服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CircleTopicServiceImpl extends ServiceImpl<CircleTopicMapper, CircleTopic> implements CircleTopicService {

    @Override
    public List<CircleTopic> getActiveTopics() {
        return list(new LambdaQueryWrapper<CircleTopic>()
                .eq(CircleTopic::getStatus, 1)
                .orderByAsc(CircleTopic::getSortOrder));
    }

    @Override
    public List<CircleTopic> getHotTopics(int limit) {
        return list(new LambdaQueryWrapper<CircleTopic>()
                .eq(CircleTopic::getStatus, 1)
                .orderByDesc(CircleTopic::getPostCount)
                .last("LIMIT " + limit));
    }

    @Override
    public CircleTopic getTopicById(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        return getById(topicId);
    }

    // ==================== 后台管理接口实现 ====================

    @Override
    public Page<CircleTopic> getAdminTopicPage(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<CircleTopic> wrapper = new LambdaQueryWrapper<CircleTopic>()
                .eq(status != null, CircleTopic::getStatus, status)
                .like(keyword != null && !keyword.isEmpty(), CircleTopic::getName, keyword)
                .orderByAsc(CircleTopic::getSortOrder)
                .orderByDesc(CircleTopic::getCreateTime);
        
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public long countByStatus(Integer status) {
        return count(new LambdaQueryWrapper<CircleTopic>().eq(CircleTopic::getStatus, status));
    }

    @Override
    public boolean createTopic(CircleTopic topic) {
        topic.setPostCount(0);
        topic.setFollowCount(0);
        topic.setCreateTime(LocalDateTime.now());
        topic.setUpdateTime(LocalDateTime.now());
        if (topic.getSortOrder() == null) {
            topic.setSortOrder(0);
        }
        if (topic.getStatus() == null) {
            topic.setStatus(1);
        }
        return save(topic);
    }

    @Override
    public boolean updateTopicStatus(Integer topicId, Integer status) {
        CircleTopic topic = new CircleTopic();
        topic.setTopicId(topicId);
        topic.setStatus(status);
        topic.setUpdateTime(LocalDateTime.now());
        return updateById(topic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateSort(List<Map<String, Integer>> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return false;
        }
        for (Map<String, Integer> item : sortList) {
            Integer topicId = item.get("topicId");
            Integer sortOrder = item.get("sortOrder");
            if (topicId != null && sortOrder != null) {
                CircleTopic topic = new CircleTopic();
                topic.setTopicId(topicId);
                topic.setSortOrder(sortOrder);
                topic.setUpdateTime(LocalDateTime.now());
                updateById(topic);
            }
        }
        return true;
    }
}
