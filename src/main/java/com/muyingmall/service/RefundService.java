package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Refund;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 退款服务接口
 */
public interface RefundService extends IService<Refund> {

        /**
         * 申请退款
         *
         * @param orderId            订单ID
         * @param userId             用户ID
         * @param amount             退款金额
         * @param refundReason       退款原因
         * @param refundReasonDetail 退款原因详情
         * @param evidenceImages     凭证图片
         * @return 退款申请ID
         */
        Long applyRefund(Integer orderId, Integer userId, BigDecimal amount, String refundReason,
                        String refundReasonDetail, String evidenceImages);

        /**
         * 审核退款申请
         *
         * @param refundId     退款ID
         * @param approved     是否批准
         * @param rejectReason 拒绝原因
         * @param adminId      管理员ID
         * @param adminName    管理员姓名
         * @return 是否成功
         */
        boolean reviewRefund(Long refundId, boolean approved, String rejectReason, Integer adminId, String adminName);

        /**
         * 处理退款
         *
         * @param refundId      退款ID
         * @param refundChannel 退款渠道
         * @param refundAccount 退款账户
         * @param adminId       管理员ID
         * @param adminName     管理员姓名
         * @return 是否成功
         */
        boolean processRefund(Long refundId, String refundChannel, String refundAccount, Integer adminId,
                        String adminName);

        /**
         * 完成退款
         *
         * @param refundId      退款ID
         * @param transactionId 交易号
         * @param adminId       管理员ID
         * @param adminName     管理员姓名
         * @return 是否成功
         */
        boolean completeRefund(Long refundId, String transactionId, Integer adminId, String adminName);

        /**
         * 退款失败
         *
         * @param refundId  退款ID
         * @param reason    失败原因
         * @param adminId   管理员ID
         * @param adminName 管理员姓名
         * @return 是否成功
         */
        boolean failRefund(Long refundId, String reason, Integer adminId, String adminName);

        /**
         * 取消退款申请
         *
         * @param refundId 退款ID
         * @param userId   用户ID
         * @param reason   取消原因
         * @return 是否成功
         */
        boolean cancelRefund(Long refundId, Integer userId, String reason);

        /**
         * 获取退款详情
         *
         * @param refundId 退款ID
         * @return 退款详情
         */
        Refund getRefundDetail(Long refundId);

        /**
         * 获取用户的退款列表
         *
         * @param userId 用户ID
         * @param page   页码
         * @param size   每页大小
         * @return 退款分页列表
         */
        Page<Refund> getUserRefunds(Integer userId, Integer page, Integer size);

        /**
         * 获取订单的退款列表
         *
         * @param orderId 订单ID
         * @return 退款列表
         */
        Page<Refund> getOrderRefunds(Integer orderId, Integer page, Integer size);

        /**
         * 管理员获取退款列表
         *
         * @param page      页码
         * @param size      每页大小
         * @param status    状态
         * @param userId    用户ID
         * @param orderId   订单ID
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 退款分页列表
         */
        Page<Refund> adminGetRefunds(Integer page, Integer size, String status, Integer userId,
                        Integer orderId, String startTime, String endTime);

        /**
         * 获取待处理的退款数量
         *
         * @return 待处理退款数量
         */
        long getPendingRefundCount();

        /**
         * 获取退款统计数据
         *
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 统计数据
         */
        Map<String, Object> getRefundStatistics(String startTime, String endTime);

        /**
         * 根据退款单号查询退款记录
         *
         * @param refundNo 退款单号
         * @return 退款记录，如果不存在返回null
         */
        Refund getRefundByRefundNo(String refundNo);
}