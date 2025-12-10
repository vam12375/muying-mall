package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.SysMenu;

import java.util.List;

/**
 * 系统菜单服务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取树形菜单列表
     */
    List<SysMenu> getMenuTree();

    /**
     * 新增菜单
     */
    boolean addMenu(SysMenu menu);

    /**
     * 更新菜单
     */
    boolean updateMenu(SysMenu menu);

    /**
     * 删除菜单（包含子菜单）
     */
    boolean deleteMenu(Integer id);

    /**
     * 切换菜单可见性
     */
    boolean toggleVisible(Integer id);

    /**
     * 更新菜单排序
     */
    boolean updateSort(Integer id, Integer sort);
}
