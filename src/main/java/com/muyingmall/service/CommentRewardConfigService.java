package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CommentRewardConfig;
import com.muyingmall.entity.Comment;

import java.util.Map;

/**
 * 评价奖励配置服务接口
 */
public interface CommentRewardConfigService extends IService<CommentRewardConfig> {

    /**
     * 获取评价奖励配置
     *
     * @return 奖励配置列表
     */
    CommentRewardConfig getActiveRewardConfig();

    /**
     * 计算评价可获得的奖励
     *
     * @param comment 评价对象
     * @return 奖励信息，包含奖励类型和值
     */
    Map<String, Object> calculateReward(Comment comment);

    /**
     * 发放评价奖励
     *
     * @param comment 评价对象
     * @return 奖励结果，包含奖励类型、值和是否成功
     */
    Map<String, Object> grantReward(Comment comment);
}