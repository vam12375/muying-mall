package com.muyingmall.controller.common;

import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsTrack;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.LogisticsTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台物流接口控制器
 */
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台物流接口", description = "前台物流查询相关接口")
public class LogisticsController {

    private final LogisticsService logisticsService;
    private final LogisticsTrackService logisticsTrackService;

    /**
     * 根据订单ID获取物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单ID获取物流信息")
    public CommonResult<Logistics> getLogisticsByOrderId(@PathVariable("orderId") Integer orderId) {
        try {
            Logistics logistics = logisticsService.getLogisticsByOrderId(orderId);
            if (logistics == null) {
                return CommonResult.failed("订单物流信息不存在");
            }

            // 填充轨迹信息
            List<LogisticsTrack> tracks = logisticsTrackService.getTracksByLogisticsId(logistics.getId());
            logistics.setTracks(tracks);

            return CommonResult.success(logistics);
        } catch (Exception e) {
            log.error("获取订单物流信息失败", e);
            return CommonResult.failed("获取订单物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据物流单号和物流公司查询物流信息
     *
     * @param trackingNo  物流单号
     * @param companyCode 物流公司代码
     * @return 物流信息
     */
    @GetMapping("/query")
    @Operation(summary = "根据物流单号和物流公司查询物流信息")
    public CommonResult<Logistics> queryLogisticsInfo(
            @RequestParam("trackingNo") String trackingNo,
            @RequestParam("companyCode") String companyCode) {
        try {
            Logistics logistics = logisticsService.getLogisticsByTrackingNo(trackingNo);
            if (logistics == null) {
                return CommonResult.failed("物流信息不存在");
            }

            // 填充轨迹信息
            List<LogisticsTrack> tracks = logisticsTrackService.getTracksByLogisticsId(logistics.getId());
            logistics.setTracks(tracks);

            return CommonResult.success(logistics);
        } catch (Exception e) {
            log.error("查询物流信息失败", e);
            return CommonResult.failed("查询物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流轨迹
     *
     * @param logisticsId 物流ID
     * @return 物流轨迹列表
     */
    @GetMapping("/{logisticsId}/tracks")
    @Operation(summary = "获取物流轨迹")
    public CommonResult<List<LogisticsTrack>> getLogisticsTracks(@PathVariable("logisticsId") Long logisticsId) {
        try {
            List<LogisticsTrack> tracks = logisticsTrackService.getTracksByLogisticsId(logisticsId);
            return CommonResult.success(tracks);
        } catch (Exception e) {
            log.error("获取物流轨迹失败", e);
            return CommonResult.failed("获取物流轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 重新生成物流轨迹（管理员接口）
     * 用于修复坐标为空或需要更新的物流轨迹
     *
     * @param logisticsId 物流ID
     * @return 操作结果
     */
    @PostMapping("/admin/{logisticsId}/regenerate-tracks")
    @Operation(summary = "重新生成物流轨迹", description = "删除旧轨迹并基于真实路径重新生成")
    public CommonResult<String> regenerateTracks(@PathVariable("logisticsId") Long logisticsId) {
        try {
            log.info("【管理员操作】开始重新生成物流轨迹: logisticsId={}", logisticsId);
            
            // 1. 获取物流信息
            Logistics logistics = logisticsService.getLogisticsById(logisticsId);
            if (logistics == null) {
                return CommonResult.failed("物流记录不存在");
            }
            
            // 2. 检查收货地坐标
            if (logistics.getReceiverLongitude() == null || logistics.getReceiverLatitude() == null) {
                return CommonResult.failed("收货地坐标为空，无法生成轨迹");
            }
            
            // 3. 删除旧轨迹（保留初始轨迹）
            List<LogisticsTrack> oldTracks = logisticsTrackService.getTracksByLogisticsId(logisticsId);
            if (oldTracks != null && oldTracks.size() > 1) {
                // 只保留第一条（初始轨迹），删除其他
                for (int i = 1; i < oldTracks.size(); i++) {
                    logisticsTrackService.removeById(oldTracks.get(i).getId());
                }
                log.info("已删除旧轨迹: logisticsId={}, 删除数量={}", logisticsId, oldTracks.size() - 1);
            }
            
            // 4. 重新生成基于真实路径的轨迹
            boolean success = logisticsService.generateRouteBasedTracks(
                    logisticsId,
                    logistics.getReceiverLongitude(),
                    logistics.getReceiverLatitude()
            );
            
            if (success) {
                log.info("【管理员操作】物流轨迹重新生成成功: logisticsId={}", logisticsId);
                return CommonResult.success("物流轨迹重新生成成功");
            } else {
                log.warn("【管理员操作】路径规划失败，使用标准轨迹: logisticsId={}", logisticsId);
                // 降级方案：使用标准轨迹
                boolean fallbackSuccess = logisticsService.generateStandardTracks(logisticsId, "管理员");
                if (fallbackSuccess) {
                    return CommonResult.success("路径规划失败，已使用标准轨迹");
                } else {
                    return CommonResult.failed("轨迹生成失败");
                }
            }
        } catch (Exception e) {
            log.error("【管理员操作】重新生成物流轨迹失败: logisticsId={}", logisticsId, e);
            return CommonResult.failed("重新生成物流轨迹失败: " + e.getMessage());
        }
    }
}