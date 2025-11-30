package com.zhly.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 密码强度验证注解
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Documented
@Constraint(validatedBy = Validators.PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "密码强度不够，至少8位且包含字母和数字";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


















