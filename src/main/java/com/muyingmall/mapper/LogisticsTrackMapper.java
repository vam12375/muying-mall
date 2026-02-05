package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.LogisticsTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物流轨迹Mapper接口
 */
@Mapper
public interface LogisticsTrackMapper extends BaseMapper<LogisticsTrack> {

    /**
     * 批量获取每个物流单最新的轨迹点
     *
     * @param logisticsIds 物流ID列表
     * @return 最新轨迹列表
     */
    @Select({
            "<script>",
            "SELECT t.*",
            "FROM logistics_track t",
            "JOIN (",
            "  SELECT logistics_id, MAX(tracking_time) AS max_time",
            "  FROM logistics_track",
            "  WHERE logistics_id IN",
            "  <foreach item='id' collection='logisticsIds' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "  GROUP BY logistics_id",
            ") latest ON t.logistics_id = latest.logistics_id AND t.tracking_time = latest.max_time",
            "</script>"
    })
    List<LogisticsTrack> selectLatestTracksByLogisticsIds(@Param("logisticsIds") List<Long> logisticsIds);
}
