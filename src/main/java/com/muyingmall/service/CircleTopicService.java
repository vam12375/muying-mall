package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleTopic;

import java.util.List;

/**
 * 育儿圈话题服务接口
 */
public interface CircleTopicService extends IService<CircleTopic> {

    /**
     * 获取所有启用的话题
     */
    List<CircleTopic> getActiveTopics();

    /**
     * 获取热门话题
     */
    List<CircleTopic> getHotTopics(int limit);

    /**
     * 根据ID获取话题
     */
    CircleTopic getTopicById(Integer topicId);
}
