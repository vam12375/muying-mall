package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.CouponBatch;
import com.muyingmall.entity.CouponRule;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台优惠券管理控制器
 */
@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
@Tag(name = "后台优惠券管理", description = "优惠券创建、修改、查询等管理功能")
public class AdminCouponController {

    private final CouponService couponService;

    /**
     * 分页查询优惠券列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询优惠券列表")
    public Result<Page<Coupon>> listCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        Page<Coupon> couponPage = couponService.adminListCoupons(page, size, name, type, status);
        return Result.success(couponPage);
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取优惠券详情")
    public Result<Coupon> getCouponDetail(@PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.error(404, "优惠券不存在");
        }
        return Result.success(coupon);
    }

    /**
     * 创建优惠券
     */
    @PostMapping
    @Operation(summary = "创建优惠券")
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        boolean success = couponService.saveCoupon(coupon);
        if (!success) {
            return Result.error("创建优惠券失败");
        }
        return Result.success(coupon, "创建优惠券成功");
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新优惠券")
    public Result<Void> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        boolean success = couponService.updateCoupon(coupon);
        if (!success) {
            return Result.error("更新优惠券失败");
        }
        return Result.success(null, "更新优惠券成功");
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        boolean success = couponService.deleteCoupon(id);
        if (!success) {
            return Result.error("删除优惠券失败");
        }
        return Result.success(null, "删除优惠券成功");
    }

    /**
     * 更新优惠券状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新优惠券状态")
    public Result<Void> updateCouponStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        boolean success = couponService.updateCouponStatus(id, status);
        if (!success) {
            return Result.error("更新优惠券状态失败");
        }
        return Result.success(null, "更新优惠券状态成功");
    }

    /**
     * 分页查询优惠券批次列表
     */
    @GetMapping("/batch/list")
    @Operation(summary = "分页查询优惠券批次列表")
    public Result<Page<CouponBatch>> listCouponBatches(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String couponName) {

        Page<CouponBatch> batchPage = couponService.listCouponBatches(page, size, couponName);
        return Result.success(batchPage);
    }

    /**
     * 创建优惠券批次
     */
    @PostMapping("/batch")
    @Operation(summary = "创建优惠券批次")
    public Result<CouponBatch> createCouponBatch(@RequestBody CouponBatch batch) {
        boolean success = couponService.saveCouponBatch(batch);
        if (!success) {
            return Result.error("创建优惠券批次失败");
        }
        return Result.success(batch, "创建优惠券批次成功");
    }

    /**
     * 获取优惠券批次详情
     */
    @GetMapping("/batch/{batchId}")
    @Operation(summary = "获取优惠券批次详情")
    public Result<CouponBatch> getCouponBatchDetail(@PathVariable Integer batchId) {
        CouponBatch batch = couponService.getCouponBatchDetail(batchId);
        if (batch == null) {
            return Result.error(404, "优惠券批次不存在");
        }
        return Result.success(batch);
    }

    /**
     * 分页查询优惠券规则列表
     */
    @GetMapping("/rule/list")
    @Operation(summary = "分页查询优惠券规则列表")
    public Result<Page<CouponRule>> listCouponRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {

        Page<CouponRule> rulePage = couponService.listCouponRules(page, size, name);
        return Result.success(rulePage);
    }

    /**
     * 创建优惠券规则
     */
    @PostMapping("/rule")
    @Operation(summary = "创建优惠券规则")
    public Result<CouponRule> createCouponRule(@RequestBody CouponRule rule) {
        boolean success = couponService.saveCouponRule(rule);
        if (!success) {
            return Result.error("创建优惠券规则失败");
        }
        return Result.success(rule, "创建优惠券规则成功");
    }

    /**
     * 更新优惠券规则
     */
    @PutMapping("/rule/{ruleId}")
    @Operation(summary = "更新优惠券规则")
    public Result<Void> updateCouponRule(@PathVariable Integer ruleId, @RequestBody CouponRule rule) {
        rule.setRuleId(ruleId);
        boolean success = couponService.updateCouponRule(rule);
        if (!success) {
            return Result.error("更新优惠券规则失败");
        }
        return Result.success(null, "更新优惠券规则成功");
    }

    /**
     * 获取优惠券使用情况统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取优惠券使用情况统计")
    public Result<Map<String, Object>> getCouponStats() {
        Map<String, Object> stats = couponService.getCouponStats();
        return Result.success(stats);
    }

    /**
     * 查询用户优惠券列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户优惠券列表")
    public Result<List<UserCoupon>> getUserCoupons(
            @PathVariable Integer userId,
            @RequestParam(required = false, defaultValue = "all") String status) {

        List<UserCoupon> userCoupons = couponService.getUserCoupons(userId, status);
        return Result.success(userCoupons);
    }
}