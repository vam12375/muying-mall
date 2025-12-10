package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.SysDictItem;
import com.muyingmall.mapper.SysDictItemMapper;
import com.muyingmall.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典项服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public List<SysDictItem> getItemsByDictCode(String dictCode) {
        return list(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, dictCode)
                .orderByAsc(SysDictItem::getSort));
    }

    @Override
    public boolean addDictItem(SysDictItem dictItem) {
        // 检查同一字典类型下value是否重复
        long count = count(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, dictItem.getDictCode())
                .eq(SysDictItem::getValue, dictItem.getValue()));
        if (count > 0) {
            throw new RuntimeException("字典项值已存在");
        }
        if (dictItem.getStatus() == null) {
            dictItem.setStatus("enabled");
        }
        if (dictItem.getSort() == null) {
            dictItem.setSort(0);
        }
        return save(dictItem);
    }

    @Override
    public boolean updateDictItem(SysDictItem dictItem) {
        // 检查同一字典类型下value是否被其他记录使用
        SysDictItem existing = getOne(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, dictItem.getDictCode())
                .eq(SysDictItem::getValue, dictItem.getValue()));
        if (existing != null && !existing.getId().equals(dictItem.getId())) {
            throw new RuntimeException("字典项值已存在");
        }
        return updateById(dictItem);
    }

    @Override
    public boolean deleteDictItem(Integer id) {
        return removeById(id);
    }

    @Override
    public boolean toggleStatus(Integer id) {
        SysDictItem dictItem = getById(id);
        if (dictItem == null) {
            return false;
        }
        dictItem.setStatus("enabled".equals(dictItem.getStatus()) ? "disabled" : "enabled");
        return updateById(dictItem);
    }

    @Override
    public boolean deleteByDictCode(String dictCode) {
        return remove(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, dictCode));
    }
}
