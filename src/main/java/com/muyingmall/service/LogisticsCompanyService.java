package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.LogisticsCompany;

import java.util.List;

/**
 * 物流公司服务接口
 */
public interface LogisticsCompanyService extends IService<LogisticsCompany> {

    /**
     * 分页获取物流公司列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @return 物流公司分页列表
     */
    Page<LogisticsCompany> getCompanyList(int page, int pageSize, String keyword);

    /**
     * 根据ID获取物流公司详情
     *
     * @param id 物流公司ID
     * @return 物流公司详情
     */
    LogisticsCompany getCompanyById(Integer id);

    /**
     * 添加物流公司
     *
     * @param company 物流公司信息
     * @return 是否添加成功
     */
    boolean addCompany(LogisticsCompany company);

    /**
     * 更新物流公司信息
     *
     * @param company 物流公司信息
     * @return 是否更新成功
     */
    boolean updateCompany(LogisticsCompany company);

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 是否删除成功
     */
    boolean deleteCompany(Integer id);

    /**
     * 获取所有启用的物流公司列表
     *
     * @return 物流公司列表
     */
    List<LogisticsCompany> getAllEnabledCompanies();

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司信息
     */
    LogisticsCompany getCompanyByCode(String code);

    /**
     * 根据名称获取物流公司
     *
     * @param name 物流公司名称
     * @return 物流公司信息
     */
    LogisticsCompany getByName(String name);

    /**
     * 获取启用的物流公司列表
     *
     * @return 启用的物流公司列表
     */
    List<LogisticsCompany> getEnabledCompanies();
}