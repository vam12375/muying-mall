package com.muyingmall.service.impl;

import com.muyingmall.entity.AdminLoginRecord;
import com.muyingmall.entity.AdminOperationLog;
import com.muyingmall.service.AdminLoginRecordService;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel导出服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private final AdminLoginRecordService loginRecordService;
    private final AdminOperationLogService operationLogService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportLoginRecords(HttpServletResponse response, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String loginStatus, String ipAddress) throws Exception {

        // 查询所有符合条件的登录记录
        com.baomidou.mybatisplus.core.metadata.IPage<AdminLoginRecord> page = loginRecordService.getLoginRecordsPage(1,
                Integer.MAX_VALUE, adminId, startTime, endTime, loginStatus, ipAddress);

        List<AdminLoginRecord> records = page.getRecords();
        String fileName = "管理员登录记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        exportLoginRecordsList(response, records, fileName);
    }

    @Override
    public void exportOperationLogs(HttpServletResponse response, Integer adminId,
            LocalDateTime startTime, LocalDateTime endTime,
            String operationType, String module,
            String operationResult) throws Exception {

        // 查询所有符合条件的操作日志
        com.baomidou.mybatisplus.core.metadata.IPage<AdminOperationLog> page = operationLogService.getOperationLogsPage(
                1, Integer.MAX_VALUE, adminId, startTime, endTime,
                operationType, module, operationResult);

        List<AdminOperationLog> logs = page.getRecords();
        String fileName = "管理员操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        exportOperationLogsList(response, logs, fileName);
    }

    @Override
    public void exportLoginRecordsList(HttpServletResponse response, List<AdminLoginRecord> records, String fileName)
            throws Exception {
        setExcelResponseHeaders(response, fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("登录记录");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "管理员ID", "管理员用户名", "登录时间", "登出时间", "IP地址", "地理位置",
                    "设备类型", "浏览器", "操作系统", "登录状态", "失败原因", "会话时长(秒)", "创建时间" };

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;

            for (AdminLoginRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(record.getId() != null ? record.getId().toString() : "");
                row.createCell(1).setCellValue(record.getAdminId() != null ? record.getAdminId().toString() : "");
                row.createCell(2).setCellValue(record.getAdminName() != null ? record.getAdminName() : "");
                row.createCell(3).setCellValue(
                        record.getLoginTime() != null ? record.getLoginTime().format(DATE_TIME_FORMATTER) : "");
                row.createCell(4).setCellValue(
                        record.getLogoutTime() != null ? record.getLogoutTime().format(DATE_TIME_FORMATTER) : "");
                row.createCell(5).setCellValue(record.getIpAddress() != null ? record.getIpAddress() : "");
                row.createCell(6).setCellValue(record.getLocation() != null ? record.getLocation() : "");
                row.createCell(7).setCellValue(record.getDeviceType() != null ? record.getDeviceType() : "");
                row.createCell(8).setCellValue(record.getBrowser() != null ? record.getBrowser() : "");
                row.createCell(9).setCellValue(record.getOs() != null ? record.getOs() : "");
                row.createCell(10).setCellValue(record.getLoginStatus() != null ? record.getLoginStatus() : "");
                row.createCell(11).setCellValue(record.getFailureReason() != null ? record.getFailureReason() : "");
                row.createCell(12).setCellValue(
                        record.getDurationSeconds() != null ? record.getDurationSeconds().toString() : "");
                row.createCell(13).setCellValue(
                        record.getCreateTime() != null ? record.getCreateTime().format(DATE_TIME_FORMATTER) : "");

                // 应用数据样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void exportOperationLogsList(HttpServletResponse response, List<AdminOperationLog> logs, String fileName)
            throws Exception {
        setExcelResponseHeaders(response, fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("操作日志");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "管理员ID", "管理员用户名", "操作名称", "操作模块", "操作类型",
                    "目标类型", "目标ID", "请求方法", "请求URL", "IP地址", "操作结果",
                    "错误信息", "执行时间(毫秒)", "操作描述", "创建时间" };

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;

            for (AdminOperationLog log : logs) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(log.getId() != null ? log.getId().toString() : "");
                row.createCell(1).setCellValue(log.getAdminId() != null ? log.getAdminId().toString() : "");
                row.createCell(2).setCellValue(log.getAdminName() != null ? log.getAdminName() : "");
                row.createCell(3).setCellValue(log.getOperation() != null ? log.getOperation() : "");
                row.createCell(4).setCellValue(log.getModule() != null ? log.getModule() : "");
                row.createCell(5).setCellValue(log.getOperationType() != null ? log.getOperationType() : "");
                row.createCell(6).setCellValue(log.getTargetType() != null ? log.getTargetType() : "");
                row.createCell(7).setCellValue(log.getTargetId() != null ? log.getTargetId() : "");
                row.createCell(8).setCellValue(log.getRequestMethod() != null ? log.getRequestMethod() : "");
                row.createCell(9).setCellValue(log.getRequestUrl() != null ? log.getRequestUrl() : "");
                row.createCell(10).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                row.createCell(11).setCellValue(log.getOperationResult() != null ? log.getOperationResult() : "");
                row.createCell(12).setCellValue(log.getErrorMessage() != null ? log.getErrorMessage() : "");
                row.createCell(13)
                        .setCellValue(log.getExecutionTimeMs() != null ? log.getExecutionTimeMs().toString() : "");
                row.createCell(14).setCellValue(log.getDescription() != null ? log.getDescription() : "");
                row.createCell(15).setCellValue(
                        log.getCreateTime() != null ? log.getCreateTime().format(DATE_TIME_FORMATTER) : "");

                // 应用数据样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void setExcelResponseHeaders(HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        } catch (Exception e) {
            log.error("设置Excel响应头失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        return style;
    }
}
