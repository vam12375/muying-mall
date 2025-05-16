package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.PointsOperationLog;

import java.time.LocalDate;

/**
 * 积分操作服务接口
 * 该接口用于提取PointsService和PointsExchangeService之间的公共操作，
 * 目的是打破循环依赖关系
 */
public interface PointsOperationService extends IService<PointsOperationLog> {

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

    /**
     * 记录积分操作日志
     * 
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param pointsChange 积分变动
     * @param currentBalance 当前余额
     * @param description 描述
     * @param relatedOrderId 关联订单ID，可为null
     * @return 是否成功
     */
    boolean recordOperation(Integer userId, String operationType, Integer pointsChange, 
                           Integer currentBalance, String description, Integer relatedOrderId);
    
    /**
     * 管理员分页查询积分操作日志
     *
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID，可为空
     * @param operationType 操作类型，可为空
     * @param startDate 开始日期，可为空
     * @param endDate 结束日期，可为空
     * @return 操作日志分页
     */
    Page<PointsOperationLog> adminListOperationLogs(Integer page, Integer size, Integer userId, 
                                                  String operationType, LocalDate startDate, LocalDate endDate);
}