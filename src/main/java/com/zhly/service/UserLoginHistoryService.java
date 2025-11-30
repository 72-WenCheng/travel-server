package com.zhly.service;

/**
 * 用户登录历史服务接口
 */
public interface UserLoginHistoryService {
    
    /**
     * 记录登录历史
     */
    void recordLoginHistory(Long userId, String loginIp, String userAgent, Integer status, String failReason);
}

