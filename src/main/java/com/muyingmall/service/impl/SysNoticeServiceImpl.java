package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.SysNotice;
import com.muyingmall.mapper.SysNoticeMapper;
import com.muyingmall.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统通知公告服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public IPage<SysNotice> getNoticePage(int page, int size, String type, String status, String keyword) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        
        // 类型筛选
        if (StringUtils.hasText(type) && !"all".equals(type)) {
            wrapper.eq(SysNotice::getType, type);
        }
        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysNotice::getTitle, keyword);
        }
        // 排序：置顶优先，然后按发布时间倒序
        wrapper.orderByDesc(SysNotice::getIsPinned)
               .orderByDesc(SysNotice::getPublishTime);
        
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> getNoticeStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数
        stats.put("total", count());
        // 已发布数
        stats.put("published", count(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, "published")));
        // 草稿数
        stats.put("draft", count(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, "draft")));
        // 置顶数
        stats.put("pinned", count(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getIsPinned, 1)));
        
        return stats;
    }

    @Override
    public boolean publishNotice(SysNotice notice) {
        notice.setStatus("published");
        notice.setPublishTime(LocalDateTime.now());
        if (notice.getViewCount() == null) {
            notice.setViewCount(0);
        }
        if (notice.getIsPinned() == null) {
            notice.setIsPinned(0);
        }
        
        if (notice.getId() != null) {
            return updateById(notice);
        }
        return save(notice);
    }

    @Override
    public boolean withdrawNotice(Integer id) {
        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setStatus("draft");
        notice.setPublishTime(null);
        return updateById(notice);
    }

    @Override
    public boolean togglePinned(Integer id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            return false;
        }
        notice.setIsPinned(notice.getIsPinned() == 1 ? 0 : 1);
        return updateById(notice);
    }

    @Override
    public boolean incrementViewCount(Integer id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            return false;
        }
        notice.setViewCount(notice.getViewCount() + 1);
        return updateById(notice);
    }
}
