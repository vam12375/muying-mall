 package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.constants.CacheConstants;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.CouponMapper;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.service.CouponService;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    @Override
    @SuppressWarnings("unchecked")
    public List<Coupon> getAvailableCoupons(Integer userId) {
        // 构建缓存键（可用优惠券列表不包含用户特定信息，用户领取状态单独处理）
        String cacheKey = CacheConstants.COUPON_AVAILABLE_KEY;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        List<Coupon> coupons;
        
        if (cached != null) {
            log.debug("从缓存获取可用优惠券列表");
            coupons = (List<Coupon>) cached;
        } else {
            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询可用优惠券列表");
            LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Coupon::getStatus, "ACTIVE") // 优惠券状态为可用
                    .gt(Coupon::getEndTime, LocalDateTime.now()) // 未过期
                    .orderByAsc(Coupon::getMinSpend) // 按最低使用金额排序
                    .orderByDesc(Coupon::getValue); // 再按金额排序

            coupons = list(queryWrapper);
            
            // 缓存结果
            if (coupons != null) {
                redisUtil.set(cacheKey, coupons, CacheConstants.COUPON_EXPIRE_TIME);
                log.debug("将可用优惠券列表缓存到Redis, 过期时间={}秒", CacheConstants.COUPON_EXPIRE_TIME);
            }
        }

        // 如果用户已登录，标记哪些优惠券已领取
        if (userId != null && coupons != null && coupons.size() > 0) {
            // 查询用户已领取的优惠券
            LambdaQueryWrapper<UserCoupon> userCouponQuery = new LambdaQueryWrapper<>();
            userCouponQuery.eq(UserCoupon::getUserId, userId)
                    .in(UserCoupon::getCouponId, coupons.stream().map(Coupon::getId).toList());

            List<UserCoupon> userCoupons = userCouponMapper.selectList(userCouponQuery);

            // 设置已领取标记（考虑 userLimit 多次领取场景）
            // 统计每张优惠券的领取次数
            Map<Long, Long> receiveCountMap = userCoupons.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            UserCoupon::getCouponId, java.util.stream.Collectors.counting()));

            for (Coupon coupon : coupons) {
                long receivedCount = receiveCountMap.getOrDefault(coupon.getId(), 0L);
                if (receivedCount == 0) {
                    coupon.setIsReceived(false);
                } else if (coupon.getUserLimit() != null && coupon.getUserLimit() > 0) {
                    // 有领取限制：达到上限才标记已领取
                    coupon.setIsReceived(receivedCount >= coupon.getUserLimit());
                } else {
                    // 无领取限制（userLimit=0或null），已领取过就标记
                    coupon.setIsReceived(true);
                }
            }
        }

        return coupons;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserCoupon> getUserCoupons(Integer userId, String status) {
        // 构建缓存键
        String cacheKey = CacheConstants.USER_COUPON_LIST_KEY + userId + ":" + (status != null ? status : "all");
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取用户优惠券列表: userId={}, status={}", userId, status);
            return (List<UserCoupon>) cached;
        }
        
        // 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询用户优惠券列表: userId={}, status={}", userId, status);
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

        // 缓存结果
        if (userCoupons != null) {
            redisUtil.set(cacheKey, userCoupons, CacheConstants.COUPON_EXPIRE_TIME);
            log.debug("将用户优惠券列表缓存到Redis: userId={}, status={}, 过期时间={}秒", userId, status, CacheConstants.COUPON_EXPIRE_TIME);
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

        // 检查用户是否已领取过该优惠券（如果有限制）
        if (coupon.getUserLimit() != null && coupon.getUserLimit() > 0) {
            LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, couponId);

            long count = userCouponMapper.selectCount(queryWrapper);
            if (count >= coupon.getUserLimit()) {
                throw new BusinessException("您已领取过该优惠券");
            }
        }

        // CAS方式安全递增领取数量，防止并发超发
        // 仅当库存充足时才能成功更新（total_quantity=0 表示不限量，始终成功）
        int rows = baseMapper.incrementReceivedQuantity(couponId);
        if (rows == 0) {
            throw new BusinessException("优惠券已被领完");
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

        // 清除用户优惠券缓存和可用优惠券缓存
        clearUserCouponCache(userId);
        redisUtil.del(CacheConstants.COUPON_AVAILABLE_KEY);

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

        // 批量查询商品信息（用于分类/品牌匹配）
        List<Product> products = new ArrayList<>();
        if (productIds != null && !productIds.isEmpty()) {
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.in(Product::getProductId, productIds);
            products = productMapper.selectList(productQuery);
        }
        final List<Product> finalProducts = products;

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
                            // 检查商品适用范围（支持 productIds/categoryIds/brandIds 三个维度）
                            if (checkProductApplicable(coupon, productIds, finalProducts)) {
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
     * 支持三个维度的匹配：productIds、categoryIds、brandIds
     * 全部为空时表示全场通用
     */
    private boolean checkProductApplicable(Coupon coupon, List<Integer> productIds, List<Product> products) {
        boolean hasProductRestriction = coupon.getProductIds() != null && !coupon.getProductIds().isEmpty();
        boolean hasCategoryRestriction = coupon.getCategoryIds() != null && !coupon.getCategoryIds().isEmpty();
        boolean hasBrandRestriction = coupon.getBrandIds() != null && !coupon.getBrandIds().isEmpty();

        // 全场通用，没有任何限制
        if (!hasProductRestriction && !hasCategoryRestriction && !hasBrandRestriction) {
            return true;
        }

        if (productIds == null || productIds.isEmpty()) {
            return false;
        }

        // 检查商品ID限制
        if (hasProductRestriction) {
            Set<String> couponProductIds = Set.of(coupon.getProductIds().split(","));
            for (Integer pid : productIds) {
                if (couponProductIds.contains(String.valueOf(pid))) {
                    return true;
                }
            }
        }

        // 检查分类ID限制
        if (hasCategoryRestriction && !products.isEmpty()) {
            Set<String> couponCategoryIds = Set.of(coupon.getCategoryIds().split(","));
            for (Product product : products) {
                if (product.getCategoryId() != null
                        && couponCategoryIds.contains(String.valueOf(product.getCategoryId()))) {
                    return true;
                }
            }
        }

        // 检查品牌ID限制
        if (hasBrandRestriction && !products.isEmpty()) {
            Set<String> couponBrandIds = Set.of(coupon.getBrandIds().split(","));
            for (Product product : products) {
                if (product.getBrandId() != null
                        && couponBrandIds.contains(String.valueOf(product.getBrandId()))) {
                    return true;
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
        boolean result = save(coupon);
        if (result) {
            // 清除可用优惠券缓存
            redisUtil.del(CacheConstants.COUPON_AVAILABLE_KEY);
        }
        return result;
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        coupon.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(coupon);
        if (result) {
            // 清除可用优惠券缓存和优惠券详情缓存
            redisUtil.del(CacheConstants.COUPON_AVAILABLE_KEY);
            redisUtil.del(CacheConstants.COUPON_DETAIL_KEY + coupon.getId());
        }
        return result;
    }

    @Override
    public boolean deleteCoupon(Long id) {
        boolean result = removeById(id);
        if (result) {
            // 清除可用优惠券缓存和优惠券详情缓存
            redisUtil.del(CacheConstants.COUPON_AVAILABLE_KEY);
            redisUtil.del(CacheConstants.COUPON_DETAIL_KEY + id);
        }
        return result;
    }

    @Override
    public boolean updateCouponStatus(Long id, String status) {
        Coupon coupon = getById(id);
        if (coupon == null) {
            return false;
        }

        coupon.setStatus(status);
        coupon.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(coupon);
        if (result) {
            // 清除可用优惠券缓存
            redisUtil.del(CacheConstants.COUPON_AVAILABLE_KEY);
        }
        return result;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCouponByCode(Integer userId, String code) {
        // 使用 code 字段精确查询，避免全表扫描
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getCode, code)
                .eq(Coupon::getStatus, "ACTIVE")
                .gt(Coupon::getEndTime, LocalDateTime.now())
                .last("LIMIT 1");

        Coupon matchedCoupon = getOne(queryWrapper, false);

        if (matchedCoupon == null) {
            throw new BusinessException("优惠码无效或已过期");
        }

        // 调用领取优惠券方法
        return receiveCoupon(userId, matchedCoupon.getId());
    }

    @Override
    public Map<String, Object> getUserCouponStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询可领取的优惠券数量
        LambdaQueryWrapper<Coupon> availableQuery = new LambdaQueryWrapper<>();
        availableQuery.eq(Coupon::getStatus, "ACTIVE")
                .gt(Coupon::getEndTime, LocalDateTime.now());
        long availableCount = count(availableQuery);

        // 查询用户的优惠券数量
        LambdaQueryWrapper<UserCoupon> mineQuery = new LambdaQueryWrapper<>();
        mineQuery.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "UNUSED")
                .gt(UserCoupon::getExpireTime, LocalDateTime.now());
        long mineCount = userCouponMapper.selectCount(mineQuery);

        // 查询已使用的优惠券数量
        LambdaQueryWrapper<UserCoupon> usedQuery = new LambdaQueryWrapper<>();
        usedQuery.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "USED");
        long usedCount = userCouponMapper.selectCount(usedQuery);

        // 查询已过期的优惠券数量
        LambdaQueryWrapper<UserCoupon> expiredQuery = new LambdaQueryWrapper<>();
        expiredQuery.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "UNUSED")
                .le(UserCoupon::getExpireTime, LocalDateTime.now());
        long expiredCount = userCouponMapper.selectCount(expiredQuery);

        // 批量查询已使用的优惠券，避免N+1循环查询
        BigDecimal totalSavings = BigDecimal.ZERO;
        List<UserCoupon> usedCoupons = userCouponMapper.selectList(usedQuery);
        if (!usedCoupons.isEmpty()) {
            List<Long> couponIds = usedCoupons.stream()
                    .map(UserCoupon::getCouponId)
                    .distinct()
                    .toList();
            Map<Long, Coupon> couponMap = listByIds(couponIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Coupon::getId, c -> c));
            for (UserCoupon uc : usedCoupons) {
                Coupon coupon = couponMap.get(uc.getCouponId());
                if (coupon != null && "FIXED".equals(coupon.getType())) {
                    totalSavings = totalSavings.add(coupon.getValue());
                }
            }
        }

        stats.put("availableCount", availableCount);
        stats.put("mineCount", mineCount);
        stats.put("usedCount", usedCount);
        stats.put("expiredCount", expiredCount);
        stats.put("totalSavings", "¥" + totalSavings.toString());

        return stats;
    }
    
    /**
     * 清除用户优惠券缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCouponCache(Integer userId) {
        if (userId == null) {
            return;
        }
        // 使用 SCAN 替代 KEYS，避免生产环境 Redis 阻塞
        String pattern = CacheConstants.USER_COUPON_LIST_KEY + userId + ":*";
        redisUtil.deleteByScan(pattern);
        log.debug("清除用户优惠券缓存: userId={}", userId);
    }
}