package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentRewardConfig;
import com.muyingmall.mapper.CommentRewardConfigMapper;
import com.muyingmall.service.CommentRewardConfigService;
import com.muyingmall.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价奖励配置服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentRewardConfigServiceImpl extends ServiceImpl<CommentRewardConfigMapper, CommentRewardConfig>
        implements CommentRewardConfigService {

    private final PointsService pointsService;

    @Override
    public CommentRewardConfig getActiveRewardConfig() {
        LambdaQueryWrapper<CommentRewardConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentRewardConfig::getStatus, 1)
                .orderByDesc(CommentRewardConfig::getRewardValue);

        List<CommentRewardConfig> configs = this.list(queryWrapper);
        return configs.isEmpty() ? null : configs.get(0);
    }

    @Override
    public Map<String, Object> calculateReward(Comment comment) {
        Map<String, Object> result = new HashMap<>();
        result.put("rewardType", "none");
        result.put("rewardValue", 0);

        if (comment == null) {
            return result;
        }

        // 获取所有有效的奖励配置
        LambdaQueryWrapper<CommentRewardConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentRewardConfig::getStatus, 1)
                .orderByDesc(CommentRewardConfig::getRewardValue);

        List<CommentRewardConfig> configs = this.list(queryWrapper);
        if (configs.isEmpty()) {
            return result;
        }

        // 检查评价是否满足奖励条件
        for (CommentRewardConfig config : configs) {
            boolean contentLengthMet = comment.getContent() != null &&
                    comment.getContent().length() >= config.getMinContentLength();

            boolean imageRequirementMet = config.getRequireImage() == 0 ||
                    (comment.getImages() != null && !comment.getImages().isEmpty());

            if (contentLengthMet && imageRequirementMet) {
                result.put("rewardType", config.getRewardType());
                result.put("rewardValue", config.getRewardValue());
                return result;
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantReward(Comment comment) {
        Map<String, Object> rewardInfo = calculateReward(comment);
        Map<String, Object> result = new HashMap<>(rewardInfo);
        result.put("success", false);

        String rewardType = (String) rewardInfo.get("rewardType");
        Integer rewardValue = (Integer) rewardInfo.get("rewardValue");

        if ("none".equals(rewardType) || rewardValue <= 0) {
            return result;
        }

        try {
            // 根据奖励类型发放奖励
            if ("points".equals(rewardType)) {
                // 发放积分奖励
                boolean success = pointsService.addPoints(
                        comment.getUserId(),
                        rewardValue,
                        "comment_reward",
                        comment.getCommentId().toString(),
                        "评价奖励");

                result.put("success", success);
                log.info("用户 {} 因评价 {} 获得 {} 积分奖励",
                        comment.getUserId(), comment.getCommentId(), rewardValue);
            }
        } catch (Exception e) {
            log.error("发放评价奖励失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}