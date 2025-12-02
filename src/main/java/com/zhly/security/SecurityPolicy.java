package com.zhly.security;

/**
 * 封装后台安全配置，便于在业务中注入使用
 */
public class SecurityPolicy {

    private final int passwordMinLength;
    private final boolean loginLockEnabled;
    private final int maxLoginAttempts;
    private final int lockTimeMinutes;
    private final int sessionTimeoutMinutes;
    private final boolean allowMultiLogin;
    private final boolean apiRateLimitEnabled;
    private final int apiRateLimitPerMinute;

    public SecurityPolicy(int passwordMinLength,
                          boolean loginLockEnabled,
                          int maxLoginAttempts,
                          int lockTimeMinutes,
                          int sessionTimeoutMinutes,
                          boolean allowMultiLogin,
                          boolean apiRateLimitEnabled,
                          int apiRateLimitPerMinute) {
        this.passwordMinLength = passwordMinLength;
        this.loginLockEnabled = loginLockEnabled;
        this.maxLoginAttempts = maxLoginAttempts;
        this.lockTimeMinutes = lockTimeMinutes;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.allowMultiLogin = allowMultiLogin;
        this.apiRateLimitEnabled = apiRateLimitEnabled;
        this.apiRateLimitPerMinute = apiRateLimitPerMinute;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public boolean isLoginLockEnabled() {
        return loginLockEnabled;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public int getLockTimeMinutes() {
        return lockTimeMinutes;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public boolean isAllowMultiLogin() {
        return allowMultiLogin;
    }

    public boolean isApiRateLimitEnabled() {
        return apiRateLimitEnabled;
    }

    public int getApiRateLimitPerMinute() {
        return apiRateLimitPerMinute;
    }
}

































