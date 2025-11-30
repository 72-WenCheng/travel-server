package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.UserCollectService;
import com.zhly.service.TravelPlanService;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user-collect")
public class UserCollectController {
    
    @Autowired
    private UserCollectService userCollectService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<String> addCollect(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam Integer collectType,
            @RequestParam Long collectId) {
        try {
            // 从token获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = userCollectService.addCollect(userId, collectType, collectId);
            if (success) {
                // 如果是攻略收藏（collectType=1），更新攻略的收藏数
                if (collectType == 1) {
                    travelPlanService.updatePlanStats(collectId, "collect");
                }
                return Result.success("添加收藏成功");
            } else {
                return Result.error("添加收藏失败（可能已收藏）");
            }
        } catch (Exception e) {
            return Result.error("添加收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public Result<String> removeCollect(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam Integer collectType,
            @RequestParam Long collectId) {
        try {
            // 从token获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = userCollectService.removeCollect(userId, collectType, collectId);
            if (success) {
                // 如果是攻略收藏（collectType=1），更新攻略的收藏数（减1）
                if (collectType == 1) {
                    userCollectService.decreasePlanCollectCount(collectId);
                }
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> isCollected(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam Integer collectType,
            @RequestParam Long collectId) {
        try {
            // 从token获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.success("用户未登录", false);
            }
            
            boolean isCollected = userCollectService.isCollected(userId, collectType, collectId);
            return Result.success("检查收藏状态成功", isCollected);
        } catch (Exception e) {
            return Result.error("检查收藏状态失败: " + e.getMessage());
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
    
    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserCollects(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer collectType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = userCollectService.getUserCollects(userId, collectType, page, size);
            return Result.success("获取用户收藏成功", result);
        } catch (Exception e) {
            return Result.error("获取用户收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户收藏统计
     */
    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getUserCollectStats(@PathVariable Long userId) {
        try {
            Map<String, Object> result = userCollectService.getUserCollectStats(userId);
            return Result.success("获取用户收藏统计成功", result);
        } catch (Exception e) {
            return Result.error("获取用户收藏统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除收藏
     */
    @PostMapping("/batch-remove")
    public Result<String> batchRemoveCollects(
            @RequestParam Long userId,
            @RequestBody List<Long> collectIds) {
        try {
            boolean success = userCollectService.batchRemoveCollects(userId, collectIds);
            if (success) {
                return Result.success("批量删除收藏成功");
            } else {
                return Result.error("批量删除收藏失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除收藏失败: " + e.getMessage());
        }
    }
}



















