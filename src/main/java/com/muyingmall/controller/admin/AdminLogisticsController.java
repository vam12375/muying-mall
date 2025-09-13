package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台物流管理控制器
 */
@RestController
@RequestMapping("/api/admin/logistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理后台物流管理", description = "管理后台物流查询、管理相关接口")
public class AdminLogisticsController {

    private final LogisticsService logisticsService;
    private final LogisticsTrackService logisticsTrackService;

    /**
     * 分页获取物流列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param status   物流状态
     * @param keyword  搜索关键词
     * @return 物流分页列表
     */
    @GetMapping
    @Operation(summary = "分页获取物流列表")
    public CommonResult<Map<String, Object>> getLogisticsList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Page<Logistics> logisticsPage = logisticsService.getLogisticsList(page, pageSize, status, keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("list", logisticsPage.getRecords());
            result.put("total", logisticsPage.getTotal());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取物流列表失败", e);
            return CommonResult.failed("获取物流列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流详情
     *
     * @param id 物流ID
     * @return 物流详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取物流详情")
    public CommonResult<Logistics> getLogisticsDetail(@PathVariable("id") Long id) {
        try {
            log.info("获取物流详情请求，物流ID: {}", id);
            Logistics logistics = logisticsService.getLogisticsById(id);
            if (logistics == null) {
                log.warn("物流不存在，物流ID: {}", id);
                return CommonResult.failed("物流不存在");
            }
            log.info("成功获取物流详情，物流单号: {}, 状态: {}", logistics.getTrackingNo(), logistics.getStatus());
            return CommonResult.success(logistics);
        } catch (Exception e) {
            log.error("获取物流详情失败，物流ID: {}", id, e);
            return CommonResult.failed("获取物流详情失败: " + e.getMessage());
        }
    }

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
            return CommonResult.success(logistics);
        } catch (Exception e) {
            log.error("获取订单物流信息失败", e);
            return CommonResult.failed("获取订单物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流状态
     *
     * @param id     物流ID
     * @param status 物流状态
     * @param remark 备注
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新物流状态")
    public CommonResult<Boolean> updateLogisticsStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            boolean result = logisticsService.updateLogisticsStatus(id, status, remark);
            if (result) {
                return CommonResult.success(true, "更新物流状态成功");
            } else {
                return CommonResult.failed("更新物流状态失败");
            }
        } catch (Exception e) {
            log.error("更新物流状态失败", e);
            return CommonResult.failed("更新物流状态失败: " + e.getMessage());
        }
    }

    /**
     * 添加物流轨迹
     *
     * @param logisticsId 物流ID
     * @param track       轨迹信息
     * @return 添加的轨迹信息
     */
    @PostMapping("/{logisticsId}/tracks")
    @Operation(summary = "添加物流轨迹")
    public CommonResult<LogisticsTrack> addLogisticsTrack(
            @PathVariable("logisticsId") Long logisticsId,
            @RequestBody LogisticsTrack track) {
        try {
            // 设置物流ID
            track.setLogisticsId(logisticsId);
            boolean result = logisticsTrackService.addTrack(track);
            if (result) {
                // 返回添加的轨迹信息
                return CommonResult.success(track, "添加物流轨迹成功");
            } else {
                return CommonResult.failed("添加物流轨迹失败");
            }
        } catch (Exception e) {
            log.error("添加物流轨迹失败", e);
            return CommonResult.failed("添加物流轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加物流轨迹
     *
     * @param logisticsId 物流ID
     * @param tracks      轨迹列表
     * @return 最新的轨迹列表
     */
    @PostMapping("/{logisticsId}/batch-tracks")
    @Operation(summary = "批量添加物流轨迹")
    public CommonResult<List<LogisticsTrack>> batchAddLogisticsTracks(
            @PathVariable("logisticsId") Long logisticsId,
            @RequestBody List<LogisticsTrack> tracks) {
        try {
            log.info("接收到批量添加物流轨迹请求，物流ID: {}, 轨迹数量: {}", logisticsId, tracks.size());

            // 首先验证物流信息是否存在
            Logistics logistics = logisticsService.getById(logisticsId);
            if (logistics == null) {
                log.error("找不到物流信息，物流ID: {}", logisticsId);
                return CommonResult.failed("找不到该物流信息，请确认物流ID是否正确");
            }

            log.info("找到物流信息，物流单号: {}, 状态: {}", logistics.getTrackingNo(), logistics.getStatus());

            boolean result = logisticsTrackService.batchAddTracks(logisticsId, tracks);
            if (result) {
                // 返回最新的轨迹列表
                List<LogisticsTrack> latestTracks = logisticsTrackService.getTracksByLogisticsId(logisticsId);
                return CommonResult.success(latestTracks, "批量添加物流轨迹成功");
            } else {
                return CommonResult.failed("批量添加物流轨迹失败");
            }
        } catch (Exception e) {
            log.error("批量添加物流轨迹失败", e);
            return CommonResult.failed("批量添加物流轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流轨迹列表
     *
     * @param logisticsId 物流ID
     * @return 轨迹列表
     */
    @GetMapping("/{logisticsId}/tracks")
    @Operation(summary = "获取物流轨迹列表")
    public CommonResult<List<LogisticsTrack>> getLogisticsTracks(@PathVariable("logisticsId") Long logisticsId) {
        try {
            List<LogisticsTrack> tracks = logisticsTrackService.getTracksByLogisticsId(logisticsId);
            return CommonResult.success(tracks);
        } catch (Exception e) {
            log.error("获取物流轨迹列表失败", e);
            return CommonResult.failed("获取物流轨迹列表失败: " + e.getMessage());
        }
    }

    /**
     * 生成物流单号
     *
     * @param companyCode 物流公司代码
     * @return 物流单号
     */
    @GetMapping("/generateTrackingNo")
    @Operation(summary = "生成物流单号")
    public CommonResult<String> generateTrackingNo(@RequestParam("companyCode") String companyCode) {
        try {
            String trackingNo = logisticsService.generateTrackingNo(companyCode);
            return CommonResult.success(trackingNo);
        } catch (Exception e) {
            log.error("生成物流单号失败", e);
            return CommonResult.failed("生成物流单号失败: " + e.getMessage());
        }
    }
}