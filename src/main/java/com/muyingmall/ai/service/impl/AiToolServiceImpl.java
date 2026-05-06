package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.ai.dto.AiOrderQueryRequest;
import com.muyingmall.ai.dto.AiProductSearchRequest;
import com.muyingmall.ai.dto.AiRefundEvaluateRequest;
import com.muyingmall.ai.dto.AiTicketCreateRequest;
import com.muyingmall.ai.entity.AiSupportTicket;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.service.AiSupportTicketService;
import com.muyingmall.ai.service.AiToolService;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.Product;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.ParentingTipService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 业务工具实现。
 */
@Service
@RequiredArgsConstructor
public class AiToolServiceImpl implements AiToolService {

    private final ProductService productService;
    private final OrderService orderService;
    private final RefundService refundService;
    private final ParentingTipService parentingTipService;
    private final AiSupportTicketService aiSupportTicketService;

    @Override
    public Page<Product> searchProducts(AiProductSearchRequest request) {
        int limit = request.getLimit() == null ? 6 : Math.min(Math.max(request.getLimit(), 1), 20);
        Page<Product> page = productService.getProductPage(
                1,
                limit,
                request.getCategoryId(),
                request.getBrandId(),
                null,
                null,
                null,
                request.getKeyword());

        List<Product> filtered = page.getRecords().stream()
                .filter(product -> product.getPriceNew() == null
                        || request.getMinPrice() == null
                        || product.getPriceNew().compareTo(request.getMinPrice()) >= 0)
                .filter(product -> product.getPriceNew() == null
                        || request.getMaxPrice() == null
                        || product.getPriceNew().compareTo(request.getMaxPrice()) <= 0)
                .toList();
        page.setRecords(filtered);
        page.setTotal(filtered.size());
        return page;
    }

    @Override
    public Product getProductDetail(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        Product product = productService.getProductDetailWithProtection(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        return product;
    }

    @Override
    public Order getOrderStatus(Integer userId, AiOrderQueryRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未认证");
        }
        if (request.getOrderId() != null) {
            return orderService.getOrderDetail(request.getOrderId(), userId);
        }
        if (StringUtils.hasText(request.getOrderNo())) {
            return orderService.getOrderByOrderNo(request.getOrderNo(), userId);
        }
        throw new IllegalArgumentException("请提供订单ID或订单编号");
    }

    @Override
    public Map<String, Object> evaluateRefund(Integer userId, AiRefundEvaluateRequest request) {
        Order order = resolveOrder(userId, request);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderNo", order.getOrderNo());
        result.put("orderStatus", order.getStatus() != null ? order.getStatus().getCode() : null);
        result.put("maxRefundAmount", order.getActualAmount());
        result.put("requestedAmount", request.getAmount());

        OrderStatus status = order.getStatus();
        if (status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED) {
            result.put("eligible", false);
            result.put("riskLevel", AiRiskLevel.LOW.getCode());
            result.put("humanApprovalRequired", false);
            result.put("decision", "当前订单状态不支持退款，可引导用户查看订单状态或联系客服。");
            return result;
        }

        BigDecimal requestedAmount = request.getAmount() != null ? request.getAmount() : order.getActualAmount();
        if (requestedAmount != null && order.getActualAmount() != null && requestedAmount.compareTo(order.getActualAmount()) > 0) {
            result.put("eligible", false);
            result.put("riskLevel", AiRiskLevel.MEDIUM.getCode());
            result.put("humanApprovalRequired", true);
            result.put("decision", "申请金额超过订单实付金额，需要人工核验。");
            return result;
        }

        Page<Refund> refundPage = refundService.getOrderRefunds(order.getOrderId(), 1, 10);
        boolean hasPendingRefund = refundPage.getRecords().stream()
                .anyMatch(refund -> RefundStatus.PENDING.getCode().equals(refund.getStatus())
                        || RefundStatus.APPROVED.getCode().equals(refund.getStatus())
                        || RefundStatus.PROCESSING.getCode().equals(refund.getStatus()));
        if (hasPendingRefund) {
            result.put("eligible", false);
            result.put("riskLevel", AiRiskLevel.MEDIUM.getCode());
            result.put("humanApprovalRequired", true);
            result.put("decision", "该订单已有未完成的退款申请，需要转人工继续处理。");
            return result;
        }

        boolean highRiskReason = containsHighRiskReason(request.getReason());
        result.put("eligible", true);
        result.put("riskLevel", highRiskReason ? AiRiskLevel.HIGH.getCode() : AiRiskLevel.MEDIUM.getCode());
        result.put("humanApprovalRequired", true);
        result.put("decision", highRiskReason
                ? "涉及过敏、质量或医疗相关表述，Agent 只创建工单，不直接执行退款。"
                : "订单具备申请售后的基础条件，但退款动作仍需用户确认和人工审核。");
        return result;
    }

    @Override
    public List<ParentingTip> searchKnowledge(String keyword, Integer limit) {
        int pageSize = limit == null ? 5 : Math.min(Math.max(limit, 1), 10);
        return parentingTipService.getPage(1, pageSize, null, keyword).getRecords();
    }

    @Override
    public AiSupportTicket createTicket(Integer userId, AiTicketCreateRequest request) {
        return aiSupportTicketService.createTicket(userId, request);
    }

    private Order resolveOrder(Integer userId, AiRefundEvaluateRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未认证");
        }
        Order order;
        if (request.getOrderId() != null) {
            order = orderService.getOrderDetail(request.getOrderId(), userId);
        } else if (StringUtils.hasText(request.getOrderNo())) {
            order = orderService.getOrderByOrderNo(request.getOrderNo(), userId);
        } else {
            throw new IllegalArgumentException("请提供订单ID或订单编号");
        }
        if (order == null) {
            throw new IllegalArgumentException("订单不存在或无权访问");
        }
        return order;
    }

    private boolean containsHighRiskReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return false;
        }
        String text = reason.toLowerCase();
        return text.contains("过敏")
                || text.contains("湿疹")
                || text.contains("质量")
                || text.contains("奶粉")
                || text.contains("投诉")
                || text.contains("医疗")
                || text.contains("医生");
    }
}
