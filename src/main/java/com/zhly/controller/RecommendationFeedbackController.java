package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.impl.RecommendationServiceImpl;
import com.zhly.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 推荐反馈控制器
 * 用于记录用户对推荐内容的反馈，实现持续学习优化
 */
@RestController
@RequestMapping("/api/recommendation/feedback")
public class RecommendationFeedbackController {
    
    @Autowired
    private RecommendationServiceImpl recommendationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 记录用户反馈
     * @param recommendType 推荐类型（1-景点，2-攻略）
     * @param recommendId 推荐内容ID
     * @param feedbackType 反馈类型（1-点击查看，2-收藏，3-浏览时长>30s，4-不感兴趣，5-不喜欢）
     * @param browseDuration 浏览时长（秒）
     */
    @PostMapping
    public Result<String> recordFeedback(
            @RequestParam Integer recommendType,
            @RequestParam Long recommendId,
            @RequestParam Integer feedbackType,
            @RequestParam(required = false) Integer browseDuration,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            recommendationService.recordUserFeedback(userId, recommendType, recommendId, feedbackType, browseDuration);
            
            return Result.success("反馈记录成功");
        } catch (Exception e) {
            return Result.error("反馈记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 方式1: 从request attribute获取（JwtAuthenticationFilter设置的）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr != null && userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        
        // 方式2: 从Authorization header解析
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        
        return null;
    }
}

