package com.zhly.service.impl;

import com.zhly.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    // 使用内存存储验证码（生产环境应该使用Redis）
    private final Map<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();
    
    @Override
    public void storeCaptcha(String email, String captcha, int expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000L);
        captchaStore.put(email, new CaptchaInfo(captcha, expireTime));
    }
    
    @Override
    public boolean verifyCaptcha(String email, String captcha) {
        CaptchaInfo captchaInfo = captchaStore.get(email);
        if (captchaInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > captchaInfo.getExpireTime()) {
            captchaStore.remove(email);
            return false;
        }
        
        // 验证码匹配
        boolean matches = captchaInfo.getCaptcha().equals(captcha);
        if (matches) {
            // 验证成功后删除验证码（一次性使用）
            captchaStore.remove(email);
        }
        
        return matches;
    }
    
    @Override
    public void removeCaptcha(String email) {
        captchaStore.remove(email);
    }
    
    /**
     * 验证码信息内部类
     */
    private static class CaptchaInfo {
        private final String captcha;
        private final long expireTime;
        
        public CaptchaInfo(String captcha, long expireTime) {
            this.captcha = captcha;
            this.expireTime = expireTime;
        }
        
        public String getCaptcha() {
            return captcha;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}





