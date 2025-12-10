package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.SysDictType;

import java.util.List;

/**
 * 字典类型服务接口
 */
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 获取所有字典类型（包含字典项数量）
     */
    List<SysDictType> getAllDictTypes();

    /**
     * 根据编码获取字典类型
     */
    SysDictType getByCode(String code);

    /**
     * 新增字典类型
     */
    boolean addDictType(SysDictType dictType);

    /**
     * 更新字典类型
     */
    boolean updateDictType(SysDictType dictType);

    /**
     * 删除字典类型（同时删除字典项）
     */
    boolean deleteDictType(Integer id);

    /**
     * 切换字典类型状态
     */
    boolean toggleStatus(Integer id);
}
