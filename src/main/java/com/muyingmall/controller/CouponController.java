package com.muyingmall.controller;

import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.service.CouponService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "优惠券管理", description = "优惠券查询、领取等接口")
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;

    /**
     * 获取可用优惠券列表
     */
    @GetMapping("/coupons/available")
    @Operation(summary = "获取可用优惠券列表")
    public Result<List<Coupon>> listAvailableCoupons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user != null) {
                userId = user.getUserId();
            }
        }

        List<Coupon> coupons = couponService.getAvailableCoupons(userId);
        return Result.success(coupons);
    }

    /**
     * 获取用户优惠券列表
     */
    @GetMapping("/user/coupons")
    @Operation(summary = "获取用户优惠券列表")
    public Result<List<UserCoupon>> getUserCoupons(
            @RequestParam(required = false, defaultValue = "all") String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        List<UserCoupon> userCoupons = couponService.getUserCoupons(user.getUserId(), status);
        return Result.success(userCoupons);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/coupons/{couponId}/receive")
    @Operation(summary = "领取优惠券")
    public Result<Void> receiveCoupon(@PathVariable("couponId") Long couponId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean success = couponService.receiveCoupon(user.getUserId(), couponId);
        if (!success) {
            return Result.error("领取失败");
        }
        return Result.success(null, "领取成功");
    }

    /**
     * 获取订单可用优惠券
     * 接收数组参数为普通形式而非方括号语法
     * 
     * @param amount     订单金额
     * @param productIds 商品ID列表
     * @return 可用优惠券列表
     */
    @GetMapping("/user/coupons/order")
    @Operation(summary = "获取订单可用优惠券")
    public Result<List<UserCoupon>> getOrderCoupons(
            @RequestParam("amount") Double amount,
            @RequestParam(required = false) List<Integer> productIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        List<UserCoupon> userCoupons = couponService.getOrderCoupons(user.getUserId(), amount, productIds);
        return Result.success(userCoupons);
    }

    /**
     * 通过优惠码领取优惠券
     */
    @PostMapping("/coupons/receive-by-code")
    @Operation(summary = "通过优惠码领取优惠券")
    public Result<Void> receiveCouponByCode(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return Result.error("优惠码不能为空");
        }

        boolean success = couponService.receiveCouponByCode(user.getUserId(), code.trim());
        if (!success) {
            return Result.error("优惠码无效或已被使用");
        }
        return Result.success(null, "兑换成功");
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/coupons/{couponId}")
    @Operation(summary = "获取优惠券详情")
    public Result<Coupon> getCouponDetail(@PathVariable("couponId") Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null) {
            return Result.error(404, "优惠券不存在");
        }
        return Result.success(coupon);
    }

    /**
     * 获取优惠券统计数据
     */
    @GetMapping("/user/coupons/stats")
    @Operation(summary = "获取用户优惠券统计数据")
    public Result<Map<String, Object>> getCouponStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> stats = couponService.getUserCouponStats(user.getUserId());
        return Result.success(stats);
    }
}