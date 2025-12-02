package com.zhly.security;

import com.zhly.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一管理安全配置并提供轻量缓存，避免每次请求都访问数据库
 */
@Service
public class SecurityPolicyService {

    private static final long CACHE_TTL_MILLIS = 60_000L;

    private final SystemConfigService systemConfigService;
    private final AtomicReference<SecurityPolicy> cache = new AtomicReference<>();
    private volatile long lastLoadedTimestamp = 0L;

    public SecurityPolicyService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 获取安全策略，如果缓存过期则自动刷新
     */
    public SecurityPolicy getPolicy() {
        long now = System.currentTimeMillis();
        SecurityPolicy policy = cache.get();
        if (policy == null || now - lastLoadedTimestamp > CACHE_TTL_MILLIS) {
            policy = loadPolicy();
            cache.set(policy);
            lastLoadedTimestamp = now;
        }
        return policy;
    }

    /**
     * 主动刷新缓存（配置更新后调用）
     */
    public void refresh() {
        cache.set(loadPolicy());
        lastLoadedTimestamp = System.currentTimeMillis();
    }

    private SecurityPolicy loadPolicy() {
        Map<String, Object> data = systemConfigService.getSecurityConfig();
        return new SecurityPolicy(
                parseInt(data.get("passwordMinLength"), 6, 4, 64),
                parseBoolean(data.get("enableLoginLock")),
                parseInt(data.get("maxLoginAttempts"), 5, 1, 20),
                parseInt(data.get("lockTime"), 30, 1, 720),
                parseInt(data.get("sessionTimeout"), 120, 5, 1440),
                parseBoolean(data.get("allowMultiLogin")),
                parseBoolean(data.get("enableApiRateLimit")),
                parseInt(data.get("apiRateLimit"), 100, 10, 10_000)
        );
    }

    private boolean parseBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(value.toString()) || "1".equals(value.toString());
    }

    private int parseInt(Object value, int fallback, int min, int max) {
        int result = fallback;
        if (value instanceof Number) {
            result = ((Number) value).intValue();
        } else if (value != null) {
            try {
                result = Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (result < min) {
            result = min;
        }
        if (result > max) {
            result = max;
        }
        return result;
    }
}

































