package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.User;
import com.zhly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, Object> registerRequest) {
        System.out.println("=== 用户注册请求 ===");
        
        // 从请求中提取用户信息和邀请码
        User user = new User();
        user.setUsername((String) registerRequest.get("username"));
        user.setPassword((String) registerRequest.get("password"));
        user.setEmail((String) registerRequest.get("email"));
        user.setPhone((String) registerRequest.get("phone"));
        user.setNickname((String) registerRequest.get("nickname"));
        
        String inviteCode = (String) registerRequest.get("inviteCode");
        
        System.out.println("用户名: " + user.getUsername());
        System.out.println("邮箱: " + user.getEmail());
        System.out.println("手机号: " + user.getPhone());
        System.out.println("邀请码: " + inviteCode);
        
        try {
            boolean success = userService.register(user, inviteCode);
            if (success) {
                System.out.println("注册成功");
                return Result.success("注册成功");
            } else {
                System.out.println("注册失败");
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            System.out.println("注册异常: " + e.getMessage());
            // 让全局异常处理器处理异常，不在这里包装
            throw e;
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String loginType = loginRequest.get("loginType"); // 获取登录类型：user或admin
        try {
            Map<String, Object> result = userService.login(username, password, loginType);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            // 让全局异常处理器处理异常，不在这里包装
            throw e;
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestBody Map<String, Long> logoutRequest) {
        Long userId = logoutRequest.get("userId");
        try {
            boolean success = userService.logout(userId);
            if (success) {
                return Result.success("登出成功");
            } else {
                return Result.error("登出失败");
            }
        } catch (Exception e) {
            return Result.error("登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-captcha")
    public Result<String> sendCaptcha(@RequestBody Map<String, String> captchaRequest) {
        String email = captchaRequest.get("email");
        try {
            boolean success = userService.sendCaptcha(email);
            if (success) {
                return Result.success("验证码已发送");
            } else {
                return Result.error("发送验证码失败");
            }
        } catch (Exception e) {
            // 让全局异常处理器处理异常，不在这里包装
            throw e;
        }
    }
    
    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> forgotRequest) {
        String email = forgotRequest.get("email");
        String captcha = forgotRequest.get("captcha");
        try {
            boolean success = userService.forgotPassword(email, captcha);
            if (success) {
                return Result.success("重置密码链接已发送到您的邮箱");
            } else {
                return Result.error("重置密码失败");
            }
        } catch (Exception e) {
            // 让全局异常处理器处理异常，不在这里包装
            throw e;
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> resetRequest) {
        String token = resetRequest.get("token");
        String newPassword = resetRequest.get("newPassword");
        try {
            boolean success = userService.resetPasswordByToken(token, newPassword);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            // 让全局异常处理器处理异常，不在这里包装
            throw e;
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, Object> changeRequest) {
        Long userId = Long.valueOf(changeRequest.get("userId").toString());
        String oldPassword = changeRequest.get("oldPassword").toString();
        String newPassword = changeRequest.get("newPassword").toString();
        try {
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                return Result.success("修改密码成功");
            } else {
                return Result.error("修改密码失败");
            }
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/user/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        try {
            User user = userService.getUserInfo(userId);
            if (user == null) {
                return Result.notFound("用户不存在");
            }
            return Result.success("获取用户信息成功", user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/user/{userId}")
    public Result<String> updateUserInfo(@PathVariable Long userId, @RequestBody User user) {
        try {
            user.setId(userId);
            boolean success = userService.updateUserInfo(user);
            if (success) {
                return Result.success("更新用户信息成功");
            } else {
                return Result.error("更新用户信息失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            return Result.success("检查用户名成功", exists);
        } catch (Exception e) {
            return Result.error("检查用户名失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.checkEmailExists(email);
            return Result.success("检查邮箱成功", exists);
        } catch (Exception e) {
            return Result.error("检查邮箱失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            return Result.success("检查手机号成功", exists);
        } catch (Exception e) {
            return Result.error("检查手机号失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> result = userService.getUserStatistics();
            return Result.success("获取用户统计成功", result);
        } catch (Exception e) {
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }
}


