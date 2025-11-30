package com.zhly.service;

/**
 * 邮件服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param email 邮箱地址（收件人，同时也是发件人显示地址）
     * @param captcha 验证码
     * @return 是否发送成功
     */
    boolean sendCaptchaEmail(String email, String captcha);
    
    /**
     * 发送重置密码邮件
     * @param email 邮箱地址（收件人，同时也是发件人显示地址）
     * @param resetToken 重置token
     * @return 是否发送成功
     */
    boolean sendResetPasswordEmail(String email, String resetToken);
}





