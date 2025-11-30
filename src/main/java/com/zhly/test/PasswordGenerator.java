package com.zhly.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器
 * 用于生成BCrypt加密的密码
 * 
 * @author zhly
 * @since 2024-01-01
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String encoded = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encoded);
        System.out.println("验证结果: " + encoder.matches(password, encoded));
        
        // 生成多个不同的加密结果（每次运行结果都不同，但都能验证通过）
        System.out.println("\n生成多个加密结果:");
        for (int i = 0; i < 3; i++) {
            String encoded2 = encoder.encode(password);
            System.out.println("加密结果 " + (i+1) + ": " + encoded2);
            System.out.println("验证结果 " + (i+1) + ": " + encoder.matches(password, encoded2));
        }
    }
}




