package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键获取配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey} AND status = 1")
    SystemConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 根据分组获取配置列表
     */
    @Select("SELECT * FROM system_config WHERE config_group = #{group} AND status = 1 ORDER BY sort_order")
    List<SystemConfig> selectByGroup(@Param("group") String group);

    /**
     * 获取所有启用的配置
     */
    @Select("SELECT * FROM system_config WHERE status = 1 ORDER BY config_group, sort_order")
    List<SystemConfig> selectAllEnabled();
}
