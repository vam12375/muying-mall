package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.LogisticsCompany;
import com.muyingmall.mapper.LogisticsCompanyMapper;
import com.muyingmall.service.LogisticsCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 物流公司服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsCompanyServiceImpl extends ServiceImpl<LogisticsCompanyMapper, LogisticsCompany>
        implements LogisticsCompanyService {

    /**
     * 分页获取物流公司列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @return 物流公司分页列表
     */
    @Override
    public Page<LogisticsCompany> getCompanyList(int page, int pageSize, String keyword) {
        Page<LogisticsCompany> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();

        // 添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(LogisticsCompany::getName, keyword)
                    .or().like(LogisticsCompany::getCode, keyword);
        }

        // 按排序字段排序
        queryWrapper.orderByAsc(LogisticsCompany::getSortOrder);

        return page(pageParam, queryWrapper);
    }

    /**
     * 根据ID获取物流公司详情
     *
     * @param id 物流公司ID
     * @return 物流公司详情
     */
    @Override
    public LogisticsCompany getCompanyById(Integer id) {
        return getById(id);
    }

    /**
     * 添加物流公司
     *
     * @param company 物流公司信息
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCompany(LogisticsCompany company) {
        // 验证代码是否已存在
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getCode, company.getCode());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("物流公司代码已存在");
        }

        return save(company);
    }

    /**
     * 更新物流公司信息
     *
     * @param company 物流公司信息
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCompany(LogisticsCompany company) {
        // 验证代码是否已被其他公司使用
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getCode, company.getCode())
                .ne(LogisticsCompany::getId, company.getId());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("物流公司代码已被其他公司使用");
        }

        return updateById(company);
    }

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCompany(Integer id) {
        // TODO: 检查是否有物流记录关联该物流公司，如有则不允许删除
        return removeById(id);
    }

    /**
     * 获取所有启用的物流公司列表
     *
     * @return 物流公司列表
     */
    @Override
    public List<LogisticsCompany> getAllEnabledCompanies() {
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getStatus, 1);
        queryWrapper.orderByAsc(LogisticsCompany::getSortOrder);
        return list(queryWrapper);
    }

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司信息
     */
    @Override
    public LogisticsCompany getCompanyByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }

        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getCode, code);
        return getOne(queryWrapper);
    }

    /**
     * 根据名称获取物流公司
     *
     * @param name 物流公司名称
     * @return 物流公司信息
     */
    @Override
    public LogisticsCompany getByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }

        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getName, name);
        return getOne(queryWrapper);
    }

    /**
     * 获取启用的物流公司列表
     *
     * @return 启用的物流公司列表
     */
    @Override
    public List<LogisticsCompany> getEnabledCompanies() {
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsCompany::getStatus, 1);
        queryWrapper.orderByAsc(LogisticsCompany::getSortOrder);
        return list(queryWrapper);
    }
}