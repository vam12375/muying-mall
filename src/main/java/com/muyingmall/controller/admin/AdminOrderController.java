package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsCompany;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.LogisticsCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台订单管理控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理后台订单管理", description = "管理后台订单查询、管理相关接口")
public class AdminOrderController {

    private final OrderService orderService;
    private final LogisticsService logisticsService;
    private final LogisticsCompanyService logisticsCompanyService;

    /**
     * 分页获取订单列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param status   订单状态
     * @param orderNo  订单编号
     * @param userId   用户ID
     * @return 订单分页列表
     */
    @GetMapping
    @Operation(summary = "分页获取订单列表")
    public CommonResult<Map<String, Object>> getOrderList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "userId", required = false) Integer userId) {
        try {
            Page<Order> orderPage = orderService.getOrdersByAdmin(page, pageSize, status, orderNo, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("list", orderPage.getRecords());
            result.put("total", orderPage.getTotal());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return CommonResult.failed("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单统计数据
     *
     * @return 订单统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计数据")
    public CommonResult<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> statistics = orderService.getOrderStatistics(null);

            // 转换为前端需要的格式
            Map<String, Object> result = new HashMap<>();
            result.put("total", statistics.get("totalCount"));
            result.put("pending_payment", statistics.get("pendingPaymentCount"));
            result.put("pending_shipment", statistics.get("pendingShipmentCount"));
            result.put("shipped", statistics.get("shippedCount"));
            result.put("completed", statistics.get("completedCount"));
            result.put("cancelled", statistics.get("cancelledCount"));

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取订单统计数据失败", e);
            return CommonResult.failed("获取订单统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public CommonResult<Order> getOrderDetail(@PathVariable("id") Integer id) {
        try {
            Order order = orderService.getOrderDetailByAdmin(id);
            return CommonResult.success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return CommonResult.failed("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     *
     * @param id     订单ID
     * @param status 订单状态
     * @param remark 备注
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态")
    public CommonResult<Boolean> updateOrderStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") String status,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            boolean result = orderService.updateOrderStatusByAdmin(id, status, remark);
            if (result) {
                return CommonResult.success(true, "更新订单状态成功");
            } else {
                return CommonResult.failed("更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return CommonResult.failed("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 订单发货
     *
     * @param id              订单ID
     * @param companyId       物流公司ID
     * @param trackingNo      物流单号（可选）
     * @param receiverName    收件人姓名
     * @param receiverPhone   收件人电话
     * @param receiverAddress 收件人地址
     * @return 发货结果
     */
    @PutMapping("/{id}/ship")
    @Operation(summary = "订单发货")
    public CommonResult<Boolean> shipOrder(
            @PathVariable("id") Integer id,
            @RequestParam("companyId") Integer companyId,
            @RequestParam(value = "trackingNo", required = false) String trackingNo,
            @RequestParam(value = "receiverName", required = false) String receiverName,
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
            @RequestParam(value = "receiverAddress", required = false) String receiverAddress) {
        try {
            // 获取物流公司
            LogisticsCompany company = logisticsCompanyService.getById(companyId);
            if (company == null) {
                return CommonResult.failed("物流公司不存在");
            }

            // 获取订单信息
            Order order = orderService.getById(id);
            if (order == null) {
                return CommonResult.failed("订单不存在");
            }

            // 如果未提供物流单号，自动生成
            String finalTrackingNo = trackingNo;
            if (!StringUtils.hasText(finalTrackingNo)) {
                finalTrackingNo = logisticsService.generateTrackingNo(company.getCode());
            }

            // 如果未提供收件人信息，使用订单信息
            String finalReceiverName = receiverName;
            if (!StringUtils.hasText(finalReceiverName)) {
                finalReceiverName = order.getReceiverName();
            }

            String finalReceiverPhone = receiverPhone;
            if (!StringUtils.hasText(finalReceiverPhone)) {
                finalReceiverPhone = order.getReceiverPhone();
            }

            String finalReceiverAddress = receiverAddress;
            if (!StringUtils.hasText(finalReceiverAddress)) {
                finalReceiverAddress = order.getReceiverProvince() + order.getReceiverCity()
                        + order.getReceiverDistrict() + order.getReceiverAddress();
            }

            // 创建物流记录
            Logistics logistics = new Logistics();
            logistics.setOrderId(id);
            logistics.setCompanyId(companyId);
            logistics.setTrackingNo(finalTrackingNo);
            logistics.setReceiverName(finalReceiverName);
            logistics.setReceiverPhone(finalReceiverPhone);
            logistics.setReceiverAddress(finalReceiverAddress);

            // 保存物流记录
            boolean logisticsResult = logisticsService.createLogistics(logistics);
            if (!logisticsResult) {
                return CommonResult.failed("创建物流记录失败");
            }

            // 更新订单发货信息
            boolean orderResult = orderService.shipOrder(id, company.getName(), finalTrackingNo);
            if (!orderResult) {
                return CommonResult.failed("更新订单发货信息失败");
            }

            return CommonResult.success(true, "订单发货成功");
        } catch (Exception e) {
            log.error("订单发货失败", e);
            return CommonResult.failed("订单发货失败: " + e.getMessage());
        }
    }

    /**
     * 导出订单数据
     *
     * @param response HTTP响应
     * @param status   订单状态
     * @param orderNo  订单编号
     * @param userId   用户ID
     */
    @GetMapping("/export")
    @Operation(summary = "导出订单数据")
    public void exportOrders(
            HttpServletResponse response,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "userId", required = false) Integer userId) {
        try {
            // 这里简单实现，实际应该使用POI或EasyExcel等库处理Excel导出
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");

            // 设置文件名
            String fileName = "订单数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);

            // 获取全部订单数据（不分页，注意性能问题）
            Page<Order> orderPage = orderService.getOrdersByAdmin(1, Integer.MAX_VALUE, status, orderNo, userId);

            // TODO: 实现Excel导出逻辑，可使用POI或EasyExcel
            // 这里暂时返回简单的CSV内容作为示例
            response.getWriter().println("订单ID,订单编号,用户ID,订单状态,订单金额,实付金额,创建时间");
            for (Order order : orderPage.getRecords()) {
                response.getWriter().println(
                        order.getOrderId() + "," +
                                order.getOrderNo() + "," +
                                order.getUserId() + "," +
                                order.getStatus() + "," +
                                order.getTotalAmount() + "," +
                                order.getActualAmount() + "," +
                                order.getCreateTime());
            }
        } catch (IOException e) {
            log.error("导出订单数据失败", e);
            // 在异常情况下也要返回错误信息
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().println("{\"code\":500,\"message\":\"导出订单数据失败: " + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
}