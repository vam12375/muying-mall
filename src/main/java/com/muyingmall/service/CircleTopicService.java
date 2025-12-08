package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CircleTopic;

import java.util.List;
import java.util.Map;

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

    // ==================== 后台管理接口 ====================

    /**
     * 后台管理-分页获取话题列表
     */
    Page<CircleTopic> getAdminTopicPage(int page, int size, Integer status, String keyword);

    /**
     * 按状态统计话题数量
     */
    long countByStatus(Integer status);

    /**
     * 创建话题
     */
    boolean createTopic(CircleTopic topic);

    /**
     * 更新话题状态
     */
    boolean updateTopicStatus(Integer topicId, Integer status);

    /**
     * 批量更新话题排序
     */
    boolean batchUpdateSort(List<Map<String, Integer>> sortList);
}
