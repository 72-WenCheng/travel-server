package com.zhly.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 最小化测试控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api")
public class MinimalController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "旅游系统运行正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "旅游系统");
        result.put("version", "1.0.0");
        result.put("description", "智能旅游攻略平台");
        result.put("status", "运行中");
        return result;
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "测试接口正常");
        result.put("data", "Hello Travel System!");
        result.put("success", true);
        return result;
    }
}



















