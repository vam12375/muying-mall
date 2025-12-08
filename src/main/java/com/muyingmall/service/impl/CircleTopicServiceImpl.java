package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.CircleTopic;
import com.muyingmall.mapper.CircleTopicMapper;
import com.muyingmall.service.CircleTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
