package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.SysDictType;
import com.muyingmall.mapper.SysDictItemMapper;
import com.muyingmall.mapper.SysDictTypeMapper;
import com.muyingmall.service.SysDictItemService;
import com.muyingmall.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典类型服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictItemMapper dictItemMapper;
    private final SysDictItemService dictItemService;

    @Override
    public List<SysDictType> getAllDictTypes() {
        List<SysDictType> dictTypes = list(new LambdaQueryWrapper<SysDictType>()
                .orderByAsc(SysDictType::getCreateTime));
        
        // 填充字典项数量
        for (SysDictType dictType : dictTypes) {
            int itemCount = dictItemMapper.countByDictCode(dictType.getCode());
            dictType.setItemCount(itemCount);
        }
        return dictTypes;
    }

    @Override
    public SysDictType getByCode(String code) {
        return getOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, code));
    }

    @Override
    public boolean addDictType(SysDictType dictType) {
        // 检查编码是否已存在
        if (getByCode(dictType.getCode()) != null) {
            throw new RuntimeException("字典类型编码已存在");
        }
        if (dictType.getStatus() == null) {
            dictType.setStatus("enabled");
        }
        return save(dictType);
    }

    @Override
    public boolean updateDictType(SysDictType dictType) {
        // 检查编码是否被其他记录使用
        SysDictType existing = getByCode(dictType.getCode());
        if (existing != null && !existing.getId().equals(dictType.getId())) {
            throw new RuntimeException("字典类型编码已存在");
        }
        return updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Integer id) {
        SysDictType dictType = getById(id);
        if (dictType == null) {
            return false;
        }
        // 删除关联的字典项
        dictItemService.deleteByDictCode(dictType.getCode());
        // 删除字典类型
        return removeById(id);
    }

    @Override
    public boolean toggleStatus(Integer id) {
        SysDictType dictType = getById(id);
        if (dictType == null) {
            return false;
        }
        dictType.setStatus("enabled".equals(dictType.getStatus()) ? "disabled" : "enabled");
        return updateById(dictType);
    }
}
