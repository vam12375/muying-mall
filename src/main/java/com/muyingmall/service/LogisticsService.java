package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.dto.logistics.LogisticsMapPointDTO;
import com.muyingmall.entity.Logistics;
import com.muyingmall.enums.LogisticsStatus;

import java.util.List;

/**
 * 物流服务接口
 */
public interface LogisticsService extends IService<Logistics> {

    /**
     * 分页获取物流列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param status   物流状态
     * @param keyword  搜索关键词
     * @return 物流分页列表
     */
    Page<Logistics> getLogisticsList(int page, int pageSize, String status, String keyword);

    /**
     * 根据ID获取物流详情
     *
     * @param id 物流ID
     * @return 物流详情
     */
    Logistics getLogisticsById(Long id);

    /**
     * 根据订单ID获取物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息
     */
    Logistics getLogisticsByOrderId(Integer orderId);

    /**
     * 根据物流单号获取物流信息
     *
     * @param trackingNo 物流单号
     * @return 物流信息
     */
    Logistics getLogisticsByTrackingNo(String trackingNo);

    /**
     * 创建物流记录
     *
     * @param logistics 物流信息
     * @return 是否创建成功
     */
    boolean createLogistics(Logistics logistics);

    /**
     * 更新物流状态
     *
     * @param id     物流ID
     * @param status 物流状态
     * @param remark 备注
     * @return 是否更新成功
     */
    boolean updateLogisticsStatus(Long id, String status, String remark);

    /**
     * 生成物流单号
     *
     * @param companyCode 物流公司代码
     * @return 生成的物流单号
     */
    String generateTrackingNo(String companyCode);

    /**
     * 生成标准物流轨迹
     *
     * @param logisticsId 物流ID
     * @param operator    操作人
     * @return 是否生成成功
     */
    boolean generateStandardTracks(Long logisticsId, String operator);

    /**
     * 【场景3：物流轨迹可视化】根据真实路径规划生成物流轨迹
     * 调用高德地图API获取驾车路线，生成带坐标的轨迹点
     *
     * @param logisticsId 物流ID
     * @param destLng     目标经度
     * @param destLat     目标纬度
     * @return 是否生成成功
     */
    boolean generateRouteBasedTracks(Long logisticsId, Double destLng, Double destLat);

    /**
     * 根据状态统计物流数量
     * 修复：添加统计方法
     *
     * @param status 物流状态
     * @return 数量
     */
    long countByStatus(String status);

    /**
     * 获取地图大屏点位列表
     *
     * @param statuses 物流状态过滤（可空）
     * @param limit    最大数量
     * @return 点位列表
     */
    List<LogisticsMapPointDTO> getLogisticsMapPoints(List<LogisticsStatus> statuses, int limit);
}
