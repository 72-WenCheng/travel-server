package com.zhly.service.impl;

import com.zhly.entity.SystemConfig;
import com.zhly.mapper.SystemConfigMapper;
import com.zhly.service.SystemConfigService;
import com.zhly.config.AiConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Autowired
    private AiConfig aiConfig;
    
    @Value("${system.info.version:1.0.0}")
    private String systemVersion;
    
    private String getConfigValueOrDefault(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public String getConfigValue(String configKey) {
        try {
            QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
            wrapper.eq("config_key", configKey);
            
            SystemConfig config = systemConfigMapper.selectOne(wrapper);
            return config != null ? config.getConfigValue() : null;
        } catch (Exception e) {
            throw new RuntimeException("获取配置值失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean setConfigValue(String configKey, String configValue) {
        try {
            QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
            wrapper.eq("config_key", configKey);
            
            SystemConfig config = systemConfigMapper.selectOne(wrapper);
            if (config != null) {
                config.setConfigValue(configValue);
                return systemConfigMapper.updateById(config) > 0;
            } else {
                // 创建新配置
                config = new SystemConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setConfigType("custom");
                config.setDescription("自定义配置");
                return systemConfigMapper.insert(config) > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("设置配置值失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<SystemConfig> getConfigList(String configType) {
        try {
            QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
            if (configType != null && !configType.isEmpty()) {
                wrapper.eq("config_type", configType);
            }
            wrapper.orderByAsc("config_key");
            
            return systemConfigMapper.selectList(wrapper);
        } catch (Exception e) {
            throw new RuntimeException("获取配置列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean batchUpdateConfigs(Map<String, String> configs) {
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                setConfigValue(entry.getKey(), entry.getValue());
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量更新配置失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getAiConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 直接从AiConfig Bean读取（从application.yml读取）
            config.put("modelName", aiConfig.getModelName());
            config.put("apiUrl", aiConfig.getApiUrl());
            config.put("apiKey", ""); // 出于安全考虑，不返回密钥
            config.put("maxTokens", aiConfig.getMaxTokens());
            config.put("temperature", aiConfig.getTemperature());
            config.put("enabled", true);
            
            return config;
        } catch (Exception e) {
            // 如果出错，返回默认配置
            Map<String, Object> config = new HashMap<>();
            config.put("modelName", "文心一言");
            config.put("apiUrl", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions");
            config.put("apiKey", "");
            config.put("maxTokens", 2000);
            config.put("temperature", 0.7);
            config.put("enabled", true);
            return config;
        }
    }
    
    @Override
    public boolean updateAiConfig(Map<String, Object> aiConfig) {
        try {
            Map<String, String> configs = new HashMap<>();
            
            if (aiConfig.containsKey("modelName")) {
                configs.put("ai.model_name", aiConfig.get("modelName").toString());
            }
            if (aiConfig.containsKey("apiUrl")) {
                configs.put("ai.api_url", aiConfig.get("apiUrl").toString());
            }
            if (aiConfig.containsKey("apiKey")) {
                configs.put("ai.api_key", aiConfig.get("apiKey").toString());
            }
            if (aiConfig.containsKey("maxTokens")) {
                configs.put("ai.max_tokens", aiConfig.get("maxTokens").toString());
            }
            if (aiConfig.containsKey("temperature")) {
                configs.put("ai.temperature", aiConfig.get("temperature").toString());
            }
            if (aiConfig.containsKey("enabled")) {
                configs.put("ai.enabled", aiConfig.get("enabled").toString());
            }
            
            return batchUpdateConfigs(configs);
        } catch (Exception e) {
            throw new RuntimeException("更新AI配置失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getSystemConfig() {
        try {
            Map<String, Object> systemConfig = new HashMap<>();
            
            String adminPlatformName = getConfigValueOrDefault("system.name.admin", getConfigValueOrDefault("system.name", "智慧生态旅游系统"));
            String userPlatformName = getConfigValueOrDefault("system.name.user", "智慧生态旅游系统");
            String systemDescription = getConfigValueOrDefault("system.description", "智慧生态旅游系统 © 2024 版权所有 | 技术支持：智慧生态科技有限公司");
            String contactEmail = getConfigValueOrDefault("system.contact.email", getConfigValueOrDefault("system.admin_email", "admin@example.com"));
            String contactPhone = getConfigValueOrDefault("system.contact.phone", getConfigValueOrDefault("system.service_phone", "400-123-4567"));
            String contactTime = getConfigValueOrDefault("system.contact.time", "工作日 9:00-18:00");
            String maintenanceMessage = getConfigValueOrDefault("system.maintenance_message", "系统维护中，请稍后再试");
            
            systemConfig.put("systemName", adminPlatformName);
            systemConfig.put("adminPlatformName", adminPlatformName);
            systemConfig.put("userPlatformName", userPlatformName);
            systemConfig.put("systemDescription", systemDescription);
            systemConfig.put("systemVersion", systemVersion);
            systemConfig.put("adminEmail", contactEmail);
            systemConfig.put("servicePhone", contactPhone);
            systemConfig.put("contactEmail", contactEmail);
            systemConfig.put("contactPhone", contactPhone);
            systemConfig.put("contactTime", contactTime);
            systemConfig.put("serviceHours", contactTime);
            systemConfig.put("maintenanceMode", getConfigValueOrDefault("system.maintenance_mode", "false"));
            systemConfig.put("maintenanceMessage", maintenanceMessage);
            
            return systemConfig;
        } catch (Exception e) {
            throw new RuntimeException("获取系统配置失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateSystemConfig(Map<String, Object> systemConfig) {
        try {
            Map<String, String> configs = new HashMap<>();
            
            if (systemConfig.containsKey("systemName")) {
                configs.put("system.name", systemConfig.get("systemName").toString());
            }
            if (systemConfig.containsKey("adminPlatformName")) {
                configs.put("system.name.admin", systemConfig.get("adminPlatformName").toString());
            }
            if (systemConfig.containsKey("userPlatformName")) {
                configs.put("system.name.user", systemConfig.get("userPlatformName").toString());
            }
            if (systemConfig.containsKey("systemDescription")) {
                configs.put("system.description", systemConfig.get("systemDescription").toString());
            }
            if (systemConfig.containsKey("systemVersion")) {
                // 系统版本改为从配置文件中获取，不再允许通过接口更新，保留兼容性
            }
            if (systemConfig.containsKey("adminEmail")) {
                configs.put("system.admin_email", systemConfig.get("adminEmail").toString());
                configs.put("system.contact.email", systemConfig.get("adminEmail").toString());
            }
            if (systemConfig.containsKey("servicePhone")) {
                configs.put("system.service_phone", systemConfig.get("servicePhone").toString());
                configs.put("system.contact.phone", systemConfig.get("servicePhone").toString());
            }
            if (systemConfig.containsKey("contactTime")) {
                configs.put("system.contact.time", systemConfig.get("contactTime").toString());
            }
            if (systemConfig.containsKey("maintenanceMode")) {
                configs.put("system.maintenance_mode", systemConfig.get("maintenanceMode").toString());
            }
            if (systemConfig.containsKey("maintenanceMessage")) {
                configs.put("system.maintenance_message", systemConfig.get("maintenanceMessage").toString());
            }
            
            return batchUpdateConfigs(configs);
        } catch (Exception e) {
            throw new RuntimeException("更新系统配置失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getCacheConfig() {
        try {
            Map<String, Object> cacheConfig = new HashMap<>();

            String cacheType = getConfigValueOrDefault("cache.type", "redis");
            String host = getConfigValueOrDefault("cache.redis.host", "localhost");
            String port = getConfigValueOrDefault("cache.redis.port", "6379");
            String password = getConfigValueOrDefault("cache.redis.password", "");
            String database = getConfigValueOrDefault("cache.redis.database", "0");
            String expireTime = getConfigValueOrDefault("cache.expire.time", "3600");

            cacheConfig.put("type", cacheType);
            cacheConfig.put("host", host);
            cacheConfig.put("port", Integer.parseInt(port));
            cacheConfig.put("password", password);
            cacheConfig.put("database", Integer.parseInt(database));
            cacheConfig.put("expireTime", Integer.parseInt(expireTime));

            return cacheConfig;
        } catch (Exception e) {
            throw new RuntimeException("获取缓存配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateCacheConfig(Map<String, Object> cacheConfig) {
        try {
            Map<String, String> configs = new HashMap<>();

            if (cacheConfig.containsKey("type")) {
                configs.put("cache.type", cacheConfig.get("type").toString());
            }
            if (cacheConfig.containsKey("host")) {
                configs.put("cache.redis.host", cacheConfig.get("host").toString());
            }
            if (cacheConfig.containsKey("port")) {
                configs.put("cache.redis.port", cacheConfig.get("port").toString());
            }
            if (cacheConfig.containsKey("password")) {
                configs.put("cache.redis.password", cacheConfig.get("password").toString());
            }
            if (cacheConfig.containsKey("database")) {
                configs.put("cache.redis.database", cacheConfig.get("database").toString());
            }
            if (cacheConfig.containsKey("expireTime")) {
                configs.put("cache.expire.time", cacheConfig.get("expireTime").toString());
            }

            return batchUpdateConfigs(configs);
        } catch (Exception e) {
            throw new RuntimeException("更新缓存配置失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getSecurityConfig() {
        try {
            Map<String, Object> securityConfig = new HashMap<>();

            securityConfig.put("passwordMinLength", parseInt(getConfigValueOrDefault("security.password.min.length", "6"), 6));
            securityConfig.put("enableLoginLock", parseBoolean(getConfigValueOrDefault("security.login.lock.enabled", "false")));
            securityConfig.put("maxLoginAttempts", parseInt(getConfigValueOrDefault("security.login.max.attempts", "5"), 5));
            securityConfig.put("lockTime", parseInt(getConfigValueOrDefault("security.login.lock.time", "30"), 30));
            securityConfig.put("sessionTimeout", parseInt(getConfigValueOrDefault("security.session.timeout", "120"), 120));
            securityConfig.put("allowMultiLogin", parseBoolean(getConfigValueOrDefault("security.multi.login", "false")));
            securityConfig.put("enableApiRateLimit", parseBoolean(getConfigValueOrDefault("security.api.rate.limit.enabled", "true")));
            securityConfig.put("apiRateLimit", parseInt(getConfigValueOrDefault("security.api.rate.limit", "100"), 100));

            return securityConfig;
        } catch (Exception e) {
            throw new RuntimeException("获取安全配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateSecurityConfig(Map<String, Object> securityConfig) {
        try {
            Map<String, String> configs = new HashMap<>();

            if (securityConfig.containsKey("passwordMinLength")) {
                configs.put("security.password.min.length", securityConfig.get("passwordMinLength").toString());
            }
            if (securityConfig.containsKey("enableLoginLock")) {
                configs.put("security.login.lock.enabled", securityConfig.get("enableLoginLock").toString());
            }
            if (securityConfig.containsKey("maxLoginAttempts")) {
                configs.put("security.login.max.attempts", securityConfig.get("maxLoginAttempts").toString());
            }
            if (securityConfig.containsKey("lockTime")) {
                configs.put("security.login.lock.time", securityConfig.get("lockTime").toString());
            }
            if (securityConfig.containsKey("sessionTimeout")) {
                configs.put("security.session.timeout", securityConfig.get("sessionTimeout").toString());
            }
            if (securityConfig.containsKey("allowMultiLogin")) {
                configs.put("security.multi.login", securityConfig.get("allowMultiLogin").toString());
            }
            if (securityConfig.containsKey("enableApiRateLimit")) {
                configs.put("security.api.rate.limit.enabled", securityConfig.get("enableApiRateLimit").toString());
            }
            if (securityConfig.containsKey("apiRateLimit")) {
                configs.put("security.api.rate.limit", securityConfig.get("apiRateLimit").toString());
            }

            return batchUpdateConfigs(configs);
        } catch (Exception e) {
            throw new RuntimeException("更新安全配置失败: " + e.getMessage());
        }
    }
}













