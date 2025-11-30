package com.zhly.user.service;

import java.util.Map;

/**
 * 用户画像服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserPortraitService {
    
    /**
     * 获取用户画像分析数据
     * 
     * @param userId 用户ID
     * @return 用户画像数据，包含浏览统计、偏好分析、兴趣标签、消费行为、出行特征等
     */
    Map<String, Object> getUserPortrait(Long userId);
}














































































