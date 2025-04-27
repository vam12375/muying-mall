package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.PointsHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分历史记录Mapper接口
 */
@Mapper
public interface PointsHistoryMapper extends BaseMapper<PointsHistory> {
}