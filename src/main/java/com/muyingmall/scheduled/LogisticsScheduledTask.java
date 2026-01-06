package com.muyingmall.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsTrack;
import com.muyingmall.enums.LogisticsStatus;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.LogisticsTrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流定时任务
 * 用于模拟物流进度推进
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsScheduledTask {

    private final LogisticsService logisticsService;
    private final LogisticsTrackService logisticsTrackService;

    /**
     * 【场景3：物流轨迹可视化】模拟物流进度推进
     * 每2分钟执行一次，更新运输中的物流状态
     */
    //@Scheduled(cron = "0 */2 * * * ?") // 每2分钟执行一次
    @Scheduled(cron = "0 0 */6 * * *")
    public void updateLogisticsProgress() {
        log.info("开始执行物流进度推进定时任务");

        try {
            // 1. 查询所有运输中的物流记录
            LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Logistics::getStatus, LogisticsStatus.SHIPPING);
            List<Logistics> shippingLogistics = logisticsService.list(queryWrapper);

            if (shippingLogistics.isEmpty()) {
                log.info("当前没有运输中的物流记录");
                return;
            }

            log.info("找到{}条运输中的物流记录", shippingLogistics.size());

            // 2. 遍历每条物流记录，检查是否需要更新状态
            for (Logistics logistics : shippingLogistics) {
                try {
                    updateSingleLogisticsProgress(logistics);
                } catch (Exception e) {
                    log.error("更新物流进度失败: logisticsId={}", logistics.getId(), e);
                }
            }

            log.info("物流进度推进定时任务执行完成");
        } catch (Exception e) {
            log.error("物流进度推进定时任务执行异常", e);
        }
    }

    /**
     * 更新单条物流记录的进度
     *
     * @param logistics 物流记录
     */
    private void updateSingleLogisticsProgress(Logistics logistics) {
        // 1. 获取该物流的所有轨迹点
        List<LogisticsTrack> tracks = logisticsTrackService.getTracksByLogisticsId(logistics.getId());
        if (tracks == null || tracks.isEmpty()) {
            log.warn("物流记录没有轨迹点: logisticsId={}", logistics.getId());
            return;
        }

        // 2. 找出当前时间应该到达的轨迹点
        LocalDateTime now = LocalDateTime.now();
        LogisticsTrack currentTrack = null;
        
        for (LogisticsTrack track : tracks) {
            if (track.getTrackingTime() != null && track.getTrackingTime().isBefore(now)) {
                currentTrack = track;
            } else {
                break; // 找到第一个未来的轨迹点就停止
            }
        }

        if (currentTrack == null) {
            log.debug("物流尚未开始运输: logisticsId={}", logistics.getId());
            return;
        }

        // 3. 检查是否已经到达最后一个轨迹点
        LogisticsTrack lastTrack = tracks.get(tracks.size() - 1);
        if (currentTrack.getId().equals(lastTrack.getId()) && 
                lastTrack.getTrackingTime().isBefore(now)) {
            // 【增强】检查是否真正到达目的地（基于坐标距离判断）
            boolean arrivedAtDestination = checkArrivalByCoordinates(logistics, lastTrack);
            
            if (arrivedAtDestination) {
                // 已到达最后一个轨迹点，更新物流状态为已送达
                log.info("【物流签收】物流已到达目的地: logisticsId={}, trackingNo={}", 
                        logistics.getId(), logistics.getTrackingNo());
                
                logisticsService.updateLogisticsStatus(
                        logistics.getId(), 
                        LogisticsStatus.DELIVERED.getCode(), 
                        "系统自动更新：已送达"
                );
            } else {
                log.debug("物流运输中（最后轨迹点未到达目的地）: logisticsId={}", logistics.getId());
            }
        } else {
            // 仍在运输中，记录当前进度
            log.debug("物流运输中: logisticsId={}, 当前位置={}, 进度={}/{}", 
                    logistics.getId(), 
                    currentTrack.getLocation(),
                    tracks.indexOf(currentTrack) + 1,
                    tracks.size());
        }
    }

    /**
     * 【场景3：物流轨迹可视化】基于坐标距离判断是否到达目的地
     * 计算最后轨迹点与收货地址的距离，如果在100米内则认为已到达
     *
     * @param logistics  物流记录
     * @param lastTrack  最后一个轨迹点
     * @return 是否到达目的地
     */
    private boolean checkArrivalByCoordinates(Logistics logistics, LogisticsTrack lastTrack) {
        // 检查坐标是否完整
        if (logistics.getReceiverLongitude() == null || logistics.getReceiverLatitude() == null) {
            log.debug("收货地坐标为空，无法判断到达状态: logisticsId={}", logistics.getId());
            return true; // 降级：如果没有坐标，按时间判断
        }

        if (lastTrack.getLongitude() == null || lastTrack.getLatitude() == null) {
            log.debug("最后轨迹点坐标为空，无法判断到达状态: logisticsId={}", logistics.getId());
            return true; // 降级：如果没有坐标，按时间判断
        }

        // 计算距离（单位：米）
        double distance = calculateDistance(
                lastTrack.getLongitude(), lastTrack.getLatitude(),
                logistics.getReceiverLongitude(), logistics.getReceiverLatitude()
        );

        log.debug("【物流签收】距离计算: logisticsId={}, 最后轨迹点=({},{}), 目的地=({},{}), 距离={}米",
                logistics.getId(),
                lastTrack.getLongitude(), lastTrack.getLatitude(),
                logistics.getReceiverLongitude(), logistics.getReceiverLatitude(),
                String.format("%.2f", distance));

        // 如果距离在100米内，认为已到达
        final double ARRIVAL_THRESHOLD = 100.0; // 100米阈值
        return distance <= ARRIVAL_THRESHOLD;
    }

    /**
     * 计算两个坐标点之间的距离（单位：米）
     * 使用 Haversine 公式计算球面距离
     *
     * @param lon1 经度1
     * @param lat1 纬度1
     * @param lon2 经度2
     * @param lat2 纬度2
     * @return 距离（米）
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        final double EARTH_RADIUS = 6371000; // 地球半径（米）

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
