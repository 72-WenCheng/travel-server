package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.SearchService;
import com.zhly.service.impl.SearchServiceImpl;
import com.zhly.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private SearchServiceImpl searchServiceImpl;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 全局搜索
     */
    @GetMapping("/global")
    public Result<Map<String, Object>> globalSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            Map<String, Object> result = searchService.globalSearch(keyword, page, size);
            
            // 计算总结果数并记录搜索日志
            int totalCount = calculateTotalCount(result);
            searchServiceImpl.recordSearchLog(userId, keyword, "global", totalCount);
            
            return Result.success("全局搜索成功", result);
        } catch (Exception e) {
            return Result.error("全局搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索景点
     */
    @GetMapping("/attractions")
    public Result<Map<String, Object>> searchAttractions(
            @RequestParam String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            Map<String, Object> result = searchService.searchAttractions(keyword, city, page, size);
            
            // 记录搜索日志
            int resultCount = result != null && result.get("total") != null ? 
                ((Number) result.get("total")).intValue() : 0;
            searchServiceImpl.recordSearchLog(userId, keyword, "attractions", resultCount);
            
            return Result.success("搜索景点成功", result);
        } catch (Exception e) {
            return Result.error("搜索景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索旅游攻略
     */
    @GetMapping("/travel-plans")
    public Result<Map<String, Object>> searchTravelPlans(
            @RequestParam String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            Map<String, Object> result = searchService.searchTravelPlans(keyword, destination, page, size);
            
            // 记录搜索日志
            int resultCount = result != null && result.get("total") != null ? 
                ((Number) result.get("total")).intValue() : 0;
            searchServiceImpl.recordSearchLog(userId, keyword, "travel-plans", resultCount);
            
            return Result.success("搜索旅游攻略成功", result);
        } catch (Exception e) {
            return Result.error("搜索旅游攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索文旅项目
     */
    @GetMapping("/culture-projects")
    public Result<Map<String, Object>> searchCultureProjects(
            @RequestParam String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            Map<String, Object> result = searchService.searchCultureProjects(keyword, region, page, size);
            
            // 记录搜索日志
            int resultCount = result != null && result.get("total") != null ? 
                ((Number) result.get("total")).intValue() : 0;
            searchServiceImpl.recordSearchLog(userId, keyword, "culture-projects", resultCount);
            
            return Result.success("搜索文旅项目成功", result);
        } catch (Exception e) {
            return Result.error("搜索文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索旅游路线
     */
    @GetMapping("/travel-routes")
    public Result<Map<String, Object>> searchTravelRoutes(
            @RequestParam String keyword,
            @RequestParam(required = false) String startCity,
            @RequestParam(required = false) String endCity,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            Map<String, Object> result = searchService.searchTravelRoutes(keyword, startCity, endCity, page, size);
            
            // 记录搜索日志
            int resultCount = result != null && result.get("total") != null ? 
                ((Number) result.get("total")).intValue() : 0;
            searchServiceImpl.recordSearchLog(userId, keyword, "travel-routes", resultCount);
            
            return Result.success("搜索旅游路线成功", result);
        } catch (Exception e) {
            return Result.error("搜索旅游路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<String> suggestions = searchService.getSearchSuggestions(keyword, limit);
            return Result.success("获取搜索建议成功", suggestions);
        } catch (Exception e) {
            return Result.error("获取搜索建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录搜索历史
     */
    @PostMapping("/history")
    public Result<String> recordSearchHistory(
            @RequestParam Long userId,
            @RequestParam String keyword,
            @RequestParam String searchType) {
        try {
            boolean success = searchService.recordSearchHistory(userId, keyword, searchType);
            if (success) {
                return Result.success("记录搜索历史成功");
            } else {
                return Result.error("记录搜索历史失败");
            }
        } catch (Exception e) {
            return Result.error("记录搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门搜索词
     */
    @GetMapping("/hot-words")
    public Result<List<Map<String, Object>>> getHotSearchWords(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> hotWords = searchService.getHotSearchWords(limit);
            return Result.success("获取热门搜索词成功", hotWords);
        } catch (Exception e) {
            return Result.error("获取热门搜索词失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户搜索历史
     */
    @GetMapping("/user-history/{userId}")
    public Result<List<String>> getUserSearchHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<String> history = searchService.getUserSearchHistory(userId, limit);
            return Result.success("获取用户搜索历史成功", history);
        } catch (Exception e) {
            return Result.error("获取用户搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除用户搜索历史
     */
    @DeleteMapping("/user-history/{userId}")
    public Result<String> clearUserSearchHistory(@PathVariable Long userId) {
        try {
            boolean success = searchService.clearUserSearchHistory(userId);
            if (success) {
                return Result.success("清除用户搜索历史成功");
            } else {
                return Result.error("清除用户搜索历史失败");
            }
        } catch (Exception e) {
            return Result.error("清除用户搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     * 优先从request attribute获取（JwtAuthenticationFilter设置的），
     * 如果没有则从Authorization header解析
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
        
        // 未登录用户返回null
        return null;
    }
    
    /**
     * 计算搜索结果总数
     */
    private int calculateTotalCount(Map<String, Object> result) {
        int totalCount = 0;
        if (result != null) {
            if (result.get("attractions") != null) {
                Map<String, Object> attractions = (Map<String, Object>) result.get("attractions");
                if (attractions.get("total") != null) {
                    totalCount += ((Number) attractions.get("total")).intValue();
                }
            }
            if (result.get("travelPlans") != null) {
                Map<String, Object> travelPlans = (Map<String, Object>) result.get("travelPlans");
                if (travelPlans.get("total") != null) {
                    totalCount += ((Number) travelPlans.get("total")).intValue();
                }
            }
            if (result.get("cultureProjects") != null) {
                Map<String, Object> cultureProjects = (Map<String, Object>) result.get("cultureProjects");
                if (cultureProjects.get("total") != null) {
                    totalCount += ((Number) cultureProjects.get("total")).intValue();
                }
            }
            if (result.get("travelRoutes") != null) {
                Map<String, Object> travelRoutes = (Map<String, Object>) result.get("travelRoutes");
                if (travelRoutes.get("total") != null) {
                    totalCount += ((Number) travelRoutes.get("total")).intValue();
                }
            }
        }
        return totalCount;
    }
}


















