package com.zhly.service;

import com.zhly.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * 用户服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     * @param user 用户信息
     * @param inviteCode 邀请码（可选）
     */
    boolean register(User user, String inviteCode);
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password, String loginType);
    
    /**
     * 用户登出
     */
    boolean logout(Long userId);
    
    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    boolean resetPassword(String email, String newPassword);
    
    /**
     * 发送验证码
     */
    boolean sendCaptcha(String email);
    
    /**
     * 忘记密码
     */
    boolean forgotPassword(String email, String captcha);
    
    /**
     * 通过token重置密码
     */
    boolean resetPasswordByToken(String token, String newPassword);
    
    /**
     * 更新用户信息
     */
    boolean updateUserInfo(User user);
    
    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);
    
    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);
    
    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone);
    
    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStatistics();
}


