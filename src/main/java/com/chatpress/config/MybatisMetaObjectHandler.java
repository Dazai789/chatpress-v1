package com.chatpress.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        // Fallback: set directly if strict mode doesn't work (e.g., null values)
        if (metaObject.hasSetter("createdAt") && metaObject.getValue("createdAt") == null) {
            metaObject.setValue("createdAt", now);
        }
        if (metaObject.hasSetter("updatedAt") && metaObject.getValue("updatedAt") == null) {
            metaObject.setValue("updatedAt", now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        if (metaObject.hasSetter("updatedAt")) {
            metaObject.setValue("updatedAt", LocalDateTime.now());
        }
    }
}
