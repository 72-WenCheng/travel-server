package com.zhly.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.GLOBAL;
    
    /**
     * 每秒允许的请求数
     */
    double permitsPerSecond() default 10.0;
    
    /**
     * 限流时间窗口（秒）
     */
    int timeWindow() default 1;
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 全局限流
         */
        GLOBAL,
        
        /**
         * IP限流
         */
        IP,
        
        /**
         * 用户限流
         */
        USER,
        
        /**
         * 自定义限流
         */
        CUSTOM
    }
}


















