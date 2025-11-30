package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.RecommendationService;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能推荐控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/recommendation")
@CrossOrigin(origins = "*")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 推荐景点
     */
    @GetMapping("/attractions")
    public Result<List<Map<String, Object>>> recommendAttractions(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.recommendAttractions(userId, city, limit);
            return Result.success("推荐景点成功", recommendations);
        } catch (Exception e) {
            return Result.error("推荐景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 推荐攻略
     */
    @GetMapping("/plans")
    public Result<List<Map<String, Object>>> recommendPlans(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.recommendPlans(userId, destination, limit);
            return Result.success("推荐攻略成功", recommendations);
        } catch (Exception e) {
            return Result.error("推荐攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据相同/相似地点推荐相关攻略
     */
    @GetMapping("/plans/related")
    public Result<List<Map<String, Object>>> getRelatedPlansByDestination(
            @RequestParam(required = false) Long planId,
            @RequestParam String destination,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Map<String, Object>> recommendations = recommendationService.getRelatedPlansByDestination(planId, destination, limit);
            return Result.success("获取相关攻略成功", recommendations);
        } catch (Exception e) {
            return Result.error("获取相关攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据相同/相似地点推荐相关景点（按热度排序）
     */
    @GetMapping("/attractions/related")
    public Result<List<Map<String, Object>>> getRelatedAttractionsByDestination(
            @RequestParam String destination,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Map<String, Object>> recommendations = recommendationService.getRelatedAttractionsByDestination(destination, limit);
            return Result.success("获取相关景点成功", recommendations);
        } catch (Exception e) {
            return Result.error("获取相关景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 协同过滤推荐
     */
    @GetMapping("/collaborative")
    public Result<List<Map<String, Object>>> collaborativeFiltering(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.collaborativeFiltering(userId, type, limit);
            return Result.success("协同过滤推荐成功", recommendations);
        } catch (Exception e) {
            return Result.error("协同过滤推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 基于内容推荐
     */
    @GetMapping("/content")
    public Result<List<Map<String, Object>>> contentBasedRecommendation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.contentBasedRecommendation(userId, type, limit);
            return Result.success("基于内容推荐成功", recommendations);
        } catch (Exception e) {
            return Result.error("基于内容推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 基于地理位置推荐
     */
    @GetMapping("/location")
    public Result<List<Map<String, Object>>> locationBasedRecommendation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> recommendations = recommendationService.locationBasedRecommendation(latitude, longitude, type, limit);
            return Result.success("基于地理位置推荐成功", recommendations);
        } catch (Exception e) {
            return Result.error("基于地理位置推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 基于时间推荐
     */
    @GetMapping("/time")
    public Result<List<Map<String, Object>>> timeBasedRecommendation(
            @RequestParam String season,
            @RequestParam String weather,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> recommendations = recommendationService.timeBasedRecommendation(season, weather, type, limit);
            return Result.success("基于时间推荐成功", recommendations);
        } catch (Exception e) {
            return Result.error("基于时间推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 混合推荐
     */
    @GetMapping("/hybrid")
    public Result<List<Map<String, Object>>> hybridRecommendation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.hybridRecommendation(userId, type, limit);
            return Result.success("混合推荐成功", recommendations);
        } catch (Exception e) {
            return Result.error("混合推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取推荐解释
     */
    @GetMapping("/explanation")
    public Result<String> getRecommendationExplanation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String itemId,
            @RequestParam String type) {
        try {
            Long userId = getUserIdFromToken(token);
            String explanation = recommendationService.getRecommendationExplanation(userId, itemId, type);
            return Result.success("获取推荐解释成功", explanation);
        } catch (Exception e) {
            return Result.error("获取推荐解释失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户偏好
     */
    @PostMapping("/preferences")
    public Result<String> updateUserPreferences(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> preferences) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = recommendationService.updateUserPreferences(userId, preferences);
            if (success) {
                return Result.success("更新用户偏好成功");
            } else {
                return Result.error("更新用户偏好失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户偏好失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户画像
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getUserProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            Map<String, Object> profile = recommendationService.getUserProfile(userId);
            return Result.success("获取用户画像成功", profile);
        } catch (Exception e) {
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
        return 1L; // 默认用户ID
    }
}






