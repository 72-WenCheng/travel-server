package com.zhly.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的管理端接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOperationLog {
    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型，例如 CREATE/UPDATE/DELETE/TEST
     */
    String type() default "OPERATE";

    /**
     * 操作描述
     */
    String description() default "";
}

































