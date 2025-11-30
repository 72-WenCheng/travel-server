package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.SystemConfig;
import com.zhly.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取配置值
     */
    @GetMapping("/value/{configKey}")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        try {
            String value = systemConfigService.getConfigValue(configKey);
            return Result.success("获取配置值成功", value);
        } catch (Exception e) {
            return Result.error("获取配置值失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置配置值
     */
    @PostMapping("/set")
    public Result<String> setConfigValue(
            @RequestParam String configKey,
            @RequestParam String configValue) {
        try {
            boolean success = systemConfigService.setConfigValue(configKey, configValue);
            if (success) {
                return Result.success("设置配置值成功");
            } else {
                return Result.error("设置配置值失败");
            }
        } catch (Exception e) {
            return Result.error("设置配置值失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置列表
     */
    @GetMapping("/list")
    public Result<List<SystemConfig>> getConfigList(@RequestParam(required = false) String configType) {
        try {
            List<SystemConfig> configs = systemConfigService.getConfigList(configType);
            return Result.success("获取配置列表成功", configs);
        } catch (Exception e) {
            return Result.error("获取配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新配置
     */
    @PostMapping("/batch-update")
    public Result<String> batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        try {
            boolean success = systemConfigService.batchUpdateConfigs(configs);
            if (success) {
                return Result.success("批量更新配置成功");
            } else {
                return Result.error("批量更新配置失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取AI配置
     */
    @GetMapping("/ai")
    public Result<Map<String, Object>> getAiConfig() {
        try {
            Map<String, Object> result = systemConfigService.getAiConfig();
            return Result.success("获取AI配置成功", result);
        } catch (Exception e) {
            return Result.error("获取AI配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新AI配置
     */
    @PostMapping("/ai")
    public Result<String> updateAiConfig(@RequestBody Map<String, Object> aiConfig) {
        try {
            boolean success = systemConfigService.updateAiConfig(aiConfig);
            if (success) {
                return Result.success("更新AI配置成功");
            } else {
                return Result.error("更新AI配置失败");
            }
        } catch (Exception e) {
            return Result.error("更新AI配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统配置
     */
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemConfig() {
        try {
            Map<String, Object> result = systemConfigService.getSystemConfig();
            return Result.success("获取系统配置成功", result);
        } catch (Exception e) {
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置
     */
    @PostMapping("/system")
    public Result<String> updateSystemConfig(@RequestBody Map<String, Object> systemConfig) {
        try {
            boolean success = systemConfigService.updateSystemConfig(systemConfig);
            if (success) {
                return Result.success("更新系统配置成功");
            } else {
                return Result.error("更新系统配置失败");
            }
        } catch (Exception e) {
            return Result.error("更新系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取安全配置（公共接口，仅返回必要字段）
     */
    @GetMapping("/security")
    public Result<Map<String, Object>> getSecurityConfig() {
        try {
            Map<String, Object> result = systemConfigService.getSecurityConfig();
            return Result.success("获取安全配置成功", result);
        } catch (Exception e) {
            return Result.error("获取安全配置失败: " + e.getMessage());
        }
    }
}



















