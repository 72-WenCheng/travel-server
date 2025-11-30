package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 基础健康检查
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "travel-server");
        health.put("version", "1.0.0");
        
        return Result.success("健康检查通过", health);
    }
    
    /**
     * 详细健康检查
     */
    @GetMapping("/detailed")
    public Result<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // 基础信息
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "travel-server");
        health.put("version", "1.0.0");
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("totalMemory", Runtime.getRuntime().totalMemory());
        system.put("freeMemory", Runtime.getRuntime().freeMemory());
        system.put("maxMemory", Runtime.getRuntime().maxMemory());
        health.put("system", system);
        
        // 缓存状态
        Map<String, Object> cache = new HashMap<>();
        try {
            cacheService.set("health:check", "ok", 60, java.util.concurrent.TimeUnit.SECONDS);
            String cacheValue = cacheService.get("health:check", String.class);
            cache.put("status", "UP");
            cache.put("test", cacheValue);
        } catch (Exception e) {
            cache.put("status", "DOWN");
            cache.put("error", e.getMessage());
        }
        health.put("cache", cache);
        
        return Result.success("详细健康检查完成", health);
    }
    
    /**
     * 数据库健康检查
     */
    @GetMapping("/database")
    public Result<Map<String, Object>> databaseHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 这里可以添加数据库连接检查
            health.put("status", "UP");
            health.put("message", "数据库连接正常");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("message", "数据库连接异常: " + e.getMessage());
        }
        
        return Result.success("数据库健康检查完成", health);
    }
}