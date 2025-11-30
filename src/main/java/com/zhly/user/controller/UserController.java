package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.user.service.UserService;
import com.zhly.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户端用户控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping({"/api/user", "/user"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        try {
            Map<String, Object> result = userService.register(user);
            return Result.success("注册成功", result);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            Map<String, Object> result = userService.login(username, password);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            User user = userService.getUserByToken(token);
            return Result.success("获取用户信息成功", user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<String> updateUserInfo(@RequestHeader("Authorization") String authHeader, @RequestBody User user) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            userService.updateUserInfo(token, user);
            return Result.success("更新用户信息成功");
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 设置用户标签
     */
    @PostMapping("/tags")
    public Result<String> setUserTags(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> tags) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            userService.setUserTags(token, tags);
            return Result.success("设置用户标签成功");
        } catch (Exception e) {
            return Result.error("设置用户标签失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("Authorization") String authHeader, 
                                                     @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            Map<String, String> result = userService.uploadAvatar(token, file);
            return Result.success("上传头像成功", result);
        } catch (Exception e) {
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestHeader("Authorization") String authHeader, 
                                         @RequestBody Map<String, String> passwordData) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            userService.changePassword(token, oldPassword, newPassword);
            return Result.success("修改密码成功");
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization header中提取token（去掉"Bearer "前缀）
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            userService.logout(token);
            return Result.success("退出登录成功");
        } catch (Exception e) {
            return Result.error("退出登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情（包含画像信息）
     */
    @GetMapping("/detail/{userId}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        try {
            Map<String, Object> result = userService.getUserDetailWithPortrait(userId);
            return Result.success("获取用户详情成功", result);
        } catch (Exception e) {
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 注销账号
     */
    @PostMapping("/deactivate")
    public Result<String> deactivateAccount(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody(required = false) Map<String, String> body) {
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ")
                    ? authHeader.substring(7)
                    : authHeader;
            String reason = body != null ? body.getOrDefault("reason", "") : null;
            userService.deactivateAccount(token, reason);
            return Result.success("注销申请已提交，7天内再次登录会自动取消");
        } catch (Exception e) {
            return Result.error("注销失败: " + e.getMessage());
        }
    }
    
}


