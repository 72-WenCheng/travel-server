package com.zhly.user.service;

import com.zhly.entity.User;
import java.util.Map;

/**
 * 用户端用户服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    Map<String, Object> register(User user);
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 根据token获取用户信息
     */
    User getUserByToken(String token);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(String token, User user);
    
    /**
     * 设置用户标签
     */
    void setUserTags(String token, Map<String, Object> tags);
    
    /**
     * 获取用户标签
     */
    Map<String, Object> getUserTags(String token);
    
    /**
     * 更新用户偏好
     */
    void updateUserPreferences(String token, Map<String, Object> preferences);
    
    /**
     * 获取用户推荐内容
     */
    Object getUserRecommendations(String token);
    
    /**
     * 上传头像
     */
    Map<String, String> uploadAvatar(String token, org.springframework.web.multipart.MultipartFile file);
    
    /**
     * 修改密码
     */
    void changePassword(String token, String oldPassword, String newPassword);
    
    /**
     * 退出登录
     */
    void logout(String token);
    
    /**
     * 获取用户详情（包含画像信息）
     */
    Map<String, Object> getUserDetailWithPortrait(Long userId);

    /**
     * 注销账号
     */
    void deactivateAccount(String token, String reason);
}


