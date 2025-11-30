package com.zhly.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流配置类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
public class RateLimitConfig {
    
    /**
     * 全局限流器 - 每秒100个请求
     */
    @Bean
    public RateLimiter globalRateLimiter() {
        return RateLimiter.create(100.0);
    }
    
    /**
     * API限流器 - 每秒50个请求
     */
    @Bean
    public RateLimiter apiRateLimiter() {
        return RateLimiter.create(50.0);
    }
    
    /**
     * 用户限流器 - 每秒10个请求
     */
    @Bean
    public RateLimiter userRateLimiter() {
        return RateLimiter.create(10.0);
    }
    
    /**
     * 用户限流器映射
     */
    @Bean
    public ConcurrentHashMap<String, RateLimiter> userRateLimiters() {
        return new ConcurrentHashMap<>();
    }
    
    /**
     * 获取用户限流器
     */
    public RateLimiter getUserRateLimiter(String userId) {
        return userRateLimiters().computeIfAbsent(userId, k -> 
            RateLimiter.create(10.0, 1, TimeUnit.SECONDS)
        );
    }
}


















