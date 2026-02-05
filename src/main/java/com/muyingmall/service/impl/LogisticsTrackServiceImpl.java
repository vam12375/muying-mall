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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        
        // 添加JSON扩展数据
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("type", "initial");
        detailsMap.put("systemGenerated", true);
        detailsMap.put("logisticsInfo", Map.of(
            "trackingNo", logistics.getTrackingNo(),
            "companyId", logistics.getCompanyId()
        ));
        track.setDetailsJson(detailsMap);
        
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
        
        // 添加JSON扩展数据
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("type", "statusChange");
        detailsMap.put("systemGenerated", true);
        detailsMap.put("previousStatus", logistics.getStatus().getCode());
        if (StringUtils.hasText(remark)) {
            detailsMap.put("remark", remark);
        }
        track.setDetailsJson(detailsMap);
        
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
        
        // 如果没有设置JSON数据，添加默认数据
        if (track.getDetailsJson() == null) {
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("type", "manual");
            detailsMap.put("systemGenerated", false);
            track.setDetailsJson(detailsMap);
        }
        
        // 手动设置创建时间，解决自动填充可能失效的问题
        if (track.getCreateTime() == null) {
            track.setCreateTime(LocalDateTime.now());
        }

        return save(track);
    }
    
    /**
     * 批量添加物流轨迹
     *
     * @param logisticsId 物流ID
     * @param tracks 轨迹列表
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddTracks(Long logisticsId, List<LogisticsTrack> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            log.warn("批量添加物流轨迹时传入空列表");
            return false;
        }
        
        log.debug("开始批量添加物流轨迹，物流ID: {}, 轨迹数量: {}", logisticsId, tracks.size());
        
        List<LogisticsTrack> processedTracks = new ArrayList<>(tracks.size());
        LocalDateTime now = LocalDateTime.now();
        
        // 处理每个轨迹点
        for (int i = 0; i < tracks.size(); i++) {
            LogisticsTrack track = tracks.get(i);
            
            // 设置物流ID
            track.setLogisticsId(logisticsId);
            
            // 设置轨迹时间（如果未设置）
            if (track.getTrackingTime() == null) {
                // 根据索引设置不同的时间，确保时间顺序
                track.setTrackingTime(now.plusMinutes(i * 30));
            }
            
            // 设置创建时间
            track.setCreateTime(now);
            
            // 设置默认操作人（如果未设置）
            if (!StringUtils.hasText(track.getOperator())) {
                track.setOperator("系统批量生成");
            }
            
            // 扩展JSON数据
            Map<String, Object> detailsJson = track.getDetailsJson();
            if (detailsJson == null) {
                detailsJson = new HashMap<>();
            }
            
            // 添加批量生成的标识
            detailsJson.put("batchGenerated", true);
            detailsJson.put("batchIndex", i);
            detailsJson.put("batchTimestamp", now.toString());
            
            track.setDetailsJson(detailsJson);
            
            processedTracks.add(track);
        }
        
        // 批量保存所有轨迹
        boolean result = saveBatch(processedTracks);
        log.debug("批量添加物流轨迹{}，物流ID: {}, 轨迹数量: {}", 
                result ? "成功" : "失败", logisticsId, processedTracks.size());
        
        return result;
    }

    /**
     * 批量获取物流最新轨迹点
     *
     * @param logisticsIds 物流ID列表
     * @return 物流ID -> 最新轨迹点
     */
    @Override
    public Map<Long, LogisticsTrack> getLatestTracksByLogisticsIds(List<Long> logisticsIds) {
        Map<Long, LogisticsTrack> result = new HashMap<>();
        if (logisticsIds == null || logisticsIds.isEmpty()) {
            return result;
        }

        List<LogisticsTrack> tracks = baseMapper.selectLatestTracksByLogisticsIds(logisticsIds);
        if (tracks == null || tracks.isEmpty()) {
            return result;
        }

        for (LogisticsTrack track : tracks) {
            if (track == null || track.getLogisticsId() == null) {
                continue;
            }
            LogisticsTrack existing = result.get(track.getLogisticsId());
            if (existing == null) {
                result.put(track.getLogisticsId(), track);
                continue;
            }

            if (track.getTrackingTime() != null && existing.getTrackingTime() != null) {
                int compare = track.getTrackingTime().compareTo(existing.getTrackingTime());
                if (compare > 0 || (compare == 0 && track.getId() != null && existing.getId() != null && track.getId() > existing.getId())) {
                    result.put(track.getLogisticsId(), track);
                }
            } else if (track.getId() != null && existing.getId() != null && track.getId() > existing.getId()) {
                result.put(track.getLogisticsId(), track);
            }
        }

        return result;
    }
}
