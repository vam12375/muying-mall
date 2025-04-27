package com.muyingmall.service;

/**
 * 积分操作服务接口
 * 该接口用于提取PointsService和PointsExchangeService之间的公共操作，
 * 目的是打破循环依赖关系
 */
public interface PointsOperationService {

    /**
     * 获取用户积分
     *
     * @param userId 用户ID
     * @return 积分数量
     */
    Integer getUserPoints(Integer userId);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description);

    /**
     * 减少用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param source 积分来源
     * @param referenceId 关联ID
     * @param description 描述
     * @return 是否成功
     */
    boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description);
}