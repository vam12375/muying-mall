package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.SysMenu;
import com.muyingmall.mapper.SysMenuMapper;
import com.muyingmall.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统菜单服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        // 查询所有启用的菜单，按排序值排序
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));
        
        // 构建树形结构
        return buildMenuTree(allMenus, 0);
    }

    /**
     * 递归构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> allMenus, Integer parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                // 递归查找子菜单
                List<SysMenu> children = buildMenuTree(allMenus, menu.getId());
                menu.setChildren(children.isEmpty() ? null : children);
                tree.add(menu);
            }
        }
        return tree;
    }

    @Override
    public boolean addMenu(SysMenu menu) {
        // 设置默认值
        if (menu.getParentId() == null) {
            menu.setParentId(0);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        return save(menu);
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        return updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Integer id) {
        // 删除子菜单
        remove(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        // 删除当前菜单
        return removeById(id);
    }

    @Override
    public boolean toggleVisible(Integer id) {
        SysMenu menu = getById(id);
        if (menu == null) {
            return false;
        }
        menu.setVisible(menu.getVisible() == 1 ? 0 : 1);
        return updateById(menu);
    }

    @Override
    public boolean updateSort(Integer id, Integer sort) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setSort(sort);
        return updateById(menu);
    }
}
