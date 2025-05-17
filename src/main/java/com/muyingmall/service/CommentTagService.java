package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CommentTag;

import java.util.List;

/**
 * 评价标签服务接口
 */
public interface CommentTagService extends IService<CommentTag> {

    /**
     * 获取热门标签列表
     *
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<CommentTag> getHotTags(Integer limit);

    /**
     * 根据商品分类获取标签列表
     *
     * @param categoryId 商品分类ID
     * @return 标签列表
     */
    List<CommentTag> getTagsByCategory(Integer categoryId);

    /**
     * 创建新标签
     *
     * @param tag 标签信息
     * @return 是否成功
     */
    boolean createTag(CommentTag tag);

    /**
     * 更新标签状态
     *
     * @param tagId  标签ID
     * @param status 状态：0-禁用，1-启用
     * @return 是否成功
     */
    boolean updateTagStatus(Integer tagId, Integer status);

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签ID
     * @return 是否成功
     */
    boolean incrementUsageCount(Integer tagId);

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签ID
     * @return 是否成功
     */
    boolean decrementUsageCount(Integer tagId);

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @param limit   限制数量
     * @return 标签列表
     */
    List<CommentTag> searchTags(String keyword, Integer limit);

    /**
     * 获取或创建标签（如果不存在）
     *
     * @param tagName 标签名称
     * @return 标签对象
     */
    CommentTag getOrCreateTag(String tagName);

    /**
     * 获取评价推荐标签
     *
     * @param productId  商品ID
     * @param categoryId 分类ID
     * @param limit      限制数量
     * @return 推荐标签列表
     */
    List<CommentTag> getRecommendedTags(Integer productId, Integer categoryId, Integer limit);
}