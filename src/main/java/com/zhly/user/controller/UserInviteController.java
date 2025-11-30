package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.service.UserInviteService;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户邀请控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user/invite")
public class UserInviteController {
    
    @Autowired
    private UserInviteService userInviteService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取邀请码和统计信息
     */
    @GetMapping("/code")
    public Result<Map<String, Object>> getInviteCode(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> inviteInfo = userInviteService.getInviteInfo(userId);
            return Result.success("获取邀请信息成功", inviteInfo);
        } catch (Exception e) {
            return Result.error("获取邀请信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    return jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return null;
    }
}







































































