package com.zhly.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 自定义验证器
 * 
 * @author zhly
 * @since 2024-01-01
 */
public class Validators {
    
    /**
     * 手机号验证器
     */
    public static class PhoneValidator implements ConstraintValidator<Phone, String> {
        private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true; // 空值由@NotNull处理
            }
            return PHONE_PATTERN.matcher(value).matches();
        }
    }
    
    /**
     * 邮箱验证器
     */
    public static class EmailValidator implements ConstraintValidator<Email, String> {
        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true; // 空值由@NotNull处理
            }
            return EMAIL_PATTERN.matcher(value).matches();
        }
    }
    
    /**
     * 密码强度验证器
     */
    public static class PasswordValidator implements ConstraintValidator<Password, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return false;
            }
            // 密码长度至少8位，包含字母和数字
            return value.length() >= 8 && 
                   value.matches(".*[a-zA-Z].*") && 
                   value.matches(".*\\d.*");
        }
    }
}


















