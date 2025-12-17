package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.entity.Comment;
import com.muyingmall.entity.CommentRewardConfig;
import com.muyingmall.entity.Product;
import com.muyingmall.enums.MessageType;
import com.muyingmall.mapper.CommentMapper;
import com.muyingmall.mapper.CommentRewardConfigMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.service.CommentRewardConfigService;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价奖励配置服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentRewardConfigServiceImpl extends ServiceImpl<CommentRewardConfigMapper, CommentRewardConfig>
        implements CommentRewardConfigService {

    private final PointsService pointsService;
    private final CommentMapper commentMapper;
    private final UserMessageService userMessageService;
    private final ProductMapper productMapper;

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
        result.put("totalReward", 0);
        result.put("rewards", new ArrayList<Map<String, Object>>());

        if (comment == null) {
            return result;
        }

        // 获取所有有效的奖励配置
        LambdaQueryWrapper<CommentRewardConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentRewardConfig::getStatus, 1)
                .orderByAsc(CommentRewardConfig::getRewardLevel);

        List<CommentRewardConfig> configs = this.list(queryWrapper);
        if (configs.isEmpty()) {
            return result;
        }

        // 计算图片数量
        int imageCount = 0;
        if (comment.getImages() != null && !comment.getImages().isEmpty()) {
            imageCount = comment.getImages().split(",").length;
        }

        // 检查是否是首次评价
        boolean isFirstComment = isFirstComment(comment.getUserId(), comment.getProductId());

        // 累计奖励总值
        int totalReward = 0;
        List<Map<String, Object>> rewardDetails = new ArrayList<>();

        // 检查评价是否满足各项奖励条件
        for (CommentRewardConfig config : configs) {
            // 检查内容长度要求
            boolean contentLengthMet = comment.getContent() != null &&
                    comment.getContent().length() >= config.getMinContentLength();

            // 检查图片要求
            boolean imageRequirementMet = !config.getRequireImage().equals(1) || imageCount >= config.getMinImages();

            // 检查评分要求
            boolean ratingRequirementMet = config.getMinRating() == null ||
                    (comment.getRating() != null && comment.getRating() >= config.getMinRating());

            // 检查首评要求
            boolean firstCommentRequirementMet = !config.getIsFirstComment().equals(1) || isFirstComment;

            // 如果满足所有要求，添加到奖励列表
            if (contentLengthMet && imageRequirementMet && ratingRequirementMet && firstCommentRequirementMet) {
                Map<String, Object> rewardDetail = new HashMap<>();
                rewardDetail.put("rewardName", config.getRewardName());
                rewardDetail.put("rewardDescription", config.getRewardDescription());
                rewardDetail.put("rewardType", config.getRewardType());
                rewardDetail.put("rewardValue", config.getRewardValue());

                rewardDetails.add(rewardDetail);
                totalReward += config.getRewardValue();
            }
        }

        result.put("totalReward", totalReward);
        result.put("rewards", rewardDetails);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantReward(Comment comment) {
        log.debug("开始计算评价奖励, commentId={}, userId={}", comment.getCommentId(), comment.getUserId());
        Map<String, Object> rewardInfo = calculateReward(comment);
        Map<String, Object> result = new HashMap<>(rewardInfo);
        result.put("success", false);

        Integer totalReward = (Integer) rewardInfo.get("totalReward");
        List<Map<String, Object>> rewards = (List<Map<String, Object>>) rewardInfo.get("rewards");

        log.debug("评价奖励计算结果: totalReward={}, rewards={}", totalReward, rewards);

        if (totalReward <= 0 || rewards.isEmpty()) {
            log.debug("无可用奖励，commentId={}, userId={}", comment.getCommentId(), comment.getUserId());
            return result;
        }

        try {
            // 获取商品名称用于消息提示
            String productName = "商品";
            Product product = productMapper.selectById(comment.getProductId());
            if (product != null) {
                productName = product.getProductName();
                if (productName.length() > 15) {
                    productName = productName.substring(0, 12) + "...";
                }
            }
            log.debug("获取商品信息成功: productId={}, productName={}", comment.getProductId(), productName);

            // 发放积分奖励
            log.debug("准备发放积分奖励: userId={}, points={}, source={}, referenceId={}",
                    comment.getUserId(), totalReward, "comment_reward", comment.getCommentId().toString());
            boolean success = pointsService.addPoints(
                    comment.getUserId(),
                    totalReward,
                    "comment_reward",
                    comment.getCommentId().toString(),
                    "评价奖励");

            log.debug("积分发放结果: success={}, userId={}, points={}",
                    success, comment.getUserId(), totalReward);

            result.put("success", success);

            // 记录奖励详情
            StringBuilder rewardDesc = new StringBuilder("评价奖励: ");
            for (Map<String, Object> reward : rewards) {
                rewardDesc.append(reward.get("rewardName"))
                        .append("(")
                        .append(reward.get("rewardValue"))
                        .append("积分), ");
            }

            log.debug("用户 {} 因评价 {} 获得奖励: {}",
                    comment.getUserId(), comment.getCommentId(), rewardDesc.toString());

            // 发送消息到消息中心
            if (success) {
                String messageTitle = "评价奖励 +" + totalReward + "积分";
                StringBuilder messageContent = new StringBuilder();
                messageContent.append("感谢您对\"").append(productName).append("\"的评价！\n\n");
                messageContent.append("系统已为您发放以下奖励：\n\n");

                for (Map<String, Object> reward : rewards) {
                    messageContent.append("· ").append(reward.get("rewardName"))
                            .append(": +").append(reward.get("rewardValue"))
                            .append("积分\n");
                }

                messageContent.append("\n总计: +").append(totalReward).append("积分");

                // 构建额外信息JSON，用于消息展示和后续处理
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("commentId", comment.getCommentId());
                extraInfo.put("productId", comment.getProductId());
                extraInfo.put("totalReward", totalReward);
                extraInfo.put("rewards", rewards);

                String extra;
                try {
                    extra = new ObjectMapper().writeValueAsString(extraInfo);
                } catch (Exception e) {
                    log.error("序列化额外信息失败", e);
                    extra = "{\"commentId\":" + comment.getCommentId() + "}";
                }

                log.debug("准备发送消息通知: userId={}, type={}, title={}",
                        comment.getUserId(), MessageType.COMMENT_REWARD.getCode(), messageTitle);
                // 发送积分消息通知
                userMessageService.createMessage(
                        comment.getUserId(),
                        MessageType.COMMENT_REWARD.getCode(),
                        messageTitle,
                        messageContent.toString(),
                        extra);

                log.debug("已发送评价奖励消息通知给用户 {}: 奖励 {} 积分", comment.getUserId(), totalReward);
            }
        } catch (Exception e) {
            log.error("发放评价奖励失败, commentId={}, userId={}, error={}",
                    comment.getCommentId(), comment.getUserId(), e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 检查是否是用户对该商品的首次评价
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否是首次评价
     */
    private boolean isFirstComment(Integer userId, Integer productId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .eq(Comment::getProductId, productId);

        return commentMapper.selectCount(queryWrapper) == 0;
    }
}