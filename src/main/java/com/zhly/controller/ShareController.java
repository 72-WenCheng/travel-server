package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.IUserPointsService;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.entity.PointsLog;
import com.zhly.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分享控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/share")
public class ShareController {
    
    @Autowired
    private IUserPointsService userPointsService;
    
    @Autowired(required = false)
    private PointsLogMapper pointsLogMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 记录分享并奖励积分
     * @param contentType 内容类型：plan/attraction/culture
     * @param contentId 内容ID
     * @param token 用户token
     */
    @PostMapping("/record")
    public Result<String> recordShare(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查是否已经给过积分（避免重复奖励）
            if (pointsLogMapper != null) {
                QueryWrapper<PointsLog> pointsLogWrapper = new QueryWrapper<>();
                pointsLogWrapper.eq("user_id", userId)
                               .eq("action_type", 6)  // 6-分享
                               .eq("related_type", contentType)
                               .eq("related_id", contentId);
                Long existingLogCount = pointsLogMapper.selectCount(pointsLogWrapper);
                
                if (existingLogCount > 0) {
                    return Result.success("分享成功（已获得过积分）");
                }
            }
            
            // 给用户奖励3积分
            if (userPointsService != null) {
                try {
                    userPointsService.addPoints(
                        userId,
                        3,  // 奖励3积分
                        6,  // 行为类型：6-分享
                        "分享内容",
                        contentType,  // 关联类型：plan/attraction/culture
                        contentId     // 关联ID
                    );
                    System.out.println("✅ 用户 " + userId + " 分享成功，已奖励3积分，类型: " + contentType + ", ID: " + contentId);
                    return Result.success("分享成功，已获得3积分");
                } catch (Exception e) {
                    System.err.println("⚠️ 分享成功，但积分奖励失败: " + e.getMessage());
                    e.printStackTrace();
                    return Result.success("分享成功（积分奖励失败）");
                }
            }
            
            return Result.success("分享成功");
        } catch (Exception e) {
            return Result.error("分享失败: " + e.getMessage());
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







































































