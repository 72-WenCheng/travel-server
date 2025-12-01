package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据统计控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        try {
            Map<String, Object> result = statisticsService.getSystemOverview();
            return Result.success("获取系统概览成功", result);
        } catch (Exception e) {
            return Result.error("获取系统概览失败: " + e.getMessage());
        }
    }

    /**
     * 导出统计数据
     */
    @GetMapping("/export")
    public void exportStatistics(
            HttpServletResponse response,
            @RequestParam(required = false, defaultValue = "overview") String type,
            @RequestParam(required = false, defaultValue = "csv") String format,
            @RequestParam(required = false) String timeRange) {
        try {
            byte[] data = statisticsService.exportStatistics(type, format);

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            String fileName = "statistics_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setCharacterEncoding("UTF-8");

            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> result = statisticsService.getUserStatistics();
            return Result.success("获取用户统计成功", result);
        } catch (Exception e) {
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取景点统计
     */
    @GetMapping("/attraction")
    public Result<Map<String, Object>> getAttractionStatistics() {
        try {
            Map<String, Object> result = statisticsService.getAttractionStatistics();
            return Result.success("获取景点统计成功", result);
        } catch (Exception e) {
            return Result.error("获取景点统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取攻略统计
     */
    @GetMapping("/travel-plan")
    public Result<Map<String, Object>> getTravelPlanStatistics() {
        try {
            Map<String, Object> result = statisticsService.getTravelPlanStatistics();
            return Result.success("获取攻略统计成功", result);
        } catch (Exception e) {
            return Result.error("获取攻略统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新用户
     */
    @GetMapping("/latest-users")
    public Result<Object> getLatestUsers(@RequestParam(defaultValue = "5") Integer limit) {
        try {
            Object result = statisticsService.getLatestUsers(limit);
            return Result.success("获取最新用户成功", result);
        } catch (Exception e) {
            return Result.error("获取最新用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新攻略
     */
    @GetMapping("/latest-plans")
    public Result<Object> getLatestPlans(@RequestParam(defaultValue = "5") Integer limit) {
        try {
            Object result = statisticsService.getLatestPlans(limit);
            return Result.success("获取最新攻略成功", result);
        } catch (Exception e) {
            return Result.error("获取最新攻略失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/analytics/users")
    public Result<Map<String, Object>> getUserTrend(@RequestParam(defaultValue = "30") Integer days) {
        try {
            Map<String, Object> result = statisticsService.getTimeTrendStatistics("user", days);
            return Result.success("获取用户增长趋势成功", result);
        } catch (Exception e) {
            return Result.error("获取用户增长趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取功能使用统计
     */
    @GetMapping("/function-usage")
    public Result<Map<String, Object>> getFunctionUsage(
            @RequestParam(value = "range", required = false) String range) {
        try {
            Map<String, Object> result = statisticsService.getFunctionUsageStatistics(range);
            return Result.success("获取功能使用统计成功", result);
        } catch (Exception e) {
            return Result.error("获取功能使用统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI详细统计
     */
    @GetMapping("/ai-detail")
    public Result<Map<String, Object>> getAiDetailStatistics() {
        try {
            Map<String, Object> result = statisticsService.getAiDetailStatistics();
            return Result.success("获取AI详细统计成功", result);
        } catch (Exception e) {
            return Result.error("获取AI详细统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取文旅项目统计
     */
    @GetMapping("/culture-project")
    public Result<Map<String, Object>> getCultureProjectStatistics() {
        try {
            Map<String, Object> result = statisticsService.getCultureProjectStatistics();
            return Result.success("获取文旅项目统计成功", result);
        } catch (Exception e) {
            return Result.error("获取文旅项目统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门内容统计
     */
    @GetMapping("/popular")
    public Result<Map<String, Object>> getPopularContentStatistics() {
        try {
            Map<String, Object> result = statisticsService.getPopularContentStatistics();
            return Result.success("获取热门内容统计成功", result);
        } catch (Exception e) {
            return Result.error("获取热门内容统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取时间趋势统计
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTimeTrendStatistics(
            @RequestParam String type,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            Map<String, Object> result = statisticsService.getTimeTrendStatistics(type, days);
            return Result.success("获取时间趋势统计成功", result);
        } catch (Exception e) {
            return Result.error("获取时间趋势统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取评论统计
     */
    @GetMapping("/content")
    public Result<Map<String, Object>> getContentStatistics() {
        try {
            Map<String, Object> result = statisticsService.getContentStatistics();
            return Result.success("获取评论统计成功", result);
        } catch (Exception e) {
            return Result.error("获取评论统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取互动统计（收藏和点赞）
     */
    @GetMapping("/interactions")
    public Result<Map<String, Object>> getInteractionStatistics() {
        try {
            Map<String, Object> result = statisticsService.getInteractionStatistics();
            return Result.success("获取互动统计成功", result);
        } catch (Exception e) {
            return Result.error("获取互动统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI使用统计
     */
    @GetMapping("/ai-usage")
    public Result<Map<String, Object>> getAiUsageStatistics() {
        try {
            Map<String, Object> result = statisticsService.getAiUsageStatistics();
            return Result.success("获取AI使用统计成功", result);
        } catch (Exception e) {
            return Result.error("获取AI使用统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取文旅业务统计
     */
    @GetMapping("/business")
    public Result<Map<String, Object>> getBusinessStatistics() {
        try {
            Map<String, Object> result = statisticsService.getBusinessStatistics();
            return Result.success("获取文旅业务统计成功", result);
        } catch (Exception e) {
            return Result.error("获取文旅业务统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取全球访问统计（供管理后台世界地图使用）
     */
    @GetMapping("/world-traffic")
    public Result<java.util.List<java.util.Map<String, Object>>> getWorldTrafficStatistics() {
        try {
            java.util.List<java.util.Map<String, Object>> result = statisticsService.getWorldTrafficStatistics();
            return Result.success("获取全球访问统计成功", result);
        } catch (Exception e) {
            return Result.error("获取全球访问统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理事项统计
     */
    @GetMapping("/pending")
    public Result<Map<String, Object>> getPendingStatistics() {
        try {
            Map<String, Object> result = statisticsService.getPendingStatistics();
            return Result.success("获取待处理事项统计成功", result);
        } catch (Exception e) {
            return Result.error("获取待处理事项统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时通知列表
     */
    @GetMapping("/notifications")
    public Result<java.util.List<Map<String, Object>>> getNotifications(
            @RequestParam(defaultValue = "3") Integer limit) {
        try {
            java.util.List<Map<String, Object>> result = statisticsService.getNotifications(limit);
            return Result.success("获取实时通知成功", result);
        } catch (Exception e) {
            return Result.error("获取实时通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取社区统计数据
     */
    @GetMapping("/community")
    public Result<Map<String, Object>> getCommunityStatistics(
            @RequestParam(defaultValue = "all") String period) {
        try {
            Map<String, Object> result = statisticsService.getCommunityStatistics(period);
            return Result.success("获取社区统计成功", result);
        } catch (Exception e) {
            return Result.error("获取社区统计失败: " + e.getMessage());
        }
    }
}