package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.MemberLevel;
import com.muyingmall.mapper.MemberLevelMapper;
import com.muyingmall.service.MemberLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 会员等级服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;

    @Override
    public List<MemberLevel> getAllLevels() {
        LambdaQueryWrapper<MemberLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(MemberLevel::getMinPoints);
        return memberLevelMapper.selectList(queryWrapper);
    }

    @Override
    public MemberLevel getLevelByPoints(Integer points) {
        if (points == null) {
            log.warn("积分为空，返回默认会员等级");
            return getDefaultLevel();
        }

        // 查询所有会员等级
        List<MemberLevel> levels = getAllLevels();
        if (levels.isEmpty()) {
            log.warn("没有配置会员等级，返回默认会员等级");
            return getDefaultLevel();
        }

        // 按照积分要求降序排序
        levels.sort(Comparator.comparing(MemberLevel::getMinPoints).reversed());

        // 查找符合条件的最高等级
        for (MemberLevel level : levels) {
            if (points >= level.getMinPoints()) {
                return level;
            }
        }

        // 如果没有符合条件的等级，返回默认等级（积分要求最低的等级）
        levels.sort(Comparator.comparing(MemberLevel::getMinPoints));
        return levels.get(0);
    }

    @Override
    public String getLevelNameByPoints(Integer points) {
        MemberLevel level = getLevelByPoints(points);
        return level != null ? level.getLevelName() : "普通会员";
    }

    /**
     * 获取默认会员等级
     * 
     * @return 默认会员等级
     */
    private MemberLevel getDefaultLevel() {
        MemberLevel defaultLevel = new MemberLevel();
        defaultLevel.setId(1L);
        defaultLevel.setLevelName("普通会员");
        defaultLevel.setMinPoints(0);
        return defaultLevel;
    }
}