package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.user.service.UserPortraitService;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户画像控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user/portrait")
public class UserPortraitController {
    
    @Autowired
    private UserPortraitService userPortraitService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取用户画像分析数据
     * 
     * @param authHeader Authorization header，包含JWT token
     * @return 用户画像数据
     */
    @GetMapping
    public Result<Map<String, Object>> getUserPortrait(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            Map<String, Object> portrait = userPortraitService.getUserPortrait(userId);
            return Result.success("获取用户画像成功", portrait);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户画像失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取用户画像（管理员接口）
     * 
     * @param userId 用户ID
     * @return 用户画像数据
     */
    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getUserPortraitById(@PathVariable Long userId) {
        try {
            Map<String, Object> portrait = userPortraitService.getUserPortrait(userId);
            return Result.success("获取用户画像成功", portrait);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户画像失败: " + e.getMessage());
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            if (jwtUtil.validateToken(jwtToken)) {
                return jwtUtil.getUserIdFromToken(jwtToken);
            }
        }
        return null;
    }
}













































































