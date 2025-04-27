package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsReward;

import java.util.List;

/**
 * 积分奖励服务接口
 */
public interface PointsRewardService extends IService<PointsReward> {

    /**
     * 分页获取积分奖励列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param type 奖励类型
     * @return 积分奖励分页对象
     */
    Page<PointsReward> getPointsRewardPage(int page, int size, String type);

    /**
     * 获取积分奖励详情
     *
     * @param id 奖励ID
     * @return 积分奖励详情
     */
    PointsReward getPointsRewardDetail(Long id);

    /**
     * 获取推荐的积分奖励列表
     *
     * @param limit 数量限制
     * @return 推荐的积分奖励列表
     */
    List<PointsReward> getRecommendRewards(int limit);

    /**
     * 兑换积分奖励
     *
     * @param userId   用户ID
     * @param rewardId 奖励ID
     * @return 是否成功
     */
    boolean exchangeReward(Integer userId, Long rewardId);

    /**
     * 更新奖励状态
     *
     * @param id      奖励ID
     * @param visible 是否显示
     * @return 是否成功
     */
    boolean updateRewardStatus(Long id, boolean visible);
}