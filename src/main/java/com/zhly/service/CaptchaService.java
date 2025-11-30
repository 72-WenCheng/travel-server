package com.zhly.service;

/**
 * 验证码服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface CaptchaService {
    
    /**
     * 存储验证码
     * @param email 邮箱
     * @param captcha 验证码
     * @param expireSeconds 过期时间（秒）
     */
    void storeCaptcha(String email, String captcha, int expireSeconds);
    
    /**
     * 验证验证码
     * @param email 邮箱
     * @param captcha 验证码
     * @return 是否验证成功
     */
    boolean verifyCaptcha(String email, String captcha);
    
    /**
     * 删除验证码
     * @param email 邮箱
     */
    void removeCaptcha(String email);
}





