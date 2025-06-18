package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.mapper.CouponMapper;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.CouponBatch;
import com.muyingmall.entity.CouponRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> getAvailableCoupons(Integer userId) {
        // 查询所有可用的优惠券
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getStatus, "ACTIVE") // 优惠券状态为可用
                .gt(Coupon::getEndTime, LocalDateTime.now()) // 未过期
                .orderByAsc(Coupon::getMinSpend) // 按最低使用金额排序
                .orderByDesc(Coupon::getValue); // 再按金额排序

        List<Coupon> coupons = list(queryWrapper);

        // 如果用户已登录，标记哪些优惠券已领取
        if (userId != null && coupons.size() > 0) {
            // 查询用户已领取的优惠券
            LambdaQueryWrapper<UserCoupon> userCouponQuery = new LambdaQueryWrapper<>();
            userCouponQuery.eq(UserCoupon::getUserId, userId)
                    .in(UserCoupon::getCouponId, coupons.stream().map(Coupon::getId).toList());

            List<UserCoupon> userCoupons = userCouponMapper.selectList(userCouponQuery);

            // 设置已领取标记
            for (Coupon coupon : coupons) {
                coupon.setIsReceived(userCoupons.stream()
                        .anyMatch(uc -> uc.getCouponId().equals(coupon.getId())));
            }
        }

        return coupons;
    }

    @Override
    public List<UserCoupon> getUserCoupons(Integer userId, String status) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);

        // 根据状态筛选
        if ("UNUSED".equals(status)) {
            queryWrapper.eq(UserCoupon::getStatus, "UNUSED")
                    .gt(UserCoupon::getExpireTime, LocalDateTime.now());
        } else if ("USED".equals(status)) {
            queryWrapper.eq(UserCoupon::getStatus, "USED");
        } else if ("EXPIRED".equals(status)) {
            queryWrapper.eq(UserCoupon::getStatus, "UNUSED")
                    .le(UserCoupon::getExpireTime, LocalDateTime.now());
        }

        queryWrapper.orderByDesc(UserCoupon::getCreateTime);

        // 查询用户优惠券
        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);

        // 查询优惠券详情
        if (!userCoupons.isEmpty()) {
            List<Long> couponIds = userCoupons.stream()
                    .map(UserCoupon::getCouponId)
                    .toList();

            LambdaQueryWrapper<Coupon> couponQuery = new LambdaQueryWrapper<>();
            couponQuery.in(Coupon::getId, couponIds);

            List<Coupon> coupons = list(couponQuery);

            // 设置优惠券详情
            for (UserCoupon userCoupon : userCoupons) {
                coupons.stream()
                        .filter(c -> c.getId().equals(userCoupon.getCouponId()))
                        .findFirst()
                        .ifPresent(userCoupon::setCoupon);
            }
        }

        return userCoupons;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Integer userId, Long couponId) {
        // 查询优惠券是否存在
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 检查优惠券是否可用
        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new BusinessException("优惠券已下架");
        }

        // 检查优惠券是否过期
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        // 检查是否已达到发放数量上限
        if (coupon.getTotalQuantity() > 0 && coupon.getReceivedQuantity() >= coupon.getTotalQuantity()) {
            throw new BusinessException("优惠券已被领完");
        }

        // 检查用户是否已领取过该优惠券
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);

        long count = userCouponMapper.selectCount(queryWrapper);
        if (count >= coupon.getUserLimit()) {
            throw new BusinessException("您已领取过该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setBatchId(coupon.getBatchId());
        userCoupon.setStatus("UNUSED");
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setExpireTime(coupon.getEndTime());
        userCoupon.setCreateTime(LocalDateTime.now());
        userCoupon.setUpdateTime(LocalDateTime.now());

        userCouponMapper.insert(userCoupon);

        // 更新优惠券领取数量
        coupon.setReceivedQuantity(coupon.getReceivedQuantity() + 1);
        updateById(coupon);

        return true;
    }

    @Override
    public List<UserCoupon> getOrderCoupons(Integer userId, Double amount, List<Integer> productIds) {
        // 查询用户未使用的优惠券
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "UNUSED")
                .gt(UserCoupon::getExpireTime, LocalDateTime.now());

        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);
        if (userCoupons.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询优惠券详情
        List<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .toList();

        LambdaQueryWrapper<Coupon> couponQuery = new LambdaQueryWrapper<>();
        couponQuery.in(Coupon::getId, couponIds);

        List<Coupon> coupons = list(couponQuery);

        // 筛选符合订单金额条件的优惠券
        List<UserCoupon> availableCoupons = new ArrayList<>();
        BigDecimal orderAmount = BigDecimal.valueOf(amount);

        for (UserCoupon userCoupon : userCoupons) {
            coupons.stream()
                    .filter(c -> c.getId().equals(userCoupon.getCouponId()))
                    .findFirst()
                    .ifPresent(coupon -> {
                        userCoupon.setCoupon(coupon);

                        // 检查最低使用金额
                        if (orderAmount.compareTo(coupon.getMinSpend()) >= 0) {
                            // 检查商品适用范围
                            if (checkProductApplicable(coupon, productIds)) {
                                availableCoupons.add(userCoupon);
                            }
                        }
                    });
        }

        // 按优惠券金额从大到小排序
        availableCoupons.sort((a, b) -> b.getCoupon().getValue().compareTo(a.getCoupon().getValue()));

        return availableCoupons;
    }

    /**
     * 检查优惠券是否适用于指定商品
     */
    private boolean checkProductApplicable(Coupon coupon, List<Integer> productIds) {
        // 全场通用，没有商品限制
        if (coupon.getProductIds() == null || coupon.getProductIds().isEmpty()) {
            return true;
        }

        // 商品限制
        if (productIds != null && !productIds.isEmpty()) {
            // 解析优惠券适用的商品ID
            String[] couponProductIds = coupon.getProductIds().split(",");
            for (String cpId : couponProductIds) {
                for (Integer pid : productIds) {
                    if (cpId.equals(String.valueOf(pid))) {
                        return true; // 只要有一个商品适用即可
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Page<Coupon> adminListCoupons(Integer page, Integer size, String name, String type, String status) {
        Page<Coupon> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like(Coupon::getName, name);
        }

        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Coupon::getType, type);
        }

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Coupon::getStatus, status);
        }

        queryWrapper.orderByDesc(Coupon::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public boolean saveCoupon(Coupon coupon) {
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        coupon.setReceivedQuantity(0);
        return save(coupon);
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        coupon.setUpdateTime(LocalDateTime.now());
        return updateById(coupon);
    }

    @Override
    public boolean deleteCoupon(Long id) {
        return removeById(id);
    }

    @Override
    public boolean updateCouponStatus(Long id, String status) {
        Coupon coupon = getById(id);
        if (coupon == null) {
            return false;
        }

        coupon.setStatus(status);
        coupon.setUpdateTime(LocalDateTime.now());
        return updateById(coupon);
    }

    @Override
    public Page<CouponBatch> listCouponBatches(Integer page, Integer size, String couponName) {
        // 为简化返回模拟数据，实际项目中应查询数据库
        Page<CouponBatch> result = new Page<>(page, size, 0);
        return result;
    }

    @Override
    public boolean saveCouponBatch(CouponBatch batch) {
        // 为简化返回成功，实际项目中应保存到数据库
        return true;
    }

    @Override
    public CouponBatch getCouponBatchDetail(Integer batchId) {
        // 为简化返回null，实际项目中应查询数据库
        return null;
    }

    @Override
    public Page<CouponRule> listCouponRules(Integer page, Integer size, String name) {
        // 为简化返回模拟数据，实际项目中应查询数据库
        Page<CouponRule> result = new Page<>(page, size, 0);
        return result;
    }

    @Override
    public boolean saveCouponRule(CouponRule rule) {
        // 为简化返回成功，实际项目中应保存到数据库
        return true;
    }

    @Override
    public boolean updateCouponRule(CouponRule rule) {
        // 为简化返回成功，实际项目中应更新数据库
        return true;
    }

    @Override
    public Map<String, Object> getCouponStats() {
        Map<String, Object> stats = new HashMap<>();

        // 查询优惠券总数
        LambdaQueryWrapper<Coupon> totalQuery = new LambdaQueryWrapper<>();
        long totalCoupons = count(totalQuery);

        // 查询已领取的优惠券数量
        LambdaQueryWrapper<UserCoupon> receivedQuery = new LambdaQueryWrapper<>();
        long receivedCount = userCouponMapper.selectCount(receivedQuery);

        // 查询已使用的优惠券数量
        LambdaQueryWrapper<UserCoupon> usedQuery = new LambdaQueryWrapper<>();
        usedQuery.eq(UserCoupon::getStatus, "USED");
        long usedCoupons = userCouponMapper.selectCount(usedQuery);

        // 查询已过期的优惠券数量
        LambdaQueryWrapper<UserCoupon> expiredQuery = new LambdaQueryWrapper<>();
        expiredQuery.eq(UserCoupon::getStatus, "UNUSED")
                .le(UserCoupon::getExpireTime, LocalDateTime.now());
        long expiredCoupons = userCouponMapper.selectCount(expiredQuery);

        stats.put("totalCoupons", totalCoupons);
        stats.put("receivedCount", receivedCount);
        stats.put("usedCoupons", usedCoupons);
        stats.put("expiredCoupons", expiredCoupons);

        return stats;
    }
}