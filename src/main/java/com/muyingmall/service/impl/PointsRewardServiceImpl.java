package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.PointsReward;
import com.muyingmall.mapper.PointsRewardMapper;
import com.muyingmall.service.CouponService;
import com.muyingmall.service.PointsRewardService;
import com.muyingmall.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分奖励服务实现类
 */
@Service
@RequiredArgsConstructor
public class PointsRewardServiceImpl extends ServiceImpl<PointsRewardMapper, PointsReward>
        implements PointsRewardService {

    private final PointsService pointsService;
    private final CouponService couponService;

    @Override
    public Page<PointsReward> getPointsRewardPage(int page, int size, String type) {
        Page<PointsReward> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsReward> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsReward::getVisible, 1); // 只查询可见的奖励

        // 如果指定了类型，则按类型筛选
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(PointsReward::getType, type);
        }

        // 只查询有效期内的奖励
        LocalDateTime now = LocalDateTime.now();
        queryWrapper
                .and(wrapper -> wrapper.isNull(PointsReward::getStartTime).or().le(PointsReward::getStartTime, now));
        queryWrapper.and(wrapper -> wrapper.isNull(PointsReward::getEndTime).or().ge(PointsReward::getEndTime, now));

        // 按排序字段降序，创建时间降序排序
        queryWrapper.orderByAsc(PointsReward::getSort)
                .orderByDesc(PointsReward::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public PointsReward getPointsRewardDetail(Long id) {
        if (id == null) {
            return null;
        }

        return getById(id);
    }

    @Override
    public List<PointsReward> getRecommendRewards(int limit) {
        // 查询可见的、库存充足的奖励
        LambdaQueryWrapper<PointsReward> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsReward::getVisible, 1)
                .gt(PointsReward::getStock, 0);

        // 只查询有效期内的奖励
        LocalDateTime now = LocalDateTime.now();
        queryWrapper
                .and(wrapper -> wrapper.isNull(PointsReward::getStartTime).or().le(PointsReward::getStartTime, now));
        queryWrapper.and(wrapper -> wrapper.isNull(PointsReward::getEndTime).or().ge(PointsReward::getEndTime, now));

        // 按排序字段、创建时间排序
        queryWrapper.orderByAsc(PointsReward::getSort)
                .orderByDesc(PointsReward::getCreateTime);

        queryWrapper.last("LIMIT " + limit);

        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangeReward(Integer userId, Long rewardId) {
        if (userId == null || rewardId == null) {
            throw new BusinessException("参数不完整");
        }

        // 查询奖励
        PointsReward reward = getById(rewardId);
        if (reward == null) {
            throw new BusinessException("奖励不存在");
        }

        // 检查奖励状态
        if (reward.getVisible() != 1) {
            throw new BusinessException("奖励已下线");
        }

        // 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (reward.getStartTime() != null && reward.getStartTime().isAfter(now)) {
            throw new BusinessException("奖励活动未开始");
        }
        if (reward.getEndTime() != null && reward.getEndTime().isBefore(now)) {
            throw new BusinessException("奖励活动已结束");
        }

        // 检查库存
        if (reward.getStock() <= 0) {
            throw new BusinessException("奖励库存不足");
        }

        // 获取用户积分
        Integer userPoints = pointsService.getUserPoints(userId);
        if (userPoints < reward.getPoints()) {
            throw new BusinessException("积分不足");
        }

        // 扣减积分
        boolean pointsDeducted = pointsService.deductPoints(
                userId,
                reward.getPoints(),
                "reward_exchange",
                rewardId.toString(),
                "兑换奖励：" + reward.getName());

        if (!pointsDeducted) {
            throw new BusinessException("积分扣减失败");
        }

        // 处理不同类型的奖励
        switch (reward.getType()) {
            case "coupon":
                // 优惠券类型，发放优惠券
                if (reward.getCouponId() == null) {
                    throw new BusinessException("未配置优惠券信息");
                }

                boolean couponReceived = couponService.receiveCoupon(userId, reward.getCouponId());
                if (!couponReceived) {
                    throw new BusinessException("优惠券发放失败");
                }
                break;

            case "product":
                // 实物商品类型，通常需要创建一个发货记录
                // 这里简化处理，仅减少库存
                break;

            case "service":
                // 服务类型，可能需要发送通知等
                // 这里简化处理
                break;

            default:
                throw new BusinessException("不支持的奖励类型");
        }

        // 更新奖励库存和兑换数量
        boolean updated = update(
                new LambdaUpdateWrapper<PointsReward>()
                        .eq(PointsReward::getId, rewardId)
                        .gt(PointsReward::getStock, 0) // 防止超兑换
                        .setSql("stock = stock - 1")
                        .setSql("exchanged_count = exchanged_count + 1"));

        if (!updated) {
            throw new BusinessException("更新奖励信息失败");
        }

        return true;
    }

    @Override
    public boolean updateRewardStatus(Long id, boolean visible) {
        if (id == null) {
            throw new BusinessException("奖励ID不能为空");
        }

        PointsReward reward = getById(id);
        if (reward == null) {
            throw new BusinessException("奖励不存在");
        }

        // 如果状态相同，则不需要更新
        if ((visible && reward.getVisible() == 1) || (!visible && reward.getVisible() == 0)) {
            return true;
        }

        LambdaUpdateWrapper<PointsReward> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PointsReward::getId, id)
                .set(PointsReward::getVisible, visible ? 1 : 0);

        return update(updateWrapper);
    }
}