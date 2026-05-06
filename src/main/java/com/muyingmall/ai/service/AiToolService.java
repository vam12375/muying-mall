package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.ai.dto.AiOrderQueryRequest;
import com.muyingmall.ai.dto.AiProductSearchRequest;
import com.muyingmall.ai.dto.AiRefundEvaluateRequest;
import com.muyingmall.ai.dto.AiTicketCreateRequest;
import com.muyingmall.ai.entity.AiSupportTicket;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * Agent 可调用的业务工具服务。
 */
public interface AiToolService {

    Page<Product> searchProducts(AiProductSearchRequest request);

    Product getProductDetail(Integer productId);

    Order getOrderStatus(Integer userId, AiOrderQueryRequest request);

    Map<String, Object> evaluateRefund(Integer userId, AiRefundEvaluateRequest request);

    List<ParentingTip> searchKnowledge(String keyword, Integer limit);

    AiSupportTicket createTicket(Integer userId, AiTicketCreateRequest request);
}
