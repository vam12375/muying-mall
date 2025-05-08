package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.Brand;

/**
 * 品牌服务接口
 */
public interface BrandService extends IService<Brand> {

    /**
     * 分页获取品牌列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词
     * @return 品牌分页对象
     */
    Page<Brand> getBrandPage(int page, int size, String keyword);

    /**
     * 获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    Brand getBrandDetail(Integer id);

    /**
     * 创建品牌
     *
     * @param brand 品牌信息
     * @return 创建结果
     */
    boolean createBrand(Brand brand);

    /**
     * 更新品牌
     *
     * @param brand 品牌信息
     * @return 更新结果
     */
    boolean updateBrand(Brand brand);

    /**
     * 删除品牌
     *
     * @param id 品牌ID
     * @return 删除结果
     */
    boolean deleteBrand(Integer id);
}