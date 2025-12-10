package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.SysDictItem;

import java.util.List;

/**
 * 字典项服务接口
 */
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 根据字典类型编码获取字典项列表
     */
    List<SysDictItem> getItemsByDictCode(String dictCode);

    /**
     * 新增字典项
     */
    boolean addDictItem(SysDictItem dictItem);

    /**
     * 更新字典项
     */
    boolean updateDictItem(SysDictItem dictItem);

    /**
     * 删除字典项
     */
    boolean deleteDictItem(Integer id);

    /**
     * 切换字典项状态
     */
    boolean toggleStatus(Integer id);

    /**
     * 根据字典类型编码删除所有字典项
     */
    boolean deleteByDictCode(String dictCode);
}
