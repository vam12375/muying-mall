package com.muyingmall.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.ai.dto.AiOrderQueryRequest;
import com.muyingmall.ai.dto.AiProductSearchRequest;
import com.muyingmall.ai.dto.AiRefundEvaluateRequest;
import com.muyingmall.ai.dto.AiTicketCreateRequest;
import com.muyingmall.ai.dto.AiToolCallLogRequest;
import com.muyingmall.ai.entity.AiSupportTicket;
import com.muyingmall.ai.entity.AiToolCallLog;
import com.muyingmall.ai.service.AiToolCallLogService;
import com.muyingmall.ai.service.AiToolService;
import com.muyingmall.common.api.Result;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.Product;
import com.muyingmall.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * FastAPI Agent 可调用的业务工具接口。
 */
@RestController
@RequestMapping("/ai/tools")
@RequiredArgsConstructor
@Tag(name = "AI工具接口", description = "供 FastAPI Agent 调用的商品、订单、售后、知识库和工单工具")
public class AiToolController {

    private final UserContext userContext;
    private final AiToolService aiToolService;
    private final AiToolCallLogService aiToolCallLogService;

    @PostMapping("/products/search")
    @Operation(summary = "Agent 搜索商品")
    public Result<Page<Product>> searchProducts(@RequestBody AiProductSearchRequest request) {
        return Result.success(aiToolService.searchProducts(request));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Agent 查询商品详情")
    public Result<Product> getProductDetail(@PathVariable Integer productId) {
        return Result.success(aiToolService.getProductDetail(productId));
    }

    @PostMapping("/orders/status")
    @Operation(summary = "Agent 查询订单状态")
    public Result<Order> getOrderStatus(@RequestBody AiOrderQueryRequest request) {
        Integer userId = requireUserId();
        return Result.success(aiToolService.getOrderStatus(userId, request));
    }

    @PostMapping("/refunds/evaluate")
    @Operation(summary = "Agent 判断售后退款规则")
    public Result<Map<String, Object>> evaluateRefund(@RequestBody AiRefundEvaluateRequest request) {
        Integer userId = requireUserId();
        return Result.success(aiToolService.evaluateRefund(userId, request));
    }

    @GetMapping("/knowledge/search")
    @Operation(summary = "Agent 检索育儿知识")
    public Result<List<ParentingTip>> searchKnowledge(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(aiToolService.searchKnowledge(keyword, limit));
    }

    @PostMapping("/tickets")
    @Operation(summary = "Agent 创建人工工单")
    public Result<AiSupportTicket> createTicket(@RequestBody @Valid AiTicketCreateRequest request) {
        Integer userId = requireUserId();
        return Result.success(aiToolService.createTicket(userId, request), "AI工单已创建");
    }

    @PostMapping("/trace/tool-call")
    @Operation(summary = "Agent 记录工具调用日志")
    public Result<AiToolCallLog> recordToolCall(@RequestBody AiToolCallLogRequest request) {
        Integer userId = userContext.getCurrentUserId();
        return Result.success(aiToolCallLogService.recordToolCall(userId, request));
    }

    private Integer requireUserId() {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未认证");
        }
        return userId;
    }
}
