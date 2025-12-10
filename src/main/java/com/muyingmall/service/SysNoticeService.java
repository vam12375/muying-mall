package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.SysNotice;

import java.util.Map;

/**
 * 系统通知公告服务接口
 */
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 分页查询公告
     */
    IPage<SysNotice> getNoticePage(int page, int size, String type, String status, String keyword);

    /**
     * 获取公告统计信息
     */
    Map<String, Object> getNoticeStats();

    /**
     * 发布公告
     */
    boolean publishNotice(SysNotice notice);

    /**
     * 撤回公告（改为草稿）
     */
    boolean withdrawNotice(Integer id);

    /**
     * 切换置顶状态
     */
    boolean togglePinned(Integer id);

    /**
     * 增加浏览次数
     */
    boolean incrementViewCount(Integer id);
}
