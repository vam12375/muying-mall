package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsTrack;

import java.util.List;

/**
 * 物流轨迹服务接口
 */
public interface LogisticsTrackService extends IService<LogisticsTrack> {

    /**
     * 根据物流ID获取轨迹列表
     *
     * @param logisticsId 物流ID
     * @return 轨迹列表
     */
    List<LogisticsTrack> getTracksByLogisticsId(Long logisticsId);

    /**
     * 创建初始物流轨迹
     *
     * @param logistics 物流信息
     * @return 是否创建成功
     */
    boolean createInitialTrack(Logistics logistics);

    /**
     * 创建物流状态变更轨迹
     *
     * @param logistics 物流信息
     * @param operator  操作人
     * @param remark    备注
     * @return 是否创建成功
     */
    boolean createStatusTrack(Logistics logistics, String operator, String remark);

    /**
     * 添加物流轨迹
     *
     * @param track 轨迹信息
     * @return 是否添加成功
     */
    boolean addTrack(LogisticsTrack track);
}