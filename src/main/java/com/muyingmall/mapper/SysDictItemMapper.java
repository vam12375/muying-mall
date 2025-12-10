package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 字典项Mapper接口
 */
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    /**
     * 统计指定字典类型下的字典项数量
     */
    @Select("SELECT COUNT(*) FROM sys_dict_item WHERE dict_code = #{dictCode}")
    int countByDictCode(@Param("dictCode") String dictCode);
}
