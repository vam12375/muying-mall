package com.muyingmall.listener;

import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsCompany;
import com.muyingmall.entity.Order;
import com.muyingmall.enums.LogisticsStatus;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.service.LogisticsCompanyService;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.LogisticsTrackService;
import com.muyingmall.service.impl.OrderStateServiceImpl.OrderStateChangedEvent;
import com.muyingmall.statemachine.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

/**
 * 物流事件监听器
 * 处理订单状态变更时的物流轨迹自动生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsEventListener {

    private final LogisticsService logisticsService;
    private final LogisticsTrackService logisticsTrackService;
    private final LogisticsCompanyService logisticsCompanyService;

    /**
     * 监听订单状态变更事件，处理物流相关逻辑
     * 使用TransactionalEventListener确保在主事务提交后执行
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderStateChanged(OrderStateChangedEvent event) {
        Order order = event.getOrder();
        OrderEvent orderEvent = event.getEvent();
        OrderStatus newStatus = event.getNewStatus();

        log.debug("接收到订单状态变更事件: orderId={}, event={}, newStatus={}, operator={}",
                order.getOrderId(), orderEvent, newStatus, event.getOperator());

        try {
            // 处理订单发货事件
            if (OrderEvent.SHIP.equals(orderEvent) && OrderStatus.SHIPPED.equals(newStatus)) {
                handleOrderShipped(order, event.getOperator());
            }
            // 处理订单收货事件
            else if (OrderEvent.RECEIVE.equals(orderEvent) && OrderStatus.COMPLETED.equals(newStatus)) {
                handleOrderReceived(order, event.getOperator());
            }
        } catch (Exception e) {
            log.error("处理物流事件失败: orderId={}, event={}, error={}",
                    order.getOrderId(), orderEvent, e.getMessage(), e);
        }
    }

    /**
     * 处理订单发货事件
     * 自动创建物流记录和初始轨迹
     */
    private void handleOrderShipped(Order order, String operator) {
        log.debug("处理订单发货事件: orderId={}, operator={}", order.getOrderId(), operator);

        try {
            // 检查是否已存在物流记录
            Logistics existingLogistics = logisticsService.getLogisticsByOrderId(order.getOrderId());
            if (existingLogistics != null) {
                log.debug("订单 {} 已存在物流记录，跳过创建", order.getOrderId());
                return;
            }

            // 获取物流公司信息
            LogisticsCompany company = getLogisticsCompany(order.getShippingCompany());
            if (company == null) {
                log.warn("未找到物流公司信息: {}", order.getShippingCompany());
                return;
            }

            // 创建物流记录
            Logistics logistics = createLogisticsRecord(order, company);
            boolean logisticsCreated = logisticsService.createLogistics(logistics);

            if (logisticsCreated) {
                log.debug("成功创建物流记录: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());

                // 创建初始物流轨迹
                boolean trackCreated = logisticsTrackService.createInitialTrack(logistics);
                if (trackCreated) {
                    log.debug("成功创建初始物流轨迹: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());

                    // 自动生成标准物流轨迹
                    generateStandardLogisticsTracks(logistics, operator);
                } else {
                    log.error("创建初始物流轨迹失败: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());
                }
            } else {
                log.error("创建物流记录失败: orderId={}", order.getOrderId());
            }
        } catch (Exception e) {
            log.error("处理订单发货事件异常: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 处理订单收货事件
     * 更新物流状态为已送达
     */
    private void handleOrderReceived(Order order, String operator) {
        log.debug("处理订单收货事件: orderId={}, operator={}", order.getOrderId(), operator);

        try {
            // 获取物流记录
            Logistics logistics = logisticsService.getLogisticsByOrderId(order.getOrderId());
            if (logistics == null) {
                log.warn("订单 {} 未找到物流记录", order.getOrderId());
                return;
            }

            // 更新物流状态为已送达
            logistics.setStatus(LogisticsStatus.DELIVERED);
            logistics.setDeliveryTime(order.getCompletionTime());
            boolean updated = logisticsService.updateById(logistics);

            if (updated) {
                log.debug("成功更新物流状态为已送达: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());

                // 创建送达轨迹
                boolean trackCreated = logisticsTrackService.createStatusTrack(logistics, operator, "用户确认收货");
                if (trackCreated) {
                    log.debug("成功创建送达轨迹: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());
                } else {
                    log.error("创建送达轨迹失败: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());
                }
            } else {
                log.error("更新物流状态失败: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());
            }
        } catch (Exception e) {
            log.error("处理订单收货事件异常: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 获取物流公司信息
     */
    private LogisticsCompany getLogisticsCompany(String shippingCompany) {
        if (!StringUtils.hasText(shippingCompany)) {
            // 如果没有指定物流公司，使用默认的第一个启用的物流公司
            return logisticsCompanyService.getEnabledCompanies().stream()
                    .findFirst()
                    .orElse(null);
        }

        // 根据公司名称查找
        return logisticsCompanyService.getByName(shippingCompany);
    }

    /**
     * 创建物流记录
     */
    private Logistics createLogisticsRecord(Order order, LogisticsCompany company) {
        Logistics logistics = new Logistics();
        logistics.setOrderId(order.getOrderId());
        logistics.setCompanyId(company.getId());
        logistics.setTrackingNo(order.getTrackingNo());
        logistics.setStatus(LogisticsStatus.CREATED);
        logistics.setShippingTime(order.getShippingTime());

        // 设置收货人信息
        logistics.setReceiverName(order.getReceiverName());
        logistics.setReceiverPhone(order.getReceiverPhone());

        // 拼接完整地址
        String fullAddress = (order.getReceiverProvince() != null ? order.getReceiverProvince() : "") +
                (order.getReceiverCity() != null ? order.getReceiverCity() : "") +
                (order.getReceiverDistrict() != null ? order.getReceiverDistrict() : "") +
                (order.getReceiverAddress() != null ? order.getReceiverAddress() : "");
        logistics.setReceiverAddress(fullAddress);

        return logistics;
    }

    /**
     * 自动生成标准物流轨迹
     */
    private void generateStandardLogisticsTracks(Logistics logistics, String operator) {
        log.debug("开始生成标准物流轨迹: logisticsId={}", logistics.getId());

        try {
            // 调用物流服务的自动生成轨迹方法
            boolean generated = logisticsService.generateStandardTracks(logistics.getId(), operator);
            if (generated) {
                log.debug("成功生成标准物流轨迹: logisticsId={}", logistics.getId());
            } else {
                log.warn("生成标准物流轨迹失败: logisticsId={}", logistics.getId());
            }
        } catch (Exception e) {
            log.error("生成标准物流轨迹异常: logisticsId={}, error={}", logistics.getId(), e.getMessage(), e);
        }
    }
}
