package com.muyingmall.fixtures;

import com.muyingmall.entity.Refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款测试夹具。
 */
public final class RefundFixtures {

    private RefundFixtures() {
    }

    /**
     * 构造一条待处理退款。
     */
    public static Refund pending(Long id, Integer orderId, Integer userId, BigDecimal amount) {
        Refund refund = new Refund();
        refund.setId(id);
        refund.setRefundNo("RFD" + id);
        refund.setOrderId(orderId);
        refund.setOrderNo("ORD" + orderId);
        refund.setUserId(userId);
        refund.setPaymentId((long) (orderId * 100));
        refund.setAmount(amount);
        refund.setRefundReason("质量问题");
        refund.setRefundReasonDetail("商品瑕疵");
        refund.setStatus("PENDING");
        refund.setIsDeleted(0);
        refund.setVersion(0);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        return refund;
    }

    /**
     * 构造指定状态的退款。
     */
    public static Refund withStatus(Long id, Integer orderId, Integer userId, BigDecimal amount, String status) {
        Refund refund = pending(id, orderId, userId, amount);
        refund.setStatus(status);
        return refund;
    }
}
