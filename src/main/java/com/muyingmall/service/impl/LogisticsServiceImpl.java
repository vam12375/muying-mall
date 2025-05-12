package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsCompany;
import com.muyingmall.entity.Order;
import com.muyingmall.enums.LogisticsStatus;
import com.muyingmall.mapper.LogisticsMapper;
import com.muyingmall.service.LogisticsCompanyService;
import com.muyingmall.service.LogisticsService;
import com.muyingmall.service.LogisticsTrackService;
import com.muyingmall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 物流服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl extends ServiceImpl<LogisticsMapper, Logistics> implements LogisticsService {

    private final LogisticsCompanyService logisticsCompanyService;
    private final LogisticsTrackService logisticsTrackService;
    private final OrderService orderService;
    private final Random random = new Random();

    /**
     * 分页获取物流列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param status   物流状态
     * @param keyword  搜索关键词
     * @return 物流分页列表
     */
    @Override
    public Page<Logistics> getLogisticsList(int page, int pageSize, String status, String keyword) {
        Page<Logistics> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();

        // 添加状态条件
        if (StringUtils.hasText(status)) {
            LogisticsStatus logisticsStatus = LogisticsStatus.getByCode(status);
            if (logisticsStatus != null) {
                queryWrapper.eq(Logistics::getStatus, logisticsStatus);
            }
        }

        // 添加关键词搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Logistics::getTrackingNo, keyword)
                    .or()
                    .like(Logistics::getReceiverName, keyword)
                    .or()
                    .like(Logistics::getReceiverPhone, keyword));
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Logistics::getCreateTime);

        Page<Logistics> logisticsPage = page(pageParam, queryWrapper);

        // 填充物流公司信息
        logisticsPage.getRecords().forEach(logistics -> {
            // 获取物流公司信息
            LogisticsCompany company = logisticsCompanyService.getById(logistics.getCompanyId());
            logistics.setCompany(company);

            // 获取物流轨迹信息
            logistics.setTracks(logisticsTrackService.getTracksByLogisticsId(logistics.getId()));
        });

        return logisticsPage;
    }

    /**
     * 根据ID获取物流详情
     *
     * @param id 物流ID
     * @return 物流详情
     */
    @Override
    public Logistics getLogisticsById(Long id) {
        Logistics logistics = getById(id);
        if (logistics != null) {
            // 填充物流公司信息
            LogisticsCompany company = logisticsCompanyService.getById(logistics.getCompanyId());
            logistics.setCompany(company);

            // 填充物流轨迹信息
            logistics.setTracks(logisticsTrackService.getTracksByLogisticsId(id));

            // 填充订单信息
            Order order = orderService.getById(logistics.getOrderId());
            logistics.setOrder(order);
        }
        return logistics;
    }

    /**
     * 根据订单ID获取物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息
     */
    @Override
    public Logistics getLogisticsByOrderId(Integer orderId) {
        LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Logistics::getOrderId, orderId);

        Logistics logistics = getOne(queryWrapper);
        if (logistics != null) {
            // 填充物流公司信息
            LogisticsCompany company = logisticsCompanyService.getById(logistics.getCompanyId());
            logistics.setCompany(company);

            // 填充物流轨迹信息
            logistics.setTracks(logisticsTrackService.getTracksByLogisticsId(logistics.getId()));
        }
        return logistics;
    }

    /**
     * 根据物流单号获取物流信息
     *
     * @param trackingNo 物流单号
     * @return 物流信息
     */
    @Override
    public Logistics getLogisticsByTrackingNo(String trackingNo) {
        if (!StringUtils.hasText(trackingNo)) {
            return null;
        }

        LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Logistics::getTrackingNo, trackingNo);

        Logistics logistics = getOne(queryWrapper);
        if (logistics != null) {
            // 填充物流公司信息
            LogisticsCompany company = logisticsCompanyService.getById(logistics.getCompanyId());
            logistics.setCompany(company);

            // 填充物流轨迹信息
            logistics.setTracks(logisticsTrackService.getTracksByLogisticsId(logistics.getId()));
        }
        return logistics;
    }

    /**
     * 创建物流记录
     *
     * @param logistics 物流信息
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createLogistics(Logistics logistics) {
        // 设置默认状态
        if (logistics.getStatus() == null) {
            logistics.setStatus(LogisticsStatus.CREATED);
        }

        // 生成物流单号
        if (!StringUtils.hasText(logistics.getTrackingNo())) {
            LogisticsCompany company = logisticsCompanyService.getById(logistics.getCompanyId());
            if (company == null) {
                throw new RuntimeException("物流公司不存在");
            }

            String trackingNo = generateTrackingNo(company.getCode());
            logistics.setTrackingNo(trackingNo);
        }

        // 设置发货时间
        if (logistics.getShippingTime() == null) {
            logistics.setShippingTime(LocalDateTime.now());
        }
        
        // 手动设置创建时间和更新时间，解决自动填充可能失效的问题
        if (logistics.getCreateTime() == null) {
            logistics.setCreateTime(LocalDateTime.now());
        }
        if (logistics.getUpdateTime() == null) {
            logistics.setUpdateTime(LocalDateTime.now());
        }

        boolean result = save(logistics);

        // 创建初始物流轨迹
        if (result) {
            logisticsTrackService.createInitialTrack(logistics);
        }

        return result;
    }

    /**
     * 更新物流状态
     *
     * @param id     物流ID
     * @param status 物流状态
     * @param remark 备注
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLogisticsStatus(Long id, String status, String remark) {
        Logistics logistics = getById(id);
        if (logistics == null) {
            throw new RuntimeException("物流记录不存在");
        }

        LogisticsStatus logisticsStatus = LogisticsStatus.getByCode(status);
        if (logisticsStatus == null) {
            throw new RuntimeException("物流状态不存在");
        }

        logistics.setStatus(logisticsStatus);
        if (StringUtils.hasText(remark)) {
            logistics.setRemark(remark);
        }

        // 如果状态为已送达，设置送达时间
        if (LogisticsStatus.DELIVERED.equals(logisticsStatus)) {
            logistics.setDeliveryTime(LocalDateTime.now());
        }

        boolean result = updateById(logistics);

        // 创建物流轨迹
        if (result) {
            logisticsTrackService.createStatusTrack(logistics, "系统更新", remark);
        }

        return result;
    }

    /**
     * 生成物流单号
     * 格式：物流公司代码（2位大写字母）+ 日期时间（12位：年月日时分秒）+ 4位随机数
     *
     * @param companyCode 物流公司代码
     * @return 生成的物流单号
     */
    @Override
    public String generateTrackingNo(String companyCode) {
        if (!StringUtils.hasText(companyCode)) {
            throw new IllegalArgumentException("物流公司代码不能为空");
        }

        // 获取物流公司代码（保证是大写的）
        String code = companyCode.toUpperCase();

        // 获取当前日期时间的格式化字符串
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        // 生成4位随机数
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);

        // 拼接物流单号
        String trackingNo = code + dateStr + randomStr;

        // 检查单号是否已存在，如果存在则重新生成
        if (checkTrackingNoExists(trackingNo)) {
            return generateTrackingNo(companyCode);
        }

        return trackingNo;
    }

    /**
     * 检查物流单号是否已存在
     *
     * @param trackingNo 物流单号
     * @return 是否存在
     */
    private boolean checkTrackingNoExists(String trackingNo) {
        LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Logistics::getTrackingNo, trackingNo);
        return count(queryWrapper) > 0;
    }
}