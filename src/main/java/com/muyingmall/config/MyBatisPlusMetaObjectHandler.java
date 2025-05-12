package com.muyingmall.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充字段处理器
 * 用于自动填充 @TableField(fill = FieldFill.INSERT) 和 @TableField(fill =
 * FieldFill.INSERT_UPDATE) 注解的字段
 */
@Slf4j
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ...");
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 同时也填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ...");
        // 只需更新 updateTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}