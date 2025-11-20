package com.muyingmall.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.PointsExchange;
import com.muyingmall.entity.PointsHistory;
import com.muyingmall.entity.PointsProduct;
import com.muyingmall.entity.PointsRule;
import com.muyingmall.entity.User;
import com.muyingmall.service.PointsExchangeService;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分控制器
 */
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "积分管理", description = "积分查询、签到、兑换等接口")
public class PointsController {

    private final PointsService pointsService;
    private final PointsExchangeService pointsExchangeService;
    private final UserService userService;

    /**
     * 获取用户积分信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户积分信息")
    public Result<Map<String, Object>> getUserPoints() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> pointsInfo = pointsService.getSignInStatus(user.getUserId());
        return Result.success(pointsInfo);
    }

    /**
     * 获取积分历史记录
     */
    @GetMapping("/records")
    @Operation(summary = "获取积分历史记录")
    public Result<Page<PointsHistory>> getPointsRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<PointsHistory> historyPage = pointsService.getUserPointsHistory(user.getUserId(), page, pageSize);
        return Result.success(historyPage);
    }

    /**
     * 获取积分规则
     */
    @GetMapping("/rules")
    @Operation(summary = "获取积分规则")
    public Result<List<PointsRule>> getPointsRules() {
        List<PointsRule> rules = pointsService.getPointsRules();
        return Result.success(rules);
    }

    /**
     * 获取积分商城商品列表
     */
    @GetMapping("/products")
    @Operation(summary = "获取积分商城商品列表")
    public Result<Page<PointsProduct>> getPointsProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        Page<PointsProduct> productsPage = pointsService.getPointsProducts(page, pageSize, category);
        return Result.success(productsPage);
    }

    /**
     * 获取积分商品详情
     */
    @GetMapping("/products/{productId}")
    @Operation(summary = "获取积分商品详情")
    public Result<PointsProduct> getPointsProductDetail(@PathVariable("productId") Long productId) {
        PointsProduct product = pointsService.getPointsProductDetail(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 签到
     */
    @PostMapping("/sign-in")
    @Operation(summary = "签到")
    public Result<Map<String, Object>> signIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> signinResult = pointsService.userSignin(user.getUserId());
        return Result.success(signinResult, "签到成功");
    }

    /**
     * 检查今日是否已签到 (现在改为处理 /sign-in-status)
     */
    @GetMapping("/sign-in-status")
    @Operation(summary = "获取用户签到状态及基本积分信息")
    public Result<Map<String, Object>> getSignInStatusEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> signInStatus = pointsService.getSignInStatus(user.getUserId());
        return Result.success(signInStatus);
    }

    /**
     * 积分兑换商品
     */
    @PostMapping("/exchange")
    @Operation(summary = "积分兑换商品")
    public Result<Void> exchangeProduct(
            @RequestBody PointsExchange exchange) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 设置用户ID
            exchange.setUserId(user.getUserId());
            
            // 调用兑换服务
            pointsExchangeService.createExchange(exchange);
            return Result.success(null, "兑换成功");
        } catch (Exception e) {
            return Result.error("兑换失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户兑换记录
     */
    @GetMapping("/exchanges")
    @Operation(summary = "获取用户兑换记录")
    public Result<Page<Map<String, Object>>> getUserExchanges(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<Map<String, Object>> exchangesPage = pointsExchangeService
            .getUserExchangesWithProduct(user.getUserId(), page, pageSize, status);
        return Result.success(exchangesPage);
    }

    /**
     * 获取兑换详情
     */
    @GetMapping("/exchanges/{id}")
    @Operation(summary = "获取兑换详情")
    public Result<Map<String, Object>> getExchangeDetail(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        Map<String, Object> detail = pointsExchangeService.getExchangeDetailWithInfo(id);
        if (detail == null) {
            return Result.error("兑换记录不存在");
        }
        
        return Result.success(detail);
    }

    /**
     * 获取用户兑换统计
     */
    @GetMapping("/exchanges/stats")
    @Operation(summary = "获取用户兑换统计")
    public Result<Map<String, Object>> getUserExchangeStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> stats = pointsExchangeService.getUserExchangeStats(user.getUserId());
        return Result.success(stats);
    }

    /**
     * 获取签到日历
     */
    @GetMapping("/sign-in-calendar")
    @Operation(summary = "获取签到日历")
    public Result<Map<String, Object>> getSignInCalendar(@RequestParam(required = false) String month) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> calendar = pointsService.getSignInCalendar(user.getUserId(), month);
        return Result.success(calendar);
    }

    /**
     * 获取用户兑换记录（带商品信息）
     */
    @GetMapping("/user-exchanges")
    @Operation(summary = "获取用户兑换记录（带商品信息）")
    public Result<Page<Map<String, Object>>> getUserExchangesWithProduct(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<Map<String, Object>> exchangesPage = pointsExchangeService
            .getUserExchangesWithProduct(user.getUserId(), page, pageSize, status);
        return Result.success(exchangesPage);
    }

    /**
     * 取消积分兑换
     */
    @PostMapping("/exchanges/{id}/cancel")
    @Operation(summary = "取消积分兑换")
    public Result<Void> cancelExchange(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            pointsExchangeService.cancelExchange(id, user.getUserId());
            return Result.success(null, "取消兑换成功，积分已退回");
        } catch (Exception e) {
            return Result.error("取消兑换失败：" + e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/exchanges/{id}/confirm")
    @Operation(summary = "确认收货")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            pointsExchangeService.confirmReceive(id, user.getUserId());
            return Result.success(null, "确认收货成功");
        } catch (Exception e) {
            return Result.error("确认收货失败：" + e.getMessage());
        }
    }
}