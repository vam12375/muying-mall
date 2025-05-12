package com.muyingmall.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页数据封装类
 */
public class CommonPage<T> {

    /**
     * 将MyBatis Plus分页结果转化为通用结果
     *
     * @param page MyBatis Plus分页结果
     * @return 通用分页结果
     */
    public static <T> Map<String, Object> restPage(Page<T> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());
        return result;
    }

    /**
     * 将MyBatis Plus分页结果转化为通用结果，并对数据进行转换
     *
     * @param page      MyBatis Plus分页结果
     * @param converter 数据转换函数
     * @return 通用分页结果
     */
    public static <T, R> Map<String, Object> restPage(Page<T> page, Function<T, R> converter) {
        Map<String, Object> result = new HashMap<>();
        List<R> convertedList = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        result.put("list", convertedList);
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());
        return result;
    }
}