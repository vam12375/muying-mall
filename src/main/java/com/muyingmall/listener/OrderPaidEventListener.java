package com.muyingmall.listener;

import com.muyingmall.entity.Address;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.Order;
import com.muyingmall.enums.LogisticsStatus;
import com.muyingmall.event.OrderPaidEvent;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.AMapService;
import com.muyingmall.service.AddressService;
import com.muyingmall.config.AMapConfig;
import com.muyingmall.dto.amap.GeoCodeResponse;
import com.muyingmall.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 订单支付事件监听器
 * 负责在订单支付后创建物流记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final LogisticsService logisticsService;
    private final AMapConfig amapConfig;
    private final AMapService amapService;
    private final AddressService addressService;
    private final RedisUtil redisUtil;
    
    /** 物流创建分布式锁前缀 */
    private static final String LOGISTICS_CREATE_LOCK_PREFIX = "logistics:create:lock:order:";

    /**
     * 处理订单支付事件，创建物流记录
     * 注意：使用 @Async 异步执行，@Transactional 在异步方法中需要新事务
     */
    @Async
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPaid(OrderPaidEvent event) {
        Order order = event.getOrder();
        Address address = event.getAddress();
        String lockKey = LOGISTICS_CREATE_LOCK_PREFIX + order.getOrderId();
        
        log.info("【物流创建】接收到订单支付事件: orderId={}, addressId={}, hasCoords={}", 
                order.getOrderId(), address.getAddressId(), address.getLongitude() != null);

        // 使用分布式锁防止并发创建
        boolean lockAcquired = false;
        try {
            // 尝试获取锁，最多等待3秒，锁过期时间30秒
            lockAcquired = redisUtil.tryLock(lockKey, "LOGISTICS_CREATE", 3, 30, TimeUnit.SECONDS);
            
            if (!lockAcquired) {
                log.warn("【物流创建】获取分布式锁失败，可能正在被其他线程处理: orderId={}", order.getOrderId());
                return;
            }
            
            log.info("【物流创建】成功获取分布式锁: orderId={}", order.getOrderId());

            // 双重检查：获取锁后再次检查是否已存在物流记录
            Logistics existingLogistics = logisticsService.getLogisticsByOrderId(order.getOrderId());
            if (existingLogistics != null) {
                log.warn("【物流创建】订单已存在物流记录，跳过创建: orderId={}, logisticsId={}", 
                        order.getOrderId(), existingLogistics.getId());
                return;
            }

            // 1. 检查并补全收货地址坐标
            if (address.getLongitude() == null || address.getLatitude() == null) {
                log.info("【坐标补全】收货地址缺少坐标，开始自动获取: addressId={}", address.getAddressId());
                boolean coordsUpdated = updateAddressCoordinates(address);
                if (!coordsUpdated) {
                    log.error("【坐标补全失败】无法获取收货地址坐标，跳过物流创建: addressId={}", address.getAddressId());
                    return;
                }
                log.info("【坐标补全成功】addressId={}, lng={}, lat={}", 
                        address.getAddressId(), address.getLongitude(), address.getLatitude());
            } else {
                log.info("【坐标检查】地址已有坐标: lng={}, lat={}", address.getLongitude(), address.getLatitude());
            }

            // 2. 创建物流记录
            Logistics logistics = new Logistics();
            logistics.setOrderId(order.getOrderId());
            logistics.setCompanyId(1); // 默认物流公司
            logistics.setTrackingNo(generateTrackingNo());
            logistics.setStatus(LogisticsStatus.CREATED); // 使用枚举类型

            // 设置发件人信息（商家信息）
            logistics.setSenderName("母婴商城");
            logistics.setSenderPhone("400-123-4567");
            logistics.setSenderAddress("浙江省杭州市西湖区");
            // 设置发货地坐标（从配置文件读取）
            logistics.setSenderLongitude(amapConfig.getWarehouse().getLongitude());
            logistics.setSenderLatitude(amapConfig.getWarehouse().getLatitude());

            // 设置收件人信息
            logistics.setReceiverName(address.getReceiver());
            logistics.setReceiverPhone(address.getPhone());
            logistics.setReceiverAddress(address.getProvince() + address.getCity() +
                    address.getDistrict() + address.getDetail());
            // 设置收货地坐标（从用户地址获取）
            logistics.setReceiverLongitude(address.getLongitude());
            logistics.setReceiverLatitude(address.getLatitude());

            logistics.setShippingTime(LocalDateTime.now());
            logistics.setRemark("系统自动创建");

            // 3. 保存物流记录
            boolean created = logisticsService.createLogistics(logistics);

            if (!created) {
                log.error("创建物流记录失败: orderId={}", order.getOrderId());
                return;
            }

            log.info("物流记录创建成功: logisticsId={}, trackingNo={}", logistics.getId(),
                    logistics.getTrackingNo());

            // 4. 生成基于真实路径的物流轨迹（修正参数）
            boolean tracksGenerated = logisticsService.generateRouteBasedTracks(
                    logistics.getId(),
                    address.getLongitude(),
                    address.getLatitude());

            if (!tracksGenerated) {
                log.warn("物流轨迹生成失败，使用标准轨迹: logisticsId={}", logistics.getId());
                // 降级方案：使用标准轨迹模板
                logisticsService.generateStandardTracks(logistics.getId(), "系统");
            }

            log.info("订单物流处理完成: orderId={}, logisticsId={}", order.getOrderId(), logistics.getId());

        } catch (Exception e) {
            log.error("处理订单支付事件失败: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
            // 注意：这里不抛出异常，避免影响订单支付流程
        } finally {
            // 释放分布式锁
            if (lockAcquired) {
                try {
                    redisUtil.unlock(lockKey, "LOGISTICS_CREATE");
                    log.info("【物流创建】释放分布式锁: orderId={}", order.getOrderId());
                } catch (Exception e) {
                    log.error("【物流创建】释放分布式锁失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 生成物流单号
     */
    private String generateTrackingNo() {
        return "SF" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    /**
     * 构建干净的地址字符串（智能去重）
     * 策略：
     * 1. 如果detail已包含完整的省市区信息，直接使用detail
     * 2. 否则，拼接省市区 + detail，并去除重复
     *
     * @param address 地址对象
     * @return 清理后的地址字符串
     */
    private String buildCleanAddress(Address address) {
        String province = address.getProvince() != null ? address.getProvince().trim() : "";
        String city = address.getCity() != null ? address.getCity().trim() : "";
        String district = address.getDistrict() != null ? address.getDistrict().trim() : "";
        String detail = address.getDetail() != null ? address.getDetail().trim() : "";

        // 策略1：检查detail是否已经包含完整的省市区信息
        boolean detailContainsProvince = !province.isEmpty() && detail.contains(province);
        boolean detailContainsCity = !city.isEmpty() && detail.contains(city);
        boolean detailContainsDistrict = !district.isEmpty() && detail.contains(district);

        String cleanAddress;
        
        if (detailContainsProvince && detailContainsCity && detailContainsDistrict) {
            // detail已包含完整信息，直接使用
            cleanAddress = detail;
            log.info("【地址清理】detail包含完整信息，直接使用: {}", cleanAddress);
        } else if (detailContainsProvince || detailContainsCity || detailContainsDistrict) {
            // detail部分包含省市区，可能有重复，需要智能拼接
            StringBuilder sb = new StringBuilder();
            
            // 只添加detail中不包含的部分
            if (!detailContainsProvince && !province.isEmpty()) {
                sb.append(province);
            }
            if (!detailContainsCity && !city.isEmpty() && !city.equals(province)) {
                sb.append(city);
            }
            if (!detailContainsDistrict && !district.isEmpty() && !district.equals(city)) {
                sb.append(district);
            }
            
            sb.append(detail);
            cleanAddress = sb.toString();
            log.info("【地址清理】部分重复，智能拼接: province={}, city={}, district={}, detail={} → {}", 
                    province, city, district, detail, cleanAddress);
        } else {
            // detail不包含省市区，正常拼接
            StringBuilder sb = new StringBuilder();
            if (!province.isEmpty()) {
                sb.append(province);
            }
            if (!city.isEmpty() && !city.equals(province)) {
                sb.append(city);
            }
            if (!district.isEmpty() && !district.equals(city)) {
                sb.append(district);
            }
            sb.append(detail);
            cleanAddress = sb.toString();
            log.info("【地址清理】正常拼接: {}", cleanAddress);
        }
        
        return cleanAddress;
    }

    /**
     * 更新地址坐标信息
     * 如果地址缺少经纬度，调用高德地图API获取并更新数据库
     *
     * @param address 地址对象
     * @return 是否成功更新
     */
    private boolean updateAddressCoordinates(Address address) {
        try {
            // 构建完整地址字符串（去重处理）
            String fullAddress = buildCleanAddress(address);

            if (fullAddress.trim().isEmpty()) {
                log.error("【坐标补全】地址信息为空: addressId={}", address.getAddressId());
                return false;
            }

            log.info("【坐标补全】调用高德API: address={}", fullAddress);

            // 调用高德地图地理编码API
            GeoCodeResponse geoCodeResponse = amapService.geoCode(fullAddress);
            if (geoCodeResponse == null) {
                log.error("【坐标补全】API返回null: address={}", fullAddress);
                return false;
            }

            if (geoCodeResponse.getGeoCodes() == null || geoCodeResponse.getGeoCodes().isEmpty()) {
                log.error("【坐标补全】API返回空结果: status={}, info={}, address={}", 
                        geoCodeResponse.getStatus(), geoCodeResponse.getInfo(), fullAddress);
                return false;
            }

            // 获取第一个结果的坐标
            GeoCodeResponse.GeoCodes geocode = geoCodeResponse.getGeoCodes().get(0);
            String location = geocode.getLocation();
            if (location == null || !location.contains(",")) {
                log.error("【坐标补全】坐标格式错误: location={}", location);
                return false;
            }

            // 解析经纬度
            String[] coords = location.split(",");
            Double longitude = Double.parseDouble(coords[0]);
            Double latitude = Double.parseDouble(coords[1]);

            log.info("【坐标补全】解析成功: lng={}, lat={}, formatted={}", 
                    longitude, latitude, geocode.getFormattedAddress());

            // 更新地址对象
            address.setLongitude(longitude);
            address.setLatitude(latitude);
            address.setFormattedAddress(geocode.getFormattedAddress());
            address.setAdcode(geocode.getAdCode());

            // 更新数据库
            boolean updated = addressService.updateById(address);
            if (updated) {
                log.info("【坐标补全】数据库更新成功: addressId={}, lng={}, lat={}", 
                        address.getAddressId(), longitude, latitude);
            } else {
                log.error("【坐标补全】数据库更新失败: addressId={}", address.getAddressId());
            }

            return updated;
        } catch (Exception e) {
            log.error("【坐标补全】异常: addressId={}, error={}", 
                    address.getAddressId(), e.getMessage(), e);
            return false;
        }
    }
}
