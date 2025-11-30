package com.zhly.service;

import com.zhly.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 设置配置值
     */
    boolean setConfigValue(String configKey, String configValue);
    
    /**
     * 获取配置列表
     */
    List<SystemConfig> getConfigList(String configType);
    
    /**
     * 批量更新配置
     */
    boolean batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 获取AI配置
     */
    Map<String, Object> getAiConfig();
    
    /**
     * 更新AI配置
     */
    boolean updateAiConfig(Map<String, Object> aiConfig);
    
    /**
     * 获取系统配置
     */
    Map<String, Object> getSystemConfig();
    
    /**
     * 更新系统配置
     */
    boolean updateSystemConfig(Map<String, Object> systemConfig);

    /**
     * 获取缓存配置
     */
    Map<String, Object> getCacheConfig();

    /**
     * 更新缓存配置
     */
    boolean updateCacheConfig(Map<String, Object> cacheConfig);

    /**
     * 获取安全配置
     */
    Map<String, Object> getSecurityConfig();

    /**
     * 更新安全配置
     */
    boolean updateSecurityConfig(Map<String, Object> securityConfig);
}



















