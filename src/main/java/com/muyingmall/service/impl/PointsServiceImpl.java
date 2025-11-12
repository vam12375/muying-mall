package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.common.CacheConstants;
import com.muyingmall.entity.MemberLevel;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.PointsExchange;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.entity.PointsRule;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserPoints;
import com.muyingmall.enums.PointsOperationType;
import com.muyingmall.event.CheckinEvent;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.PointsExchangeMapper;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.mapper.PointsRuleMapper;
import com.muyingmall.mapper.UserPointsMapper;
import com.muyingmall.service.MemberLevelService;
import com.muyingmall.service.PointsExchangeService;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.service.PointsProductService;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.UserService;
import com.muyingmall.util.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 积分管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements PointsService {

    private final PointsHistoryMapper pointsHistoryMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final PointsProductService pointsProductService;
    private final PointsOperationService pointsOperationService;
    private final MemberLevelService memberLevelService;
    private final UserPointsMapper userPointsMapper;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisUtil redisUtil;
    private final PointsExchangeMapper pointsExchangeMapper;

    @Override
    public Integer getUserPoints(Integer userId) {
        return pointsOperationService.getUserPoints(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        return pointsOperationService.addPoints(userId, points, source, referenceId, description);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Integer userId, Integer points, String source, String referenceId, String description) {
        return pointsOperationService.deductPoints(userId, points, source, referenceId, description);
    }

    @Override
    public Page<PointsHistory> getUserPointsHistory(Integer userId, int page, int size) {
        Page<PointsHistory> pageParam = new Page<>(page, size);

        // 构建缓存键
        String cacheKey = CacheConstants.USER_POINTS_HISTORY_KEY + userId + ":page_" + page + ":size_" + size;

        // 查询缓存
        Object cacheResult = redisUtil.get(cacheKey);
        if (cacheResult != null) {
            log.debug("从缓存中获取用户积分历史: userId={}, page={}, size={}", userId, page, size);
            return (Page<PointsHistory>) cacheResult;
        }

        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户积分历史: userId={}, page={}, size={}", userId, page, size);

        LambdaQueryWrapper<PointsHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsHistory::getUserId, userId)
                .orderByDesc(PointsHistory::getCreateTime);

        Page<PointsHistory> result = pointsHistoryMapper.selectPage(pageParam, queryWrapper);

        // 缓存结果
        if (result != null && !result.getRecords().isEmpty()) {
            redisUtil.set(cacheKey, result, CacheConstants.POINTS_HISTORY_EXPIRE_TIME);
            log.debug("将用户积分历史缓存到Redis: userId={}, page={}, size={}, 过期时间={}秒",
                    userId, page, size, CacheConstants.POINTS_HISTORY_EXPIRE_TIME);
        }

        return result;
    }

    @Override
    public List<PointsRule> getPointsRules() {
        LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsRule::getEnabled, 1)
                .orderByAsc(PointsRule::getSort);

        return pointsRuleMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer signIn(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        try {
            Map<String, Object> signInStatus = getSignInStatus(userId);
            boolean alreadySignedToday = (boolean) signInStatus.get("todaySigned");

            if (alreadySignedToday) {
                throw new BusinessException("您今日已签到，请明天再来");
            }

            // 获取签到规则 - 避免使用sort列排序
            LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PointsRule::getType, "signin")
                    .eq(PointsRule::getEnabled, 1);
            // 不使用orderByAsc(PointsRule::getSort)排序，避免列不存在的问题

            PointsRule signInRule = null;
            try {
                signInRule = pointsRuleMapper.selectOne(queryWrapper);
            } catch (Exception e) {
                log.error("查询签到规则失败: {}", e.getMessage());
                // 继续执行，使用默认值
            }

            // 基础积分 - 使用默认值处理空值情况
            int signInPoints = 20; // 默认值为20积分
            if (signInRule != null && signInRule.getValue() != null) {
                signInPoints = signInRule.getValue();
            } else {
                log.warn("签到规则不存在或值为空，使用默认值 20 积分");
            }

            // 连续签到天数
            int continuousDays = (int) signInStatus.get("continuousDays");

            // 计算今天签到后的连续天数
            int newContinuousDays = continuousDays + 1;

            // 基础额外积分（小额奖励）
            int extraPoints = 0;
            if (continuousDays >= 3) {
                // 修改连续签到额外积分计算逻辑
                // 连续签到3天及以上，第3天后每天额外获得5积分
                // 对于已经连续签到超过3天的用户，额外积分为(天数-2)*5
                extraPoints = (continuousDays - 2) * 5;

                // 如果连续签到天数超过7天，每天获得10积分而不是5积分
                if (continuousDays >= 7) {
                    extraPoints = (continuousDays - 6) * 10 + 20; // 3-6天获得5积分/天，共20积分
                }
            }

            int totalPoints = signInPoints + extraPoints;
            String description = "每日签到";

            // 判断是否达到特殊连续签到奖励条件（第3天或第7天）
            if (newContinuousDays == 3 || newContinuousDays == 7) {
                // 获取对应的连续签到规则
                String continuousType = "signin_continuous_" + newContinuousDays;
                LambdaQueryWrapper<PointsRule> continuousRuleQuery = new LambdaQueryWrapper<>();
                continuousRuleQuery.eq(PointsRule::getType, continuousType)
                        .eq(PointsRule::getEnabled, 1);

                try {
                    PointsRule continuousRule = pointsRuleMapper.selectOne(continuousRuleQuery);
                    if (continuousRule != null && continuousRule.getValue() != null) {
                        int continuousPoints = continuousRule.getValue();
                        // 记录连续签到奖励
                        String specialDescription = "连续签到" + newContinuousDays + "天额外奖励";
                        boolean continuousSuccess = this.addPoints(userId, continuousPoints,
                                "signin_continuous",
                                LocalDate.now().toString(),
                                specialDescription);

                        if (!continuousSuccess) {
                            log.warn("用户 {} 连续签到 {} 天奖励记录失败", userId, newContinuousDays);
                        } else {
                            log.info("用户 {} 获得连续签到 {} 天额外奖励 {} 积分", userId, newContinuousDays, continuousPoints);
                        }
                    }
                } catch (Exception e) {
                    log.error("查询连续签到规则失败: {}", e.getMessage());
                    // 继续执行，不影响基础签到
                }
            }

            if (extraPoints > 0) {
                description += "，连续签到" + newContinuousDays + "天";
            }

            // 记录基础签到积分
            boolean success = this.addPoints(userId, totalPoints, "signin", LocalDate.now().toString(), description);

            if (!success) {
                throw new BusinessException("签到积分记录失败");
            }

            log.info("用户 {} 签到成功，获得 {} 积分", userId, totalPoints);
            return totalPoints;
        } catch (BusinessException be) {
            // 业务异常直接抛出
            throw be;
        } catch (Exception e) {
            log.error("用户 {} 签到异常: {}", userId, e.getMessage(), e);
            throw new BusinessException("签到失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getSignInStatus(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        Map<String, Object> result = new HashMap<>();

        try {
            // 构建缓存键
            String cacheKey = CacheConstants.USER_SIGNIN_STATUS_KEY + userId;

            // 查询缓存
            Object cacheResult = redisUtil.get(cacheKey);
            if (cacheResult != null) {
                log.debug("从缓存中获取用户签到状态: userId={}", userId);

                // 直接返回缓存结果前，确保积分总数是最新的
                Map<String, Object> cachedStatus = (Map<String, Object>) cacheResult;

                // 从数据库获取最新的积分总数
                Integer latestTotalPoints = getUserPoints(userId);
                cachedStatus.put("totalPoints", latestTotalPoints);
                cachedStatus.put("points", latestTotalPoints);

                return cachedStatus;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户签到状态: userId={}", userId);

            // 当前日期
            LocalDate today = LocalDate.now();
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime endOfToday = today.plusDays(1).atStartOfDay().minusNanos(1);

            // 检查今日是否已签到
            boolean todaySigned = false;
            try {
                LambdaQueryWrapper<PointsHistory> todaySignInQuery = new LambdaQueryWrapper<>();
                todaySignInQuery.eq(PointsHistory::getUserId, userId)
                        .eq(PointsHistory::getSource, "signin")
                        .between(PointsHistory::getCreateTime, startOfToday, endOfToday);

                long todaySignInCount = pointsHistoryMapper.selectCount(todaySignInQuery);
                todaySigned = todaySignInCount > 0;
            } catch (Exception e) {
                log.error("检查今日签到状态失败: {}", e.getMessage());
                // 继续执行，假设未签到
            }

            // 计算连续签到天数
            int continuousDays = 0;
            try {
                LocalDate checkDate = today.minusDays(1); // 从昨天开始检查

                while (true) {
                    LocalDateTime startOfDay = checkDate.atStartOfDay();
                    LocalDateTime endOfDay = checkDate.plusDays(1).atStartOfDay().minusNanos(1);

                    LambdaQueryWrapper<PointsHistory> dayQuery = new LambdaQueryWrapper<>();
                    dayQuery.eq(PointsHistory::getUserId, userId)
                            .eq(PointsHistory::getSource, "signin")
                            .between(PointsHistory::getCreateTime, startOfDay, endOfDay);

                    long count = pointsHistoryMapper.selectCount(dayQuery);

                    if (count > 0) {
                        continuousDays++;
                        checkDate = checkDate.minusDays(1);
                    } else {
                        break;
                    }

                    // 为了防止无限循环，限制最大检查天数
                    if (continuousDays >= 1000) {
                        log.warn("连续签到天数超过1000天，可能存在数据问题，用户ID: {}", userId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("计算连续签到天数失败: {}", e.getMessage());
                // 继续执行，使用已计算的天数
            }

            // 获取历史最长连续签到天数
            int historyMaxContinuousDays = getHistoryMaxContinuousDays(userId, continuousDays);

            // 获取用户总积分和会员等级（直接从user_points表读取）
            Integer totalPoints = 0;
            String userLevel = "普通会员";
            try {
                UserPoints userPoints = userPointsMapper.selectOne(
                    new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId.longValue())
                );
                if (userPoints != null) {
                    totalPoints = userPoints.getPoints() != null ? userPoints.getPoints() : 0;
                    userLevel = userPoints.getLevel() != null ? userPoints.getLevel() : "普通会员";
                } else {
                    log.warn("用户ID: {} 的积分记录不存在", userId);
                }
            } catch (Exception e) {
                log.error("获取用户积分和等级失败: {}", e.getMessage());
                // 继续执行，使用默认值
            }

            // 获取累计获得的积分和已使用的积分
            Integer totalEarned = 0;
            Integer totalUsed = 0;
            try {
                // 查询累计获得的积分 - 确保正确使用type字段
                LambdaQueryWrapper<PointsHistory> earnQuery = new LambdaQueryWrapper<>();
                earnQuery.eq(PointsHistory::getUserId, userId)
                        .eq(PointsHistory::getType, "earn"); // 类型为"earn"的记录

                List<PointsHistory> earnRecords = pointsHistoryMapper.selectList(earnQuery);
                totalEarned = earnRecords.stream()
                        .mapToInt(PointsHistory::getPoints)
                        .sum();

                // 查询已使用的积分 - 确保正确使用type字段
                LambdaQueryWrapper<PointsHistory> usedQuery = new LambdaQueryWrapper<>();
                usedQuery.eq(PointsHistory::getUserId, userId)
                        .eq(PointsHistory::getType, "spend"); // 类型为"spend"的记录

                List<PointsHistory> usedRecords = pointsHistoryMapper.selectList(usedQuery);
                totalUsed = usedRecords.stream()
                        .mapToInt(PointsHistory::getPoints)
                        .map(Math::abs) // 确保用正数来计算总使用量
                        .sum();

                log.debug("用户 {} 的积分统计: 累计获得={}, 已使用={}", userId, totalEarned, totalUsed);
            } catch (Exception e) {
                log.error("计算用户积分统计信息失败: {}", e.getMessage());
                // 继续执行，使用默认值0
            }

            // 构建结果
            result.put("todaySigned", todaySigned);
            result.put("isSignedIn", todaySigned); // 添加isSignedIn字段，与todaySigned保持一致
            result.put("continuousDays", continuousDays);
            result.put("historyMaxContinuousDays", historyMaxContinuousDays);
            result.put("totalPoints", totalPoints);
            result.put("points", totalPoints); // 为前端提供一个额外的积分字段
            result.put("userLevel", userLevel); // 添加会员等级
            result.put("totalEarned", totalEarned); // 添加累计获得积分
            result.put("totalUsed", totalUsed); // 添加已使用积分

            // 添加本月获得的积分（可选）
            LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
            try {
                LambdaQueryWrapper<PointsHistory> monthQuery = new LambdaQueryWrapper<>();
                monthQuery.eq(PointsHistory::getUserId, userId)
                        .eq(PointsHistory::getType, "earn")
                        .ge(PointsHistory::getCreateTime, startOfMonth);

                List<PointsHistory> monthRecords = pointsHistoryMapper.selectList(monthQuery);
                int pointsThisMonth = monthRecords.stream()
                        .mapToInt(PointsHistory::getPoints)
                        .sum();

                result.put("pointsThisMonth", pointsThisMonth);
            } catch (Exception e) {
                log.error("计算本月积分失败: {}", e.getMessage());
                result.put("pointsThisMonth", 0);
            }

            // 添加可能的下一次签到可获得的积分数
            int nextSignInPoints = calculateNextSignInPoints(continuousDays);
            result.put("nextSignInPoints", nextSignInPoints);

            log.debug("用户 {} 的签到状态: 今日已签到={}, 连续签到天数={}, 历史最长连续签到={}, 总积分={}, 累计获得={}, 已使用={}, 会员等级={}, 下次签到积分={}",
                    userId, todaySigned, continuousDays, historyMaxContinuousDays, totalPoints, totalEarned, totalUsed,
                    userLevel,
                    nextSignInPoints);

            // 缓存结果，由于签到状态会随着时间变化，设置较短的过期时间
            redisUtil.set(cacheKey, result, CacheConstants.POINTS_STATUS_EXPIRE_TIME);
            log.debug("将用户签到状态缓存到Redis: userId={}, 过期时间={}秒", userId, CacheConstants.POINTS_STATUS_EXPIRE_TIME);

            return result;
        } catch (Exception e) {
            log.error("获取用户 {} 的签到状态失败: {}", userId, e.getMessage(), e);
            // 返回基本信息，避免前端出错
            result.put("todaySigned", false);
            result.put("isSignedIn", false); // 添加isSignedIn字段，与todaySigned保持一致
            result.put("continuousDays", 0);
            result.put("historyMaxContinuousDays", 0);
            result.put("totalPoints", 0);
            result.put("points", 0);
            result.put("userLevel", "普通会员");
            result.put("nextSignInPoints", 20); // 默认为20
            result.put("totalEarned", 0);
            result.put("totalUsed", 0);
            result.put("pointsThisMonth", 0);
            return result;
        }
    }

    /**
     * 获取历史最长连续签到天数
     */
    private int getHistoryMaxContinuousDays(Integer userId, int currentContinuousDays) {
        // 这里简化实现，实际可以从数据库中查询历史记录
        // 或者在用户表中添加一个字段记录历史最大连续签到天数

        // 此处暂时使用当前连续签到天数作为历史最大值
        return currentContinuousDays;
    }

    /**
     * 计算下一次签到可获得的积分
     */
    private int calculateNextSignInPoints(int continuousDays) {
        int nextDay = continuousDays + 1;

        // 基础积分，默认20
        int basePoints = 20;

        try {
            // 尝试获取签到规则中的基础积分
            LambdaQueryWrapper<PointsRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PointsRule::getType, "signin")
                    .eq(PointsRule::getEnabled, 1);

            PointsRule signInRule = pointsRuleMapper.selectOne(queryWrapper);
            if (signInRule != null && signInRule.getValue() != null) {
                basePoints = signInRule.getValue();
            }
        } catch (Exception e) {
            log.error("获取签到规则失败，使用默认值: {}", e.getMessage());
            // 使用默认值
        }

        // 计算常规额外积分
        int extraPoints = 0;
        if (nextDay >= 3) {
            // 修改连续签到额外积分计算逻辑
            // 连续签到3天及以上，第3天后每天额外获得5积分
            // 对于已经连续签到超过3天的用户，额外积分为(天数-2)*5
            extraPoints = (nextDay - 2) * 5;

            // 如果连续签到天数超过7天，每天获得10积分而不是5积分
            if (nextDay >= 7) {
                extraPoints = (nextDay - 6) * 10 + 20; // 3-6天获得5积分/天，共20积分
            }
        }

        int totalPoints = basePoints + extraPoints;

        // 计算特定里程碑的额外奖励（第3天或第7天）
        if (nextDay == 3 || nextDay == 7) {
            try {
                // 获取对应的连续签到规则
                String continuousType = "signin_continuous_" + nextDay;
                LambdaQueryWrapper<PointsRule> continuousRuleQuery = new LambdaQueryWrapper<>();
                continuousRuleQuery.eq(PointsRule::getType, continuousType)
                        .eq(PointsRule::getEnabled, 1);

                PointsRule continuousRule = pointsRuleMapper.selectOne(continuousRuleQuery);
                if (continuousRule != null && continuousRule.getValue() != null) {
                    // 将额外奖励加入到提示中
                    totalPoints += continuousRule.getValue();
                }
            } catch (Exception e) {
                log.error("获取连续签到奖励规则失败，无法计算额外奖励: {}", e.getMessage());
                // 继续使用基础积分
                if (nextDay == 3) {
                    // 使用默认值200
                    totalPoints += 200;
                } else if (nextDay == 7) {
                    // 使用默认值500
                    totalPoints += 500;
                }
            }
        }

        return totalPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangeProduct(Integer userId, Long productId, Integer addressId, String phone) {
        // 查询商品
        PointsProduct product = pointsProductService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        // 检查必要信息
        if (product.getNeedAddress() == 1 && addressId == null) {
            throw new BusinessException("请选择收货地址");
        }

        if (product.getNeedPhone() == 1 && phone == null) {
            throw new BusinessException("请输入手机号码");
        }

        // 计算所需积分
        int totalPoints = product.getPoints();

        // 获取用户积分
        Integer userPoints = pointsOperationService.getUserPoints(userId);
        if (userPoints < totalPoints) {
            throw new BusinessException("积分不足");
        }

        // 创建兑换记录
        PointsExchange exchange = new PointsExchange();
        exchange.setUserId(userId);
        exchange.setProductId(productId.intValue());
        exchange.setQuantity(1);
        exchange.setAddressId(addressId);
        exchange.setPhone(phone);
        exchange.setOrderNo(generateExchangeOrderNo());
        exchange.setPoints(totalPoints);
        exchange.setStatus(String.valueOf(0)); // 待发货
        exchange.setCreateTime(LocalDateTime.now());
        exchange.setUpdateTime(LocalDateTime.now());

        // 扣减积分
        boolean success = pointsOperationService.deductPoints(userId, totalPoints, "exchange", exchange.getOrderNo(),
                "积分兑换商品");

        if (success) {
            // 减少库存
            pointsProductService.update(
                    new LambdaUpdateWrapper<PointsProduct>()
                            .eq(PointsProduct::getId, productId)
                            .setSql("stock = stock - 1"));
        }

        return success;
    }

    /**
     * 生成兑换订单号
     */
    private String generateExchangeOrderNo() {
        return "PE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * 以下是实现接口中的其余方法
     */

    @Override
    public Page<PointsProduct> getPointsProducts(int page, int size, String category) {
        // 委托给PointsProductService处理
        return pointsProductService.getPointsProductPage(page, size, category);
    }

    @Override
    public PointsProduct getPointsProductDetail(Long productId) {
        // 委托给PointsProductService处理
        return pointsProductService.getPointsProductDetail(productId);
    }

    @Override
    public Map<String, Object> userSignin(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        log.info("用户 {} 开始签到", userId);
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取签到前的状态
            Map<String, Object> beforeStatus = getSignInStatus(userId);
            int continuousDays = (int) beforeStatus.get("continuousDays");

            // 计算签到后的连续天数
            int newContinuousDays = continuousDays + 1;

            // 签到获取积分
            Integer earnedPoints = signIn(userId);
            int totalEarnedPoints = earnedPoints;

            // 如果是第3天或第7天，需要加上额外奖励
            if (newContinuousDays == 3 || newContinuousDays == 7) {
                try {
                    String continuousType = "signin_continuous_" + newContinuousDays;
                    LambdaQueryWrapper<PointsRule> continuousRuleQuery = new LambdaQueryWrapper<>();
                    continuousRuleQuery.eq(PointsRule::getType, continuousType)
                            .eq(PointsRule::getEnabled, 1);

                    PointsRule continuousRule = pointsRuleMapper.selectOne(continuousRuleQuery);
                    if (continuousRule != null && continuousRule.getValue() != null) {
                        totalEarnedPoints += continuousRule.getValue();
                    }
                } catch (Exception e) {
                    log.error("获取连续签到规则失败，无法计算总奖励: {}", e.getMessage());
                    // 继续使用基础积分
                    if (newContinuousDays == 3) {
                        totalEarnedPoints += 200; // 默认值
                    } else if (newContinuousDays == 7) {
                        totalEarnedPoints += 500; // 默认值
                    }
                }
            }

            // 清除用户积分状态缓存，确保下次获取的是最新数据
            String cacheKey = CacheConstants.USER_SIGNIN_STATUS_KEY + userId;
            redisUtil.del(cacheKey);
            log.debug("已清除用户积分状态缓存: cacheKey={}", cacheKey);

            // 获取最新的签到状态（重新从数据库获取，避免使用缓存）
            Map<String, Object> status = getSignInStatus(userId);

            // 手动更新累计获得的积分值
            Integer oldTotalEarned = (Integer) status.getOrDefault("totalEarned", 0);
            status.put("totalEarned", oldTotalEarned + totalEarnedPoints);

            // 合并结果
            result.putAll(status);
            result.put("earnedPoints", earnedPoints);
            result.put("totalEarnedPoints", totalEarnedPoints); // 添加包含额外奖励的总积分
            // 确保积分值也放在根级别，方便前端直接获取
            result.put("points", totalEarnedPoints); // 前端会使用这个字段，修改为显示包含奖励的总积分
            result.put("todaySigned", true); // 确保todaySigned为true
            result.put("isSignedIn", true); // 添加isSignedIn字段，确保和todaySigned一致
            result.put("success", true);

            // 如果有连续签到奖励，更新消息
            if (newContinuousDays == 3 || newContinuousDays == 7) {
                int extraReward = totalEarnedPoints - earnedPoints;
                result.put("message", String.format("签到成功！连续签到%d天，额外奖励%d积分", newContinuousDays, extraReward));
            } else {
                result.put("message", "签到成功");
            }

            // 发布签到事件，用于在消息中心显示签到消息
            try {
                // 创建签到事件的额外参数 JSON
                String extraJson = String.format("{\"earnedPoints\":%d,\"continuousDays\":%d}",
                        earnedPoints, newContinuousDays);

                // 创建并发布签到事件
                CheckinEvent checkinEvent = new CheckinEvent(
                        this, // 事件源
                        userId, // 用户ID
                        earnedPoints, // 签到获得的积分
                        newContinuousDays, // 连续签到天数
                        extraJson // 额外信息
                );

                applicationEventPublisher.publishEvent(checkinEvent);
                log.info("用户 {} 签到事件已发布，连续天数: {}, 获得积分: {}",
                        userId, newContinuousDays, earnedPoints);
            } catch (Exception e) {
                // 捕获事件发布异常，但不影响签到的主要功能
                log.error("发布用户 {} 签到事件失败: {}", userId, e.getMessage(), e);
            }

            log.info("用户 {} 签到成功，获得基础积分 {} 点，总计获得 {} 积分", userId, earnedPoints, totalEarnedPoints);
            return result;
        } catch (BusinessException be) {
            // 业务异常，如今日已签到
            log.warn("用户 {} 签到业务异常: {}", userId, be.getMessage());
            result.put("success", false);
            result.put("message", be.getMessage());

            // 尝试获取用户的签到状态
            try {
                Map<String, Object> status = getSignInStatus(userId);
                result.putAll(status);
                result.put("earnedPoints", 0); // 签到失败时设置为0
                result.put("totalEarnedPoints", 0); // 签到失败时总奖励也为0
                result.put("points", status.get("totalPoints")); // 使用当前总积分值
                // 确保todaySigned和isSignedIn字段已经从status中复制过来，无需额外添加
            } catch (Exception ex) {
                log.error("获取用户 {} 签到状态失败: {}", userId, ex.getMessage());
                // 添加默认值，避免前端出错
                result.put("todaySigned", true); // 假设已签到
                result.put("isSignedIn", true); // 添加isSignedIn字段，确保和todaySigned一致
                result.put("continuousDays", 0);
                result.put("historyMaxContinuousDays", 0);
                result.put("totalPoints", 0);
                result.put("nextSignInPoints", 20);
                result.put("earnedPoints", 0);
                result.put("totalEarnedPoints", 0);
                result.put("points", 0);
                result.put("totalEarned", 0);
                result.put("totalUsed", 0);
            }

            return result;
        } catch (Exception e) {
            // 其他异常
            log.error("用户 {} 签到异常: {}", userId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "签到失败: " + e.getMessage());

            // 添加默认值，避免前端出错
            result.put("todaySigned", false);
            result.put("isSignedIn", false); // 添加isSignedIn字段，确保和todaySigned一致
            result.put("continuousDays", 0);
            result.put("historyMaxContinuousDays", 0);
            result.put("totalPoints", 0);
            result.put("earnedPoints", 0);
            result.put("totalEarnedPoints", 0);
            result.put("points", 0);
            result.put("nextSignInPoints", 20);
            result.put("totalEarned", 0);
            result.put("totalUsed", 0);

            return result;
        }
    }

    @Override
    public boolean usePoints(Integer userId, int points, String source, String referenceId, String description) {
        // 这个方法的功能与deductPoints相同，只是参数略有不同
        return deductPoints(userId, points, source, referenceId, description);
    }

    @Override
    public Map<String, Object> getSignInCalendar(Integer userId, String month) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取用户 {} 的 {} 月份签到日历", userId, month);

            // 解析月份参数，如果为空则取当前月
            YearMonth yearMonth;
            if (StringUtils.hasText(month)) {
                try {
                    yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
                } catch (Exception e) {
                    log.warn("月份格式错误 '{}': {}, 使用当前月份", month, e.getMessage());
                    yearMonth = YearMonth.now();
                }
            } else {
                yearMonth = YearMonth.now();
            }

            // 构建缓存键
            String cacheKey = CacheConstants.USER_SIGNIN_CALENDAR_KEY + userId + ":" + yearMonth.toString();

            // 查询缓存
            Object cacheResult = redisUtil.get(cacheKey);
            if (cacheResult != null) {
                log.debug("从缓存中获取用户签到日历: userId={}, month={}", userId, yearMonth);
                return (Map<String, Object>) cacheResult;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户签到日历: userId={}, month={}", userId, yearMonth);

            LocalDate today = LocalDate.now();
            boolean isCurrMonth = (yearMonth.getYear() == today.getYear()
                    && yearMonth.getMonthValue() == today.getMonthValue());

            // 当前月份的第一天和最后一天
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusNanos(1);

            // 查询该月所有签到记录
            List<PointsHistory> monthSignInRecords;
            try {
                LambdaQueryWrapper<PointsHistory> monthSignInQuery = new LambdaQueryWrapper<>();
                monthSignInQuery.eq(PointsHistory::getUserId, userId)
                        .eq(PointsHistory::getSource, "signin")
                        .between(PointsHistory::getCreateTime, firstDayOfMonth, lastDayOfMonth)
                        .orderByAsc(PointsHistory::getCreateTime);

                monthSignInRecords = pointsHistoryMapper.selectList(monthSignInQuery);
                log.debug("用户 {} 在 {}-{} 月有 {} 条签到记录", userId,
                        yearMonth.getYear(), yearMonth.getMonthValue(), monthSignInRecords.size());
            } catch (Exception e) {
                log.error("查询用户 {} 的 {}-{} 月签到记录失败: {}", userId,
                        yearMonth.getYear(), yearMonth.getMonthValue(), e.getMessage());
                monthSignInRecords = new ArrayList<>();
            }

            // 构建签到日历

            // 根据签到记录构建已签到的日期列表和详情
            List<Integer> signedDays = new ArrayList<>();
            Map<String, Object> signRecords = new HashMap<>();

            for (PointsHistory record : monthSignInRecords) {
                LocalDateTime signTime = record.getCreateTime();
                int day = signTime.getDayOfMonth();
                signedDays.add(day);

                // 构建每天的签到详情
                Map<String, Object> dayRecord = new HashMap<>();
                dayRecord.put("day", day);
                dayRecord.put("points", record.getPoints());
                dayRecord.put("time", signTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                dayRecord.put("description", record.getDescription());

                signRecords.put(String.valueOf(day), dayRecord);
            }

            // 获取当前月的总天数
            int daysInMonth = yearMonth.lengthOfMonth();

            // 构建月份信息
            Map<String, Object> monthInfo = new HashMap<>();
            monthInfo.put("year", yearMonth.getYear());
            monthInfo.put("month", yearMonth.getMonthValue());
            monthInfo.put("daysInMonth", daysInMonth);
            monthInfo.put("signedDays", signedDays);
            monthInfo.put("signedRecords", signRecords);
            monthInfo.put("isCurrentMonth", isCurrMonth);

            // 获取今日日期
            if (isCurrMonth) {
                monthInfo.put("today", today.getDayOfMonth());
            }

            // 获取签到状态 - 使用当前状态
            Map<String, Object> signInStatus = getSignInStatus(userId);

            // 确保积分统计值存在
            if (!signInStatus.containsKey("totalEarned")) {
                // 手动获取累计获得的积分
                try {
                    LambdaQueryWrapper<PointsHistory> earnQuery = new LambdaQueryWrapper<>();
                    earnQuery.eq(PointsHistory::getUserId, userId)
                            .eq(PointsHistory::getType, "earn");

                    List<PointsHistory> earnRecords = pointsHistoryMapper.selectList(earnQuery);
                    int totalEarned = earnRecords.stream()
                            .mapToInt(PointsHistory::getPoints)
                            .sum();

                    signInStatus.put("totalEarned", totalEarned);
                } catch (Exception e) {
                    log.error("计算累计获得积分失败: {}", e.getMessage());
                    signInStatus.put("totalEarned", 0);
                }
            }

            if (!signInStatus.containsKey("totalUsed")) {
                // 手动获取已使用的积分
                try {
                    LambdaQueryWrapper<PointsHistory> usedQuery = new LambdaQueryWrapper<>();
                    usedQuery.eq(PointsHistory::getUserId, userId)
                            .eq(PointsHistory::getType, "spend");

                    List<PointsHistory> usedRecords = pointsHistoryMapper.selectList(usedQuery);
                    int totalUsed = usedRecords.stream()
                            .mapToInt(PointsHistory::getPoints)
                            .map(Math::abs)
                            .sum();

                    signInStatus.put("totalUsed", totalUsed);
                } catch (Exception e) {
                    log.error("计算已使用积分失败: {}", e.getMessage());
                    signInStatus.put("totalUsed", 0);
                }
            }

            // 合并结果
            result.put("monthInfo", monthInfo);
            result.put("signInStatus", signInStatus);
            result.put("success", true);

            // 缓存结果
            // 当月的缓存设置较短时间，其他月份可以缓存更长时间
            long expireTime = isCurrMonth ? CacheConstants.POINTS_STATUS_EXPIRE_TIME : // 当前月用较短的过期时间
                    CacheConstants.SIGNIN_CALENDAR_EXPIRE_TIME; // 历史月份用较长的过期时间

            redisUtil.set(cacheKey, result, expireTime);
            log.debug("将用户签到日历缓存到Redis: userId={}, month={}, 过期时间={}秒",
                    userId, yearMonth, expireTime);

            return result;
        } catch (Exception e) {
            log.error("获取用户 {} 的签到日历失败: {}", e.getMessage(), e);

            // 返回基本结构，避免前端出错
            Map<String, Object> basicMonthInfo = new HashMap<>();
            YearMonth currentMonth = YearMonth.now();
            LocalDate today = LocalDate.now();

            basicMonthInfo.put("year", currentMonth.getYear());
            basicMonthInfo.put("month", currentMonth.getMonthValue());
            basicMonthInfo.put("daysInMonth", currentMonth.lengthOfMonth());
            basicMonthInfo.put("signedDays", new ArrayList<Integer>());
            basicMonthInfo.put("signedRecords", new HashMap<String, Object>());
            basicMonthInfo.put("isCurrentMonth", true);
            basicMonthInfo.put("today", today.getDayOfMonth());

            // 添加基本签到状态
            Map<String, Object> basicStatus = new HashMap<>();
            basicStatus.put("todaySigned", false);
            basicStatus.put("continuousDays", 0);
            basicStatus.put("historyMaxContinuousDays", 0);
            basicStatus.put("totalPoints", 0);
            basicStatus.put("totalEarned", 0);
            basicStatus.put("totalUsed", 0);

            result.put("monthInfo", basicMonthInfo);
            result.put("signInStatus", basicStatus);
            result.put("success", false);
            result.put("message", "获取签到日历失败: " + e.getMessage());

            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void awardPointsForOrder(Integer userId, Integer orderId, BigDecimal orderAmount) {
        if (userId == null || orderId == null || orderAmount == null) {
            log.error("奖励积分参数错误: userId={}, orderId={}, orderAmount={}", userId, orderId, orderAmount);
            return;
        }

        // 检查订单是否使用了积分抵扣
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在, orderId={}", orderId);
            return;
        }

        // 如果订单使用了积分抵扣，则不再奖励积分
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            log.info("订单 {} 使用了积分抵扣 {}，不再奖励积分", order.getOrderNo(), order.getPointsUsed());
            return;
        }

        try {
            // 计算应奖励的积分 (订单金额的10%)
            int pointsToAward = orderAmount.multiply(new BigDecimal("0.1")).intValue();

            // 确保至少奖励1积分（如果订单金额大于0）
            if (orderAmount.compareTo(BigDecimal.ZERO) > 0 && pointsToAward < 1) {
                pointsToAward = 1;
            }

            if (pointsToAward <= 0) {
                log.info("订单奖励积分为0，不进行奖励操作");
                return;
            }

            // 添加积分
            String description = "订单完成奖励";
            boolean success = addPoints(userId, pointsToAward, "order_completed", orderId.toString(), description);

            if (success) {
                log.info("用户 {} 订单 {} 完成，奖励 {} 积分", userId, orderId, pointsToAward);
            } else {
                log.error("用户 {} 订单 {} 奖励积分失败", userId, orderId);
            }
        } catch (Exception e) {
            log.error("订单奖励积分异常: userId={}, orderId={}, exception={}", userId, orderId, e.getMessage(), e);
        }
    }

    @Override
    public Page<PointsHistory> adminListPointsHistory(Integer page, Integer size, Integer userId,
            String type, String source, LocalDate startDate, LocalDate endDate) {
        Page<PointsHistory> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PointsHistory> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(PointsHistory::getUserId, userId);
        }

        if (StringUtils.hasText(type)) {
            queryWrapper.eq(PointsHistory::getType, type);
        }

        if (StringUtils.hasText(source)) {
            queryWrapper.eq(PointsHistory::getSource, source);
        }

        if (startDate != null) {
            queryWrapper.ge(PointsHistory::getCreateTime, startDate.atStartOfDay());
        }

        if (endDate != null) {
            queryWrapper.le(PointsHistory::getCreateTime, endDate.plusDays(1).atStartOfDay().minusNanos(1));
        }

        queryWrapper.orderByDesc(PointsHistory::getCreateTime);

        return pointsHistoryMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAdjustPoints(Integer userId, Integer points, String description) {
        if (userId == null || points == null || points == 0) {
            log.warn("管理员调整积分 - 无效参数: userId={}, points={}", userId, points);
            return false;
        }

        // 积分增加
        if (points > 0) {
            return addPoints(userId, points, "admin", null, description);
        }
        // 积分扣减
        else {
            return deductPoints(userId, Math.abs(points), "admin", null, description);
        }
    }

    @Override
    public Page<PointsExchange> adminListPointsExchanges(Integer page, Integer size, Integer userId,
            Long productId, String status, LocalDate startDate, LocalDate endDate) {

        Page<PointsExchange> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<PointsExchange> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID筛选
        if (userId != null) {
            queryWrapper.eq(PointsExchange::getUserId, userId);
        }

        // 商品ID筛选
        if (productId != null) {
            queryWrapper.eq(PointsExchange::getProductId, productId.intValue());
        }

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(PointsExchange::getStatus, status);
        }

        // 时间范围筛选
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            queryWrapper.ge(PointsExchange::getCreateTime, startDateTime);
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            queryWrapper.lt(PointsExchange::getCreateTime, endDateTime);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(PointsExchange::getCreateTime);

        // 查询数据
        Page<PointsExchange> resultPage = pointsExchangeMapper.selectPage(pageParam, queryWrapper);

        // 填充用户和商品信息
        List<PointsExchange> records = resultPage.getRecords();
        if (records != null && !records.isEmpty()) {
            for (PointsExchange exchange : records) {
                // 获取并设置用户信息
                if (exchange.getUserId() != null) {
                    User user = userService.getById(exchange.getUserId().longValue());
                    if (user != null) {
                        exchange.setUser(user);
                        exchange.setUsername(user.getUsername());
                    }
                }

                // 获取并设置商品信息
                if (exchange.getProductId() != null) {
                    PointsProduct product = pointsProductService.getById(exchange.getProductId().longValue());
                    if (product != null) {
                        exchange.setProduct(product);
                        exchange.setProductName(product.getName());
                        exchange.setProductImage(product.getImage());
                    }
                }
            }
        }

        return resultPage;
    }


    @Override
    public Map<String, Object> getPointsStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 累计发放积分
        Integer totalEarned = 0;
        // 累计消费积分
        Integer totalSpent = 0;
        // 今日发放积分
        Integer todayEarned = 0;
        // 今日消费积分
        Integer todaySpent = 0;

        // 使用模拟数据，实际项目中应查询数据库
        stats.put("totalEarned", totalEarned);
        stats.put("totalSpent", totalSpent);
        stats.put("todayEarned", todayEarned);
        stats.put("todaySpent", todaySpent);

        return stats;
    }

    @Override
    public Page<UserPoints> pageWithUser(Page<UserPoints> page, LambdaQueryWrapper<UserPoints> queryWrapper) {
        // 先查询用户积分信息
        this.page(page, queryWrapper);

        List<UserPoints> records = page.getRecords();
        if (records != null && !records.isEmpty()) {
            // 收集所有用户ID
            List<Long> userIds = records.stream()
                    .map(UserPoints::getUserId)
                    .collect(Collectors.toList());

            // 查询用户信息
            List<User> users = userService.listByIds(userIds);

            // 创建用户ID到用户信息的映射
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            // 关联用户信息到积分记录
            for (UserPoints userPoints : records) {
                User user = userMap.get(userPoints.getUserId().intValue());
                userPoints.setUser(user);
            }
        }

        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExchangeStatus(Long id, String status) {
        if (id == null || status == null) {
            log.warn("更新兑换状态 - 无效参数: id={}, status={}", id, status);
            return false;
        }

        PointsExchange exchange = pointsExchangeMapper.selectById(id);
        if (exchange == null) {
            log.warn("更新兑换状态 - 兑换记录不存在: id={}", id);
            return false;
        }

        // 如果是取消状态，需要退还积分
        if ("cancelled".equals(status) && !"cancelled".equals(exchange.getStatus())) {
            Integer userId = exchange.getUserId();
            Integer points = exchange.getPoints() * exchange.getQuantity();
            addPoints(userId, points, "exchange_cancel", id.toString(), "取消兑换退还积分");
        }

        exchange.setStatus(status);
        exchange.setUpdateTime(LocalDateTime.now());
        return pointsExchangeMapper.updateById(exchange) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipExchange(Long id, String logisticsCompany, String trackingNumber, String shipRemark) {
        if (id == null || logisticsCompany == null || trackingNumber == null) {
            log.warn("发货 - 无效参数: id={}, logisticsCompany={}, trackingNumber={}", 
                id, logisticsCompany, trackingNumber);
            return false;
        }

        PointsExchange exchange = pointsExchangeMapper.selectById(id);
        if (exchange == null) {
            log.warn("发货 - 兑换记录不存在: id={}", id);
            return false;
        }

        // 更新物流信息和状态
        exchange.setLogisticsCompany(logisticsCompany);
        exchange.setTrackingNumber(trackingNumber);
        if (shipRemark != null) {
            exchange.setRemark(shipRemark);
        }
        exchange.setStatus("shipped");
        exchange.setShipTime(LocalDateTime.now());
        exchange.setUpdateTime(LocalDateTime.now());

        return pointsExchangeMapper.updateById(exchange) > 0;
    }

    @Override
    public PointsExchange getExchangeById(Long id) {
        if (id == null) {
            return null;
        }
        return pointsExchangeMapper.selectById(id);
    }

    @Override
    public UserPoints getUserPointsWithStats(Integer userId) {
        if (userId == null) {
            log.warn("获取用户积分统计 - 用户ID为空");
            return null;
        }

        // 查询用户积分基本信息
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoints::getUserId, userId.longValue());
        UserPoints userPoints = userPointsMapper.selectOne(wrapper);

        if (userPoints == null) {
            log.warn("获取用户积分统计 - 用户积分记录不存在: userId={}", userId);
            return null;
        }

        // 填充统计数据
        fillUserPointsStats(userPoints);

        // 关联用户信息
        User user = userService.getById(userId.longValue());
        userPoints.setUser(user);
        
        // 填充用户名（用于前端直接显示）
        if (user != null) {
            userPoints.setUsername(user.getUsername());
        }
        
        // 填充会员等级信息（根据当前积分获取）
        if (userPoints.getPoints() != null) {
            MemberLevel memberLevel = memberLevelService.getLevelByPoints(userPoints.getPoints());
            if (memberLevel != null) {
                // 设置等级名称
                userPoints.setLevelName(memberLevel.getLevelName());
                // 计算等级数字（根据在所有等级中的位置）
                List<MemberLevel> allLevels = memberLevelService.getAllLevels();
                int levelNumber = 1;
                for (int i = 0; i < allLevels.size(); i++) {
                    if (allLevels.get(i).getId().equals(memberLevel.getId())) {
                        levelNumber = i + 1;
                        break;
                    }
                }
                userPoints.setLevel(String.valueOf(levelNumber));
            }
        }

        return userPoints;
    }

    @Override
    public Page<UserPoints> pageUserPointsWithStats(Page<UserPoints> page, LambdaQueryWrapper<UserPoints> queryWrapper) {
        // 先调用原有的分页查询方法
        Page<UserPoints> resultPage = pageWithUser(page, queryWrapper);

        // 为每条记录填充统计数据和会员等级信息
        List<UserPoints> records = resultPage.getRecords();
        if (records != null && !records.isEmpty()) {
            for (UserPoints userPoints : records) {
                // 填充统计数据
                fillUserPointsStats(userPoints);
                
                // 填充用户名
                if (userPoints.getUser() != null) {
                    userPoints.setUsername(userPoints.getUser().getUsername());
                }
                
                // 填充会员等级信息
                if (userPoints.getPoints() != null) {
                    MemberLevel memberLevel = memberLevelService.getLevelByPoints(userPoints.getPoints());
                    if (memberLevel != null) {
                        userPoints.setLevelName(memberLevel.getLevelName());
                        // 计算等级数字（根据在所有等级中的位置）
                        List<MemberLevel> allLevels = memberLevelService.getAllLevels();
                        int levelNumber = 1;
                        for (int i = 0; i < allLevels.size(); i++) {
                            if (allLevels.get(i).getId().equals(memberLevel.getId())) {
                                levelNumber = i + 1;
                                break;
                            }
                        }
                        userPoints.setLevel(String.valueOf(levelNumber));
                    }
                }
            }
        }

        return resultPage;
    }

    /**
     * 填充用户积分统计数据的私有方法
     *
     * @param userPoints 用户积分对象
     */
    private void fillUserPointsStats(UserPoints userPoints) {
        if (userPoints == null || userPoints.getUserId() == null) {
            return;
        }

        Integer userId = userPoints.getUserId().intValue();

        // 统计已获得积分（type='earn'的积分总和）
        LambdaQueryWrapper<PointsHistory> earnWrapper = new LambdaQueryWrapper<>();
        earnWrapper.eq(PointsHistory::getUserId, userId)
                   .eq(PointsHistory::getType, "earn");
        List<PointsHistory> earnRecords = pointsHistoryMapper.selectList(earnWrapper);
        Integer totalEarned = earnRecords.stream()
                .mapToInt(record -> record.getPoints() != null ? record.getPoints() : 0)
                .sum();

        // 统计已使用积分（type='spend'的积分总和，取绝对值）
        LambdaQueryWrapper<PointsHistory> spendWrapper = new LambdaQueryWrapper<>();
        spendWrapper.eq(PointsHistory::getUserId, userId)
                    .eq(PointsHistory::getType, "spend");
        List<PointsHistory> spendRecords = pointsHistoryMapper.selectList(spendWrapper);
        Integer totalUsed = spendRecords.stream()
                .mapToInt(record -> record.getPoints() != null ? Math.abs(record.getPoints()) : 0)
                .sum();

        // 设置统计数据
        userPoints.setTotalEarned(totalEarned);
        userPoints.setTotalUsed(totalUsed);
        userPoints.setAvailablePoints(userPoints.getPoints()); // 可用积分等同于当前积分
        userPoints.setExpiredPoints(0); // 暂未实现过期机制，固定为0
        userPoints.setExpiringSoonPoints(0); // 暂未实现过期机制，固定为0
    }

    @Override
    public Map<String, Object> getExchangeStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 构建基础查询条件
        LambdaQueryWrapper<PointsExchange> baseWrapper = new LambdaQueryWrapper<>();

        // 时间范围筛选
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            baseWrapper.ge(PointsExchange::getCreateTime, startDateTime);
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            baseWrapper.lt(PointsExchange::getCreateTime, endDateTime);
        }

        // 查询所有兑换记录
        List<PointsExchange> allExchanges = pointsExchangeMapper.selectList(baseWrapper);

        // 统计总兑换次数
        stats.put("totalCount", allExchanges.size());

        // 统计总消耗积分
        Integer totalPoints = allExchanges.stream()
                .filter(e -> !"cancelled".equals(e.getStatus()))
                .mapToInt(e -> (e.getPoints() != null ? e.getPoints() : 0) * (e.getQuantity() != null ? e.getQuantity() : 1))
                .sum();
        stats.put("totalPoints", totalPoints);

        // 统计各状态的订单数
        long pendingCount = allExchanges.stream().filter(e -> "pending".equals(e.getStatus())).count();
        long processingCount = allExchanges.stream().filter(e -> "processing".equals(e.getStatus())).count();
        long shippedCount = allExchanges.stream().filter(e -> "shipped".equals(e.getStatus())).count();
        long completedCount = allExchanges.stream().filter(e -> "completed".equals(e.getStatus())).count();
        long cancelledCount = allExchanges.stream().filter(e -> "cancelled".equals(e.getStatus())).count();

        stats.put("pendingCount", pendingCount);
        stats.put("processingCount", processingCount);
        stats.put("shippedCount", shippedCount);
        stats.put("completedCount", completedCount);
        stats.put("cancelledCount", cancelledCount);

        // 统计今日兑换数据
        LocalDate today = LocalDate.now();
        long todayCount = allExchanges.stream()
                .filter(e -> e.getCreateTime() != null && e.getCreateTime().toLocalDate().equals(today))
                .count();
        stats.put("todayCount", todayCount);

        Integer todayPoints = allExchanges.stream()
                .filter(e -> e.getCreateTime() != null && e.getCreateTime().toLocalDate().equals(today))
                .filter(e -> !"cancelled".equals(e.getStatus()))
                .mapToInt(e -> (e.getPoints() != null ? e.getPoints() : 0) * (e.getQuantity() != null ? e.getQuantity() : 1))
                .sum();
        stats.put("todayPoints", todayPoints);

        // 统计热门兑换商品 (Top 5)
        Map<Integer, Long> productCountMap = allExchanges.stream()
                .filter(e -> e.getProductId() != null && !"cancelled".equals(e.getStatus()))
                .collect(Collectors.groupingBy(PointsExchange::getProductId, Collectors.counting()));

        List<Map<String, Object>> topProducts = productCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    Integer productId = entry.getKey();
                    Long count = entry.getValue();

                    PointsProduct product = pointsProductService.getById(productId.longValue());
                    if (product != null) {
                        productInfo.put("productId", productId);
                        productInfo.put("productName", product.getName());
                        productInfo.put("count", count);
                        productInfo.put("points", product.getPoints());
                    }
                    return productInfo;
                })
                .filter(map -> !map.isEmpty())
                .collect(Collectors.toList());

        stats.put("topProducts", topProducts);

        return stats;
    }
}
