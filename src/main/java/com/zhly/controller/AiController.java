package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.AiService;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * AI控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 生成旅游攻略
     */
    @PostMapping("/generate-plan")
    public Result<Map<String, Object>> generateTravelPlan(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> params) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            // 如果没有用户ID，使用默认值
            if (userId == null) {
                userId = 1L;
            }
            
            // 获取请求参数
            String destination = (String) params.get("destination");
            Integer days = (Integer) params.get("days");
            String budget = (String) params.get("budget");
            String interests = (String) params.get("interests");
            String travelStyle = (String) params.get("travelStyle");
            
            // 构建AI请求
            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append("请为我生成一个").append(days).append("天的").append(destination).append("旅游攻略。");
            requestBuilder.append("预算：").append(budget).append("。");
            requestBuilder.append("兴趣爱好：").append(interests).append("。");
            requestBuilder.append("旅游风格：").append(travelStyle).append("。");
            requestBuilder.append("请包含详细的行程安排、景点推荐、美食推荐、住宿建议和交通指南。");
            
            String request = requestBuilder.toString();
            String modelName = "gpt-3.5-turbo";
            
            // 调用AI服务生成内容
            String response = aiService.generateContent(userId, request, modelName);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("plan", response);
            result.put("destination", destination);
            result.put("days", days);
            result.put("budget", budget);
            result.put("interests", interests);
            result.put("travelStyle", travelStyle);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("生成旅游攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 智能问答
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> params) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                userId = 1L;
            }
            
            String question = (String) params.get("question");
            String modelName = "gpt-3.5-turbo";
            
            // 调用AI服务生成回答
            String response = aiService.generateContent(userId, question, modelName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("answer", response);
            result.put("question", question);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("AI问答失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取AI日志列表
     */
    @GetMapping("/logs")
    public Result<Map<String, Object>> getLogs(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                userId = 1L;
            }
            
            Map<String, Object> result = aiService.getLogList(page, size, userId, status);
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("获取AI日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取AI统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                userId = 1L;
            }
            
            Map<String, Object> result = aiService.getUserAiStats(userId);
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("获取AI统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有AI日志列表（管理员专用）
     */
    @GetMapping("/admin/logs")
    public Result<Map<String, Object>> getAllLogs(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 管理员获取所有日志，不限制userId
            Map<String, Object> result = aiService.getLogList(page, size, null, status);
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("获取AI日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有AI统计信息（管理员专用）
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getAllStatistics(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取所有AI统计
            Map<String, Object> result = aiService.getAiStatistics();
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("获取AI统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成AI内容（通用接口）
     */
    @PostMapping("/generate")
    public Result<String> generateContent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam Long userId,
            @RequestParam String request,
            @RequestParam(required = false, defaultValue = "gpt-3.5-turbo") String modelName) {
        try {
            // 验证用户身份
            Long currentUserId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    currentUserId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (currentUserId == null) {
                userId = 1L; // 使用默认管理员ID
            }
            
            // 调用AI服务生成内容
            String response = aiService.generateContent(userId, request, modelName);
            return Result.success(response);
            
        } catch (Exception e) {
            return Result.error("AI生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除AI日志
     */
    @DeleteMapping("/logs/{id}")
    public Result<String> deleteLog(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        try {
            // 验证用户身份
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                if (jwtUtil.validateToken(jwtToken)) {
                    userId = jwtUtil.getUserIdFromToken(jwtToken);
                }
            }
            
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = aiService.deleteLog(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            return Result.error("删除AI日志失败: " + e.getMessage());
        }
    }
}






