package com.muyingmall.controller;

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
}