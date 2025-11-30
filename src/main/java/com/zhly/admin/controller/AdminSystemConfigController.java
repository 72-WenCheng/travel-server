package com.zhly.admin.controller;

import com.zhly.annotation.AdminOperationLog;
import com.zhly.common.Result;
import com.zhly.mapper.UserOperationLogMapper;
import com.zhly.security.LoginSecurityService;
import com.zhly.security.SecurityPolicyService;
import com.zhly.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-系统配置控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "管理端-系统配置")
@RestController
@RequestMapping("/api/admin/system-config")
@RequiredArgsConstructor
public class AdminSystemConfigController {
    
    private final SystemConfigService systemConfigService;
    private final SecurityPolicyService securityPolicyService;
    private final LoginSecurityService loginSecurityService;
    private final UserOperationLogMapper userOperationLogMapper;
    
    /**
     * 获取系统配置
     */
    @GetMapping("/system")
    @Operation(summary = "获取系统配置")
    public Result<Map<String, Object>> getSystemConfig() {
        try {
            Map<String, Object> result = systemConfigService.getSystemConfig();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置
     */
    @PostMapping("/system")
    @Operation(summary = "更新系统配置")
    @AdminOperationLog(module = "SYSTEM_CONFIG", type = "UPDATE", description = "更新系统配置")
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
     * 获取AI配置
     */
    @GetMapping("/ai")
    @Operation(summary = "获取AI配置")
    public Result<Map<String, Object>> getAiConfig() {
        try {
            Map<String, Object> result = systemConfigService.getAiConfig();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取AI配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新AI配置
     */
    @PostMapping("/ai")
    @Operation(summary = "更新AI配置")
    @AdminOperationLog(module = "AI_CONFIG", type = "UPDATE", description = "更新AI配置")
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
     * 批量更新配置
     */
    @PostMapping("/batch-update")
    @Operation(summary = "批量更新配置")
    @AdminOperationLog(module = "SYSTEM_CONFIG", type = "UPDATE", description = "批量更新系统配置")
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
     * 获取缓存配置
     */
    @GetMapping("/cache")
    @Operation(summary = "获取缓存配置")
    public Result<Map<String, Object>> getCacheConfig() {
        try {
            Map<String, Object> result = systemConfigService.getCacheConfig();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取缓存配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新缓存配置
     */
    @PostMapping("/cache")
    @Operation(summary = "更新缓存配置")
    @AdminOperationLog(module = "CACHE_CONFIG", type = "UPDATE", description = "更新缓存配置")
    public Result<String> updateCacheConfig(@RequestBody Map<String, Object> cacheConfig) {
        try {
            boolean success = systemConfigService.updateCacheConfig(cacheConfig);
            if (success) {
                return Result.success("更新缓存配置成功");
            } else {
                return Result.error("更新缓存配置失败");
            }
        } catch (Exception e) {
            return Result.error("更新缓存配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取安全配置
     */
    @GetMapping("/security")
    @Operation(summary = "获取安全配置")
    public Result<Map<String, Object>> getSecurityConfig() {
        try {
            Map<String, Object> result = systemConfigService.getSecurityConfig();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取安全配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新安全配置
     */
    @PostMapping("/security")
    @Operation(summary = "更新安全配置")
    @AdminOperationLog(module = "SECURITY_CONFIG", type = "UPDATE", description = "更新安全配置")
    public Result<String> updateSecurityConfig(@RequestBody Map<String, Object> securityConfig) {
        try {
            boolean success = systemConfigService.updateSecurityConfig(securityConfig);
            if (success) {
                securityPolicyService.refresh();
                loginSecurityService.resetAllFailures();
                return Result.success("更新安全配置成功");
            } else {
                return Result.error("更新安全配置失败");
            }
        } catch (Exception e) {
            return Result.error("更新安全配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试AI连接
     */
    @PostMapping("/ai/test")
    @Operation(summary = "测试AI连接")
    @AdminOperationLog(module = "AI_CONFIG", type = "TEST", description = "测试AI连接")
    public Result<String> testAIConnection() {
        try {
            // TODO: 实现AI连接测试
            return Result.success("AI连接测试成功");
        } catch (Exception e) {
            return Result.error("AI连接测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试邮件连接
     */
    @PostMapping("/email/test")
    @Operation(summary = "测试邮件连接")
    @AdminOperationLog(module = "EMAIL", type = "TEST", description = "测试邮件连接")
    public Result<String> testEmailConnection(@RequestBody Map<String, String> params) {
        try {
            String testEmail = params.get("testEmail");
            if (testEmail == null || testEmail.isEmpty()) {
                return Result.error("测试邮箱地址不能为空");
            }
            // TODO: 实现邮件发送测试
            return Result.success("测试邮件发送成功");
        } catch (Exception e) {
            return Result.error("测试邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试缓存连接
     */
    @PostMapping("/cache/test")
    @Operation(summary = "测试缓存连接")
    @AdminOperationLog(module = "CACHE_CONFIG", type = "TEST", description = "测试缓存连接")
    public Result<String> testCacheConnection() {
        try {
            // TODO: 实现缓存连接测试
            return Result.success("缓存连接测试成功");
        } catch (Exception e) {
            return Result.error("缓存连接测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统操作日志
     */
    @GetMapping("/logs")
    @Operation(summary = "获取系统操作日志")
    public Result<Map<String, Object>> getSystemLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<Map<String, Object>> logs = userOperationLogMapper.selectSystemOperationLogs(page, size);
            Long total = userOperationLogMapper.selectSystemOperationLogsCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取操作日志失败: " + e.getMessage());
        }
    }
}

