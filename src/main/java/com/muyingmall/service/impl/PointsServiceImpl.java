package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.*;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.mapper.PointsRuleMapper;
import com.muyingmall.service.PointsExchangeService;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.service.PointsProductService;
import com.muyingmall.service.PointsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 积分管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointsServiceImpl implements PointsService {

    private final PointsHistoryMapper pointsHistoryMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final PointsProductService pointsProductService;
    private final PointsOperationService pointsOperationService;

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

        LambdaQueryWrapper<PointsHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsHistory::getUserId, userId)
                .orderByDesc(PointsHistory::getCreateTime);

        return pointsHistoryMapper.selectPage(pageParam, queryWrapper);
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
                throw new BusinessException("今日已签到");
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

            // 连续签到奖励计算
            int continuousDays = (int) signInStatus.get("continuousDays");

            // 可以根据连续签到天数增加额外奖励
            int extraPoints = 0;
            if (continuousDays >= 7) {
                extraPoints = 10; // 连续签到7天以上额外10积分
            } else if (continuousDays >= 3) {
                extraPoints = 5; // 连续签到3天以上额外5积分
            }

            int totalPoints = signInPoints + extraPoints;

            // 构建签到描述
            String description = "每日签到";
            if (extraPoints > 0) {
                description += "，连续签到" + (continuousDays + 1) + "天";
            }

            // 记录积分
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

            // 获取用户总积分
            Integer totalPoints = 0;
            try {
                totalPoints = getUserPoints(userId);
            } catch (Exception e) {
                log.error("获取用户总积分失败: {}", e.getMessage());
                // 继续执行，使用默认值0
            }

            // 构建结果
            result.put("todaySigned", todaySigned);
            result.put("continuousDays", continuousDays);
            result.put("historyMaxContinuousDays", historyMaxContinuousDays);
            result.put("totalPoints", totalPoints);

            // 添加可能的下一次签到可获得的积分数
            int nextSignInPoints = calculateNextSignInPoints(continuousDays);
            result.put("nextSignInPoints", nextSignInPoints);

            log.debug("用户 {} 的签到状态: 今日已签到={}, 连续签到天数={}, 历史最长连续签到={}, 总积分={}, 下次签到积分={}",
                    userId, todaySigned, continuousDays, historyMaxContinuousDays, totalPoints, nextSignInPoints);

            return result;
        } catch (Exception e) {
            log.error("获取用户 {} 的签到状态失败: {}", userId, e.getMessage(), e);
            // 返回基本信息，避免前端出错
            result.put("todaySigned", false);
            result.put("continuousDays", 0);
            result.put("historyMaxContinuousDays", 0);
            result.put("totalPoints", 0);
            result.put("nextSignInPoints", 20); // 默认为20
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

        // 计算额外积分
        int extraPoints = 0;
        if (nextDay >= 7) {
            extraPoints = 10; // 下一次是连续7天或以上，额外10积分
        } else if (nextDay >= 3) {
            extraPoints = 5; // 下一次是连续3天或以上，额外5积分
        }

        return basePoints + extraPoints;
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
        exchange.setStatus(0); // 待发货
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
        return pointsProductService.getPointsProductPage(page, size, category, null);
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
            // 签到获取积分
            Integer earnedPoints = signIn(userId);

            // 获取最新的签到状态
            Map<String, Object> status = getSignInStatus(userId);

            // 合并结果
            result.putAll(status);
            result.put("earnedPoints", earnedPoints);
            // 确保积分值也放在根级别，方便前端直接获取
            result.put("points", earnedPoints);
            result.put("success", true);
            result.put("message", "签到成功");

            log.info("用户 {} 签到成功，获得 {} 积分", userId, earnedPoints);
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
                result.put("points", 0); // 确保前端显示0
            } catch (Exception ex) {
                log.error("获取用户 {} 签到状态失败: {}", userId, ex.getMessage());
                // 添加默认值，避免前端出错
                result.put("todaySigned", true); // 假设已签到
                result.put("continuousDays", 0);
                result.put("historyMaxContinuousDays", 0);
                result.put("totalPoints", 0);
                result.put("nextSignInPoints", 20);
                result.put("earnedPoints", 0);
                result.put("points", 0);
            }

            return result;
        } catch (Exception e) {
            // 其他异常
            log.error("用户 {} 签到异常: {}", userId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "签到失败: " + e.getMessage());

            // 添加默认值，避免前端出错
            result.put("todaySigned", false);
            result.put("continuousDays", 0);
            result.put("historyMaxContinuousDays", 0);
            result.put("totalPoints", 0);
            result.put("earnedPoints", 0);
            result.put("points", 0);
            result.put("nextSignInPoints", 20);

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

            // 合并结果
            result.put("monthInfo", monthInfo);
            result.put("signInStatus", signInStatus);
            result.put("success", true);

            return result;
        } catch (Exception e) {
            log.error("获取用户 {} 的签到日历失败: {}", userId, e.getMessage(), e);

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

            result.put("monthInfo", basicMonthInfo);
            result.put("signInStatus", basicStatus);
            result.put("success", false);
            result.put("message", "获取签到日历失败: " + e.getMessage());

            return result;
        }
    }
}
