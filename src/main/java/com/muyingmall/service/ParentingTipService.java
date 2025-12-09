package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.ParentingTip;
import com.muyingmall.entity.ParentingTipCategory;
import com.muyingmall.entity.ParentingTipComment;

import java.util.List;

/**
 * 育儿知识服务接口
 */
public interface ParentingTipService extends IService<ParentingTip> {

    /**
     * 分页查询育儿知识
     */
    IPage<ParentingTip> getPage(Integer page, Integer pageSize, Integer categoryId, String keyword);

    /**
     * 获取热门知识
     */
    List<ParentingTip> getHotTips(Integer limit);

    /**
     * 获取所有分类
     */
    List<ParentingTipCategory> getCategories();

    /**
     * 获取详情
     */
    ParentingTip getDetail(Long id);

    /**
     * 增加浏览量
     */
    void increaseViewCount(Long id);

    /**
     * 获取相关知识
     */
    List<ParentingTip> getRelatedTips(Integer categoryId, Long excludeId, Integer limit);

    /**
     * 分页获取评论
     */
    IPage<ParentingTipComment> getComments(Long tipId, Integer page, Integer pageSize);

    /**
     * 添加评论
     */
    ParentingTipComment addComment(Long tipId, Integer userId, String content);
}
