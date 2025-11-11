package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.response.Result;
import com.muyingmall.entity.*;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.service.PointsProductService;
import com.muyingmall.service.PointsRuleService;
import com.muyingmall.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 后台积分管理控制器
 */
@RestController
@RequestMapping("/admin/points")
@RequiredArgsConstructor
@Tag(name = "后台积分管理", description = "积分规则配置、积分历史查询等管理功能")
public class AdminPointsController {

    private final PointsService pointsService;
    private final PointsRuleService pointsRuleService;
    private final PointsProductService pointsProductService;
    private final PointsOperationService pointsOperationService;

    /**
     * 分页查询积分历史记录
     */
    @GetMapping("/history/list")
    @Operation(summary = "分页查询积分历史记录")
    public Result<Page<PointsHistory>> listPointsHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<PointsHistory> historyPage = pointsService.adminListPointsHistory(page, size, userId, type, source,
                startDate, endDate);
        return Result.success(historyPage);
    }

    /**
     * 分页查询积分操作日志
     */
    @GetMapping("/operation/list")
    @Operation(summary = "分页查询积分操作日志")
    public Result<Page<PointsOperationLog>> listPointsOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<PointsOperationLog> logsPage = pointsOperationService.adminListOperationLogs(page, size, userId,
                operationType, startDate, endDate);
        return Result.success(logsPage);
    }

    /**
     * 获取用户积分信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户积分信息")
    public Result<UserPoints> getUserPoints(@PathVariable Integer userId) {
        UserPoints userPoints = pointsService.getById(userId);
        if (userPoints == null) {
            return Result.error(404, "用户积分信息不存在");
        }
        return Result.success(userPoints);
    }

    /**
     * 管理员调整用户积分
     */
    @PostMapping("/user/{userId}/adjust")
    @Operation(summary = "管理员调整用户积分")
    public Result<Void> adjustUserPoints(
            @PathVariable Integer userId,
            @RequestParam Integer points,
            @RequestParam String description) {

        boolean success = pointsService.adminAdjustPoints(userId, points, description);
        if (!success) {
            return Result.error("调整用户积分失败");
        }
        return Result.success(null, "调整用户积分成功");
    }

    /**
     * 分页查询积分规则列表
     */
    @GetMapping("/rule/list")
    @Operation(summary = "分页查询积分规则列表")
    public Result<Page<PointsRule>> listPointsRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {

        Page<PointsRule> rulePage = pointsRuleService.adminListPointsRules(page, size, name);
        return Result.success(rulePage);
    }

    /**
     * 创建积分规则
     */
    @PostMapping("/rule")
    @Operation(summary = "创建积分规则")
    public Result<PointsRule> createPointsRule(@RequestBody PointsRule rule) {
        boolean success = pointsRuleService.save(rule);
        if (!success) {
            return Result.error("创建积分规则失败");
        }
        return Result.success(rule, "创建积分规则成功");
    }

    /**
     * 更新积分规则
     */
    @PutMapping("/rule/{id}")
    @Operation(summary = "更新积分规则")
    public Result<Void> updatePointsRule(@PathVariable Long id, @RequestBody PointsRule rule) {
        rule.setId(id);
        boolean success = pointsRuleService.updateById(rule);
        if (!success) {
            return Result.error("更新积分规则失败");
        }
        return Result.success(null, "更新积分规则成功");
    }

    /**
     * 删除积分规则
     */
    @DeleteMapping("/rule/{id}")
    @Operation(summary = "删除积分规则")
    public Result<Void> deletePointsRule(@PathVariable Integer id) {
        boolean success = pointsRuleService.removeById(id);
        if (!success) {
            return Result.error("删除积分规则失败");
        }
        return Result.success(null, "删除积分规则成功");
    }

    /**
     * 分页查询积分商品列表
     */
    @GetMapping("/product/list")
    @Operation(summary = "分页查询积分商品列表")
    public Result<Page<PointsProduct>> listPointsProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        Page<PointsProduct> productPage = pointsProductService.adminListPointsProducts(page, size, name, category);
        return Result.success(productPage);
    }

    /**
     * 创建积分商品
     */
    @PostMapping("/product")
    @Operation(summary = "创建积分商品")
    public Result<PointsProduct> createPointsProduct(@RequestBody PointsProduct product) {
        boolean success = pointsProductService.save(product);
        if (!success) {
            return Result.error("创建积分商品失败");
        }
        return Result.success(product, "创建积分商品成功");
    }

    /**
     * 更新积分商品
     */
    @PutMapping("/product/{id}")
    @Operation(summary = "更新积分商品")
    public Result<Void> updatePointsProduct(@PathVariable Long id, @RequestBody PointsProduct product) {
        product.setId(id);
        boolean success = pointsProductService.updateById(product);
        if (!success) {
            return Result.error("更新积分商品失败");
        }
        return Result.success(null, "更新积分商品成功");
    }

    /**
     * 删除积分商品
     */
    @DeleteMapping("/product/{id}")
    @Operation(summary = "删除积分商品")
    public Result<Void> deletePointsProduct(@PathVariable Long id) {
        boolean success = pointsProductService.removeById(id);
        if (!success) {
            return Result.error("删除积分商品失败");
        }
        return Result.success(null, "删除积分商品成功");
    }

    /**
     * 获取积分兑换记录
     */
    @GetMapping("/exchange/list")
    @Operation(summary = "获取积分兑换记录")
    public Result<Page<PointsExchange>> listPointsExchanges(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<PointsExchange> exchangePage = pointsService.adminListPointsExchanges(page, size, userId, productId,
                status, startDate, endDate);
        return Result.success(exchangePage);
    }

    /**
     * 更新积分兑换状态
     */
    @PutMapping("/exchange/{id}/status")
    @Operation(summary = "更新积分兑换状态")
    public Result<Void> updateExchangeStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        boolean success = pointsService.updateExchangeStatus(id, status);
        if (!success) {
            return Result.error("更新积分兑换状态失败");
        }
        return Result.success(null, "更新积分兑换状态成功");
    }

    /**
     * 积分兑换发货
     */
    @PostMapping("/exchange/{id}/ship")
    @Operation(summary = "积分兑换发货")
    public Result<Void> shipExchange(
            @PathVariable Long id,
            @RequestBody Map<String, Object> shipData) {

        String logisticsCompany = (String) shipData.get("logisticsCompany");
        String trackingNumber = (String) shipData.get("trackingNumber");
        String shipRemark = (String) shipData.get("shipRemark");

        boolean success = pointsService.shipExchange(id, logisticsCompany, trackingNumber, shipRemark);
        if (!success) {
            return Result.error("发货失败");
        }
        return Result.success(null, "发货成功");
    }

    /**
     * 获取积分兑换详情
     */
    @GetMapping("/exchange/{id}")
    @Operation(summary = "获取积分兑换详情")
    public Result<PointsExchange> getExchangeDetail(@PathVariable Long id) {
        PointsExchange exchange = pointsService.getExchangeById(id);
        if (exchange == null) {
            return Result.error(404, "兑换记录不存在");
        }
        return Result.success(exchange);
    }

    /**
     * 获取积分统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取积分统计数据")
    public Result<Map<String, Object>> getPointsStats(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Map<String, Object> stats = pointsService.getPointsStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 分页查询用户积分列表
     */
    @GetMapping("/user/list")
    @Operation(summary = "分页查询用户积分列表")
    public Result<Page<UserPoints>> listUserPoints(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String username) {

        Page<UserPoints> userPointsPage = new Page<>(page, size);
        LambdaQueryWrapper<UserPoints> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (userId != null) {
            queryWrapper.eq(UserPoints::getUserId, userId.longValue());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(UserPoints::getCreateTime);

        // 使用pageWithUser方法查询并关联用户信息
        Page<UserPoints> resultPage = pointsService.pageWithUser(userPointsPage, queryWrapper);

        // 用户名称查询需要关联用户表，简化处理，只支持userId过滤
        // 如果有username参数，可以在service层实现复杂查询
        // 此处简化处理，不支持username过滤

        return Result.success(resultPage);
    }
}