package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Logistics;
import com.muyingmall.entity.LogisticsTrack;
import com.muyingmall.enums.LogisticsStatus;
import com.muyingmall.mapper.LogisticsTrackMapper;
import com.muyingmall.service.LogisticsTrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流轨迹服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsTrackServiceImpl extends ServiceImpl<LogisticsTrackMapper, LogisticsTrack>
        implements LogisticsTrackService {

    /**
     * 根据物流ID获取轨迹列表
     *
     * @param logisticsId 物流ID
     * @return 轨迹列表
     */
    @Override
    public List<LogisticsTrack> getTracksByLogisticsId(Long logisticsId) {
        LambdaQueryWrapper<LogisticsTrack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsTrack::getLogisticsId, logisticsId);
        queryWrapper.orderByDesc(LogisticsTrack::getTrackingTime);
        return list(queryWrapper);
    }

    /**
     * 创建初始物流轨迹
     *
     * @param logistics 物流信息
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createInitialTrack(Logistics logistics) {
        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsId(logistics.getId());
        track.setTrackingTime(LocalDateTime.now());
        track.setStatus("CREATED");
        track.setContent("物流信息已创建，等待揽收");
        track.setOperator("系统");
        
        // 手动设置创建时间，解决自动填充可能失效的问题
        track.setCreateTime(LocalDateTime.now());

        return save(track);
    }

    /**
     * 创建物流状态变更轨迹
     *
     * @param logistics 物流信息
     * @param operator  操作人
     * @param remark    备注
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createStatusTrack(Logistics logistics, String operator, String remark) {
        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsId(logistics.getId());
        track.setTrackingTime(LocalDateTime.now());
        track.setStatus(logistics.getStatus().getCode());

        // 根据状态生成内容
        String content;
        switch (logistics.getStatus()) {
            case CREATED:
                content = "物流信息已创建，等待揽收";
                break;
            case SHIPPING:
                content = "包裹已发出，运输中";
                break;
            case DELIVERED:
                content = "包裹已送达";
                break;
            case EXCEPTION:
                content = "物流异常";
                break;
            default:
                content = "状态更新";
        }

        // 添加备注
        if (StringUtils.hasText(remark)) {
            content += "，备注：" + remark;
        }

        track.setContent(content);
        track.setOperator(StringUtils.hasText(operator) ? operator : "系统");
        
        // 手动设置创建时间，解决自动填充可能失效的问题
        track.setCreateTime(LocalDateTime.now());

        return save(track);
    }

    /**
     * 添加物流轨迹
     *
     * @param track 轨迹信息
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTrack(LogisticsTrack track) {
        // 设置轨迹时间
        if (track.getTrackingTime() == null) {
            track.setTrackingTime(LocalDateTime.now());
        }
        
        // 手动设置创建时间，解决自动填充可能失效的问题
        if (track.getCreateTime() == null) {
            track.setCreateTime(LocalDateTime.now());
        }

        return save(track);
    }
}