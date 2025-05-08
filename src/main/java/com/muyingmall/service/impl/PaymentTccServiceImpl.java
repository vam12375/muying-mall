package com.muyingmall.service.impl;

import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.lock.DistributedLock;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.PaymentStateLogService;
import com.muyingmall.service.PaymentTccService;
import com.muyingmall.statemachine.PaymentEvent;
import com.muyingmall.statemachine.PaymentStateContext;
import com.muyingmall.tcc.TccAction;
import com.muyingmall.tcc.TccTransaction;
import com.muyingmall.tcc.TccTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 支付TCC服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTccServiceImpl implements PaymentTccService {

    private final PaymentService paymentService;
    private final PaymentStateLogService paymentStateLogService;
    private final TccTransactionManager tccTransactionManager;
    private final DistributedLock distributedLock;

    private static final String PAYMENT_LOCK_KEY_PREFIX = "payment:lock:";
    private static final long LOCK_EXPIRE_TIME = 10000; // 10秒

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPaymentWithTcc(Payment payment) {
        // 开始TCC事务
        String transactionId = tccTransactionManager.begin("createPayment", payment.getPaymentNo(), payment);

        try {
            // 执行Try阶段
            Payment result = tccTransactionManager.tryAction(transactionId, this, payment);

            // 执行Confirm阶段
            tccTransactionManager.confirmAction(transactionId, this, result);

            return result;
        } catch (Exception e) {
            // 执行Cancel阶段
            tccTransactionManager.cancelAction(transactionId, this, payment);
            throw new BusinessException("创建支付失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment processPaymentWithTcc(String paymentNo, String transactionId) {
        // 查询支付信息
        Payment payment = paymentService.getByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException("支付不存在：" + paymentNo);
        }

        // 设置第三方交易号
        payment.setTransactionId(transactionId);

        // 开始TCC事务
        String tccTransactionId = tccTransactionManager.begin("processPayment", paymentNo, payment);

        try {
            // 执行Try阶段
            Payment result = tccTransactionManager.tryAction(tccTransactionId, this, payment);

            // 执行Confirm阶段
            tccTransactionManager.confirmAction(tccTransactionId, this, result);

            return result;
        } catch (Exception e) {
            // 执行Cancel阶段
            tccTransactionManager.cancelAction(tccTransactionId, this, payment);
            throw new BusinessException("处理支付失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment closePaymentWithTcc(String paymentNo) {
        // 查询支付信息
        Payment payment = paymentService.getByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException("支付不存在：" + paymentNo);
        }

        // 开始TCC事务
        String transactionId = tccTransactionManager.begin("closePayment", paymentNo, payment);

        try {
            // 执行Try阶段
            Payment result = tccTransactionManager.tryAction(transactionId, this, payment);

            // 执行Confirm阶段
            tccTransactionManager.confirmAction(transactionId, this, result);

            return result;
        } catch (Exception e) {
            // 执行Cancel阶段
            tccTransactionManager.cancelAction(transactionId, this, payment);
            throw new BusinessException("关闭支付失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment tryAction(Payment payment) {
        String lockKey = PAYMENT_LOCK_KEY_PREFIX + payment.getPaymentNo();
        String requestId = UUID.randomUUID().toString();

        try {
            // 获取分布式锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new BusinessException("获取支付锁失败：" + payment.getPaymentNo());
            }

            // 检查支付状态
            Payment existingPayment = null;

            if (payment.getId() != null) {
                existingPayment = paymentService.getById(payment.getId());
            } else if (payment.getPaymentNo() != null) {
                existingPayment = paymentService.getByPaymentNo(payment.getPaymentNo());
            }

            if (existingPayment != null) {
                // 支付已存在，检查状态
                if (Objects.equals("createPayment", getTransactionType(payment))) {
                    throw new BusinessException("支付已存在：" + payment.getPaymentNo());
                }

                // 根据事务类型执行不同的操作
                if (Objects.equals("processPayment", getTransactionType(payment))) {
                    // 处理支付，预留资源
                    checkPaymentStatus(existingPayment, PaymentStatus.PENDING, PaymentStatus.PROCESSING);

                    // 更新为处理中状态
                    PaymentStateContext context = PaymentStateContext.of(existingPayment);
                    context.setEvent(PaymentEvent.PROCESS);
                    context.setOperator("system");
                    context.setReason("支付处理中");
                    context.setTransactionId(payment.getTransactionId());

                    // 记录原始值，用于回滚
                    payment.setStatus(existingPayment.getStatus());
                    payment.setVersion(existingPayment.getVersion());

                    // 更新状态
                    existingPayment.setStatus(PaymentStatus.PROCESSING);
                    existingPayment.setUpdateTime(LocalDateTime.now());
                    boolean updated = paymentService.updateById(existingPayment);
                    if (!updated) {
                        throw new BusinessException("更新支付状态失败，可能已被其他操作修改");
                    }

                    // 记录状态变更日志
                    paymentStateLogService.recordStateChange(context);

                    return existingPayment;
                } else if (Objects.equals("closePayment", getTransactionType(payment))) {
                    // 关闭支付，预留资源
                    checkPaymentStatus(existingPayment, PaymentStatus.PENDING, PaymentStatus.PROCESSING);

                    // 更新为关闭状态
                    PaymentStateContext context = PaymentStateContext.of(existingPayment);
                    context.setEvent(PaymentEvent.CLOSE);
                    context.setOperator("system");
                    context.setReason("支付关闭");

                    // 记录原始值，用于回滚
                    payment.setStatus(existingPayment.getStatus());
                    payment.setVersion(existingPayment.getVersion());

                    // 更新状态
                    existingPayment.setStatus(PaymentStatus.CLOSED);
                    existingPayment.setUpdateTime(LocalDateTime.now());
                    boolean updated = paymentService.updateById(existingPayment);
                    if (!updated) {
                        throw new BusinessException("更新支付状态失败，可能已被其他操作修改");
                    }

                    // 记录状态变更日志
                    paymentStateLogService.recordStateChange(context);

                    return existingPayment;
                }
            } else if (Objects.equals("createPayment", getTransactionType(payment))) {
                // 创建新支付
                paymentService.save(payment);
                return payment;
            }

            throw new BusinessException("无效的事务类型：" + getTransactionType(payment));
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAction(Payment payment) {
        String lockKey = PAYMENT_LOCK_KEY_PREFIX + payment.getPaymentNo();
        String requestId = UUID.randomUUID().toString();

        try {
            // 获取分布式锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new BusinessException("获取支付锁失败：" + payment.getPaymentNo());
            }

            // 根据事务类型执行不同的操作
            if (Objects.equals("createPayment", getTransactionType(payment))) {
                // 创建支付确认操作，实际上不需要做什么，因为在try阶段已经创建了
                log.info("支付创建确认成功：{}", payment.getPaymentNo());
            } else if (Objects.equals("processPayment", getTransactionType(payment))) {
                // 处理支付确认操作，更新为支付成功
                Payment existingPayment = paymentService.getByPaymentNo(payment.getPaymentNo());
                if (existingPayment == null) {
                    throw new BusinessException("支付不存在：" + payment.getPaymentNo());
                }

                // 只有处理中状态才能确认为成功
                if (existingPayment.getStatus() != PaymentStatus.PROCESSING) {
                    throw new BusinessException("支付状态错误：" + existingPayment.getStatus());
                }

                // 更新为成功状态
                PaymentStateContext context = PaymentStateContext.of(existingPayment);
                context.setEvent(PaymentEvent.SUCCESS);
                context.setOperator("system");
                context.setReason("支付成功");
                context.setTransactionId(payment.getTransactionId());

                // 更新状态
                existingPayment.setStatus(PaymentStatus.SUCCESS);
                existingPayment.setPaymentTime(LocalDateTime.now());
                existingPayment.setTransactionId(payment.getTransactionId());
                existingPayment.setUpdateTime(LocalDateTime.now());
                boolean updated = paymentService.updateById(existingPayment);
                if (!updated) {
                    throw new BusinessException("更新支付状态失败，可能已被其他操作修改");
                }

                // 记录状态变更日志
                paymentStateLogService.recordStateChange(context);

                log.info("支付处理确认成功：{}", payment.getPaymentNo());
            } else if (Objects.equals("closePayment", getTransactionType(payment))) {
                // 关闭支付确认操作，状态已经在try阶段更新为关闭
                log.info("支付关闭确认成功：{}", payment.getPaymentNo());
            }
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAction(Payment payment) {
        String lockKey = PAYMENT_LOCK_KEY_PREFIX + payment.getPaymentNo();
        String requestId = UUID.randomUUID().toString();

        try {
            // 获取分布式锁
            boolean locked = distributedLock.tryLock(lockKey, requestId, LOCK_EXPIRE_TIME);
            if (!locked) {
                throw new BusinessException("获取支付锁失败：" + payment.getPaymentNo());
            }

            // 根据事务类型执行不同的操作
            if (Objects.equals("createPayment", getTransactionType(payment))) {
                // 创建支付取消操作，删除支付记录
                Payment existingPayment = paymentService.getByPaymentNo(payment.getPaymentNo());
                if (existingPayment != null) {
                    paymentService.removeById(existingPayment.getId());
                    log.info("支付创建取消成功：{}", payment.getPaymentNo());
                }
            } else if (Objects.equals("processPayment", getTransactionType(payment))
                    || Objects.equals("closePayment", getTransactionType(payment))) {
                // 处理支付或关闭支付取消操作，恢复原状态
                Payment existingPayment = paymentService.getByPaymentNo(payment.getPaymentNo());
                if (existingPayment == null) {
                    return;
                }

                // 恢复原状态
                PaymentStateContext context = PaymentStateContext.of(existingPayment);
                context.setEvent(PaymentEvent.FAIL); // 使用FAIL事件记录失败
                context.setOperator("system");
                context.setReason("支付操作取消");

                // 恢复原始状态
                existingPayment.setStatus(payment.getStatus());
                existingPayment.setVersion(payment.getVersion());
                existingPayment.setUpdateTime(LocalDateTime.now());
                boolean updated = paymentService.updateById(existingPayment);
                if (!updated) {
                    throw new BusinessException("恢复支付状态失败，可能已被其他操作修改");
                }

                // 记录状态变更日志
                paymentStateLogService.recordStateChange(context);

                log.info("支付操作取消成功：{}", payment.getPaymentNo());
            }
        } finally {
            // 释放锁
            distributedLock.releaseLock(lockKey, requestId);
        }
    }

    /**
     * 检查支付状态
     *
     * @param payment         支付
     * @param allowedStatuses 允许的状态
     */
    private void checkPaymentStatus(Payment payment, PaymentStatus... allowedStatuses) {
        for (PaymentStatus status : allowedStatuses) {
            if (payment.getStatus() == status) {
                return;
            }
        }

        throw new BusinessException("支付状态错误：" + payment.getStatus() + "，不允许执行当前操作");
    }

    /**
     * 获取事务类型
     *
     * @param payment 支付
     * @return 事务类型
     */
    private String getTransactionType(Payment payment) {
        TccTransaction transaction = tccTransactionManager.getTransaction(payment.getTransactionId());
        return transaction != null ? transaction.getTransactionType() : null;
    }
}