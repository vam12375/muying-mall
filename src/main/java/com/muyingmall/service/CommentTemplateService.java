package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.CommentTemplate;

import java.util.List;

/**
 * 评价模板服务接口
 */
public interface CommentTemplateService extends IService<CommentTemplate> {

    /**
     * 获取系统预设模板
     *
     * @param rating     评分
     * @param categoryId 商品类别ID (可选)
     * @return 模板列表
     */
    List<CommentTemplate> getSystemTemplates(Integer rating, Integer categoryId);

    /**
     * 获取用户自定义模板
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    List<CommentTemplate> getUserTemplates(Integer userId);

    /**
     * 创建用户自定义模板
     *
     * @param template 模板对象
     * @return 是否成功
     */
    boolean createUserTemplate(CommentTemplate template);

    /**
     * 更新模板
     *
     * @param template 模板对象
     * @return 是否成功
     */
    boolean updateTemplate(CommentTemplate template);

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     * @param userId     用户ID (用于验证权限)
     * @return 是否成功
     */
    boolean deleteTemplate(Integer templateId, Integer userId);

    /**
     * 增加模板使用次数
     *
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean incrementUseCount(Integer templateId);

    /**
     * 分页获取所有模板（管理员使用）
     *
     * @param page         页码
     * @param size         每页大小
     * @param templateType 模板类型
     * @return 分页结果
     */
    IPage<CommentTemplate> getTemplatesPage(int page, int size, Integer templateType);
}