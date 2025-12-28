package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.config.AMapConfig;
import com.muyingmall.entity.Address;
import com.muyingmall.entity.ShippingRule;
import com.muyingmall.mapper.ShippingRuleMapper;
import com.muyingmall.service.AddressService;
import com.muyingmall.service.AMapService;
import com.muyingmall.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费计算服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final AddressService addressService;
    private final AMapService amapService;
    private final AMapConfig amapConfig;
    private final ShippingRuleMapper shippingRuleMapper;

    @Override
    public BigDecimal calculateShippingFee(Integer addressId, BigDecimal orderAmount) {
        // 获取地址信息
        Address address = addressService.getById(addressId);
        if (address == null) {
            log.warn("地址不存在: addressId={}", addressId);
            return BigDecimal.ZERO;
        }

        // 检查地址是否有经纬度信息
        if (address.getLongitude() == null || address.getLatitude() == null) {
            log.warn("地址缺少经纬度信息，无法计算运费: addressId={}", addressId);
            return BigDecimal.ZERO;
        }

        // 计算距离
        Double distance = amapService.calculateDistanceFromWarehouse(
                address.getLongitude(),
                address.getLatitude()
        );

        if (distance == null) {
            log.error("距离计算失败: addressId={}", addressId);
            return BigDecimal.ZERO;
        }

        log.info("配送距离计算成功: addressId={}, distance={}km", addressId, distance);
        return calculateShippingFeeByDistance(distance, orderAmount);
    }

    @Override
    public BigDecimal calculateShippingFeeByDistance(Double distance, BigDecimal orderAmount) {
        // 查询所有启用的运费规则，按优先级排序
        LambdaQueryWrapper<ShippingRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShippingRule::getIsActive, 1)
                .orderByDesc(ShippingRule::getPriority);

        List<ShippingRule> rules = shippingRuleMapper.selectList(queryWrapper);

        if (rules == null || rules.isEmpty()) {
            log.warn("未配置运费规则，返回默认运费0");
            return BigDecimal.ZERO;
        }

        // 匹配运费规则
        for (ShippingRule rule : rules) {
            BigDecimal minDist = rule.getMinDistance();
            BigDecimal maxDist = rule.getMaxDistance();

            // 检查距离是否在规则范围内
            boolean inRange = distance >= minDist.doubleValue() &&
                    (maxDist == null || distance < maxDist.doubleValue());

            if (inRange) {
                // 检查是否满足免运费条件
                if (rule.getFreeThreshold() != null &&
                        orderAmount.compareTo(rule.getFreeThreshold()) >= 0) {
                    log.info("订单金额满足免运费条件: orderAmount={}, threshold={}",
                            orderAmount, rule.getFreeThreshold());
                    return BigDecimal.ZERO;
                }

                // 计算运费：基础运费 + 距离 * 每公里加价
                BigDecimal baseFee = rule.getBaseFee();
                BigDecimal perKmFee = rule.getPerKmFee();
                BigDecimal distanceFee = perKmFee.multiply(BigDecimal.valueOf(distance));
                BigDecimal totalFee = baseFee.add(distanceFee);

                log.info("运费计算完成: rule={}, distance={}km, baseFee={}, perKmFee={}, totalFee={}",
                        rule.getRuleName(), distance, baseFee, perKmFee, totalFee);

                return totalFee;
            }
        }

        log.warn("未找到匹配的运费规则: distance={}km", distance);
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isDeliverable(Integer addressId) {
        // 获取地址信息
        Address address = addressService.getById(addressId);
        if (address == null || address.getLongitude() == null || address.getLatitude() == null) {
            return false;
        }

        // 计算距离
        Double distance = amapService.calculateDistanceFromWarehouse(
                address.getLongitude(),
                address.getLatitude()
        );

        if (distance == null) {
            return false;
        }

        // 判断是否在配送范围内
        boolean deliverable = distance <= amapConfig.getDelivery().getMaxDistance();
        log.info("配送范围判断: addressId={}, distance={}km, maxDistance={}km, deliverable={}",
                addressId, distance, amapConfig.getDelivery().getMaxDistance(), deliverable);

        return deliverable;
    }
}
