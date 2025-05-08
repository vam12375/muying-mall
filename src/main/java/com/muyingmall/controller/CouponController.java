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
}