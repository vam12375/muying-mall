package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.mapper.UserPointsMapper;
import com.muyingmall.entity.UserPoints;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 积分操作服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointsOperationServiceImpl implements PointsOperationService {

    private final PointsHistoryMapper pointsHistoryMapper;
    private final UserPointsMapper userPointsMapper;
    private static final Logger log = LoggerFactory.getLogger(PointsOperationServiceImpl.class);

    @Override
    public Integer getUserPoints(Integer userId) {
        if (userId == null) {
            return 0;
        }
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId.longValue()));
        if (userPoints != null && userPoints.getPoints() != null) {
            return userPoints.getPoints();
        } else {
            log.warn("用户ID: {} 的积分记录不存在或积分字段为空。返回0积分。", userId);
            // 如果用户积分记录不存在，尝试创建一个，以增强 addPoints/deductPoints 的健壮性
            // 这是一种简单的方式，也可以设计一个专门的 getOrCreateUserPoints 方法
            UserPoints newUserPoints = new UserPoints();
            newUserPoints.setUserId(userId.longValue());
            newUserPoints.setPoints(0);
            newUserPoints.setLevel("普通会员"); // 默认等级
            newUserPoints.setCreateTime(LocalDateTime.now());
            newUserPoints.setUpdateTime(LocalDateTime.now());
            try {
                userPointsMapper.insert(newUserPoints);
                log.info("已为用户ID: {} 创建新的积分记录，初始积分为0。", userId);
                return 0;
            } catch (Exception e) {
                log.error("为用户ID: {} 创建积分记录失败", userId, e);
                return 0; // 创建失败依然返回0
            }
        }
    }

    private UserPoints getOrCreateUserPoints(Integer userId) {
        if (userId == null)
            return null;
        Long userLongId = userId.longValue();
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userLongId));
        if (userPoints == null) {
            log.warn("未能找到用户ID: {} 的UserPoints记录，将创建新记录。", userId);
            userPoints = new UserPoints();
            userPoints.setUserId(userLongId);
            userPoints.setPoints(0);
            userPoints.setLevel("普通会员"); // 设置默认等级
            userPoints.setCreateTime(LocalDateTime.now());
            userPoints.setUpdateTime(LocalDateTime.now());
            try {
                userPointsMapper.insert(userPoints);
                log.info("已为用户ID: {} 创建新的UserPoints记录，ID为: {}", userId, userPoints.getId());
            } catch (Exception e) {
                log.error("为用户ID: {} 插入新的UserPoints记录失败", userId, e);
                // 根据期望行为，此处可以抛出异常或返回null
                // 目前返回未持久化的对象，调用者需知晓或处理null ID的情况
                return userPoints; // 或者 throw new BusinessException("创建用户积分记录失败");
            }
        }
        return userPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        if (userId == null || points == null || points <= 0) { // 积分不能为null，确保不小于等于0
            log.warn("增加积分 - 无效参数: userId={}, points={}", userId, points);
            return false;
        }

        // 步骤1: 记录到积分历史表
        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setPoints(points);
        history.setType("earn");
        history.setSource(source);
        history.setReferenceId(referenceId);
        history.setDescription(description);
        // history.setCreateTime(LocalDateTime.now()); // 假设PointsHistory实体自己处理或由数据库设置

        boolean historyInserted = pointsHistoryMapper.insert(history) > 0;
        if (!historyInserted) {
            log.error("增加积分 - 为用户ID: {} 插入积分历史记录失败", userId);
            // 可选择抛出异常以确保事务回滚，如果历史记录非常重要
            throw new BusinessException("积分历史记录失败");
        }

        // 步骤2: 更新用户积分表
        UserPoints userPoints = getOrCreateUserPoints(userId);
        if (userPoints == null) { // 如果getOrCreateUserPoints抛出异常或健壮地处理创建失败，应该不会发生
            log.error("增加积分 - 为用户ID: {} 获取或创建积分记录失败", userId);
            throw new BusinessException("获取或创建用户积分记录失败");
        }

        int currentActualPoints = userPoints.getPoints() == null ? 0 : userPoints.getPoints();
        userPoints.setPoints(currentActualPoints + points);
        userPoints.setUpdateTime(LocalDateTime.now());

        boolean userPointsUpdated;
        if (userPoints.getId() == null) { // 这意味着getOrCreateUserPoints返回了一个由于插入失败而未持久化的对象
            log.error(
                    "增加积分 - 用户ID: {} 的UserPoints对象ID为空，无法更新。这表明getOrCreateUserPoints持久化存在问题。",
                    userId);
            throw new BusinessException("用户积分记录状态异常，无法更新");
        } else {
            userPointsUpdated = userPointsMapper.updateById(userPoints) > 0;
        }

        if (!userPointsUpdated) {
            log.error("增加积分 - 为用户ID: {} 更新user_points表失败", userId);
            throw new BusinessException("更新用户总积分失败");
        }
        log.info("增加积分 - 成功为用户ID: {} 添加 {} 积分。来源: {}。user_points表中的新总积分: {}",
                points, userId, source, userPoints.getPoints());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        if (userId == null || points == null || points <= 0) { // 扣除的积分必须为正值
            log.warn("扣减积分 - 无效参数: userId={}, points={}", userId, points);
            return false;
        }

        // 步骤1: 检查并更新user_points表中的积分余额是否足够
        UserPoints userPoints = getOrCreateUserPoints(userId);
        if (userPoints == null) { // 理想情况下，getOrCreateUserPoints应该抛出异常处理
            log.error("扣减积分 - 为用户ID: {} 获取或创建积分记录失败", userId);
            throw new BusinessException("获取或创建用户积分记录失败");
        }

        int currentActualPoints = userPoints.getPoints() == null ? 0 : userPoints.getPoints();
        if (currentActualPoints < points) {
            log.warn("扣减积分 - 用户ID: {} 积分不足。当前: {}, 需要: {}", userId,
                    currentActualPoints, points);
            throw new BusinessException("积分不足");
        }

        userPoints.setPoints(currentActualPoints - points);
        userPoints.setUpdateTime(LocalDateTime.now());

        boolean userPointsUpdated;
        if (userPoints.getId() == null) {
            log.error(
                    "扣减积分 - 用户ID: {} 的UserPoints对象ID为空，无法更新。这表明getOrCreateUserPoints持久化存在问题。",
                    userId);
            throw new BusinessException("用户积分记录状态异常，无法更新");
        } else {
            userPointsUpdated = userPointsMapper.updateById(userPoints) > 0;
        }

        if (!userPointsUpdated) {
            log.error("扣减积分 - 为用户ID: {} 更新user_points表失败", userId);
            throw new BusinessException("更新用户总积分失败（扣除）");
        }

        // 步骤2: 记录到积分历史表
        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setPoints(-points); // 记录为负值表示扣减
        history.setType("spend");
        history.setSource(source);
        history.setReferenceId(referenceId);
        history.setDescription(description);
        // history.setCreateTime(LocalDateTime.now()); // 假设实体或数据库处理此字段

        boolean historyInserted = pointsHistoryMapper.insert(history) > 0;
        if (!historyInserted) {
            log.error("扣减积分 - 为用户ID: {} 插入积分历史记录失败", userId);
            throw new BusinessException("积分历史记录失败（扣除）");
        }
        log.info(
                "扣减积分 - 成功为用户ID: {} 扣除 {} 积分。来源: {}。user_points表中的新总积分: {}",
                points, userId, source, userPoints.getPoints());
        return true;
    }
}