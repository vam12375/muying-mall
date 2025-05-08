package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.service.PointsOperationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分操作服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointsOperationServiceImpl implements PointsOperationService {

    private final PointsHistoryMapper pointsHistoryMapper;

    @Override
    public Integer getUserPoints(Integer userId) {
        if (userId == null) {
            return 0;
        }

        // 通过积分历史记录计算用户当前积分
        LambdaQueryWrapper<PointsHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsHistory::getUserId, userId);

        List<PointsHistory> historyList = pointsHistoryMapper.selectList(queryWrapper);

        return historyList.stream()
                .mapToInt(PointsHistory::getPoints)
                .sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        if (userId == null || points <= 0) {
            return false;
        }

        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setPoints(points);
        history.setType("earn");
        history.setSource(source);
        history.setReferenceId(referenceId);
        history.setDescription(description);

        return pointsHistoryMapper.insert(history) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        if (userId == null || points <= 0) {
            return false;
        }

        // 检查用户积分是否足够
        Integer userPoints = this.getUserPoints(userId);
        if (userPoints < points) {
            throw new BusinessException("积分不足");
        }

        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setPoints(-points);
        history.setType("spend");
        history.setSource(source);
        history.setReferenceId(referenceId);
        history.setDescription(description);

        return pointsHistoryMapper.insert(history) > 0;
    }
}