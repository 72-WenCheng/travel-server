package com.zhly.service;

import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface StatisticsService {
    
    /**
     * 获取系统概览统计
     */
    Map<String, Object> getSystemOverview();
    
    /**
     * 获取用户统计
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取景点统计
     */
    Map<String, Object> getAttractionStatistics();
    
    /**
     * 获取旅游攻略统计
     */
    Map<String, Object> getTravelPlanStatistics();
    
    /**
     * 获取文旅项目统计
     */
    Map<String, Object> getCultureProjectStatistics();
    
    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStatistics();
    
    /**
     * 获取收入统计
     */
    Map<String, Object> getRevenueStatistics();
    
    /**
     * 获取访问统计
     */
    Map<String, Object> getVisitStatistics();
    
    /**
     * 获取热门内容统计
     */
    Map<String, Object> getPopularContentStatistics();
    
    /**
     * 获取地区分布统计
     */
    Map<String, Object> getRegionDistributionStatistics();
    
    /**
     * 获取全球访问统计（按国家/地区聚合）
     */
    java.util.List<java.util.Map<String, Object>> getWorldTrafficStatistics();
    
    /**
     * 获取时间趋势统计
     */
    Map<String, Object> getTimeTrendStatistics(String type, Integer days);
    
    /**
     * 获取用户行为统计
     */
    Map<String, Object> getUserBehaviorStatistics();
    
    /**
     * 获取内容质量统计
     */
    Map<String, Object> getContentQualityStatistics();
    
    /**
     * 获取系统性能统计
     */
    Map<String, Object> getSystemPerformanceStatistics();
    
    /**
     * 导出统计数据
     */
    byte[] exportStatistics(String type, String format);
    
    /**
     * 获取最新用户
     */
    Object getLatestUsers(Integer limit);
    
    /**
     * 获取最新攻略
     */
    Object getLatestPlans(Integer limit);
    
    /**
     * 获取功能使用统计
     */
    Map<String, Object> getFunctionUsageStatistics(String timeRange);
    
    /**
     * 获取AI详细统计
     */
    Map<String, Object> getAiDetailStatistics();
    
    /**
     * 获取评论统计
     */
    Map<String, Object> getContentStatistics();
    
    /**
     * 获取互动统计（收藏和点赞）
     */
    Map<String, Object> getInteractionStatistics();
    
    /**
     * 获取AI使用统计
     */
    Map<String, Object> getAiUsageStatistics();
    
    /**
     * 获取文旅业务统计
     */
    Map<String, Object> getBusinessStatistics();
    
    /**
     * 获取待处理事项统计
     */
    Map<String, Object> getPendingStatistics();
    
    /**
     * 获取实时通知列表
     */
    List<Map<String, Object>> getNotifications(Integer limit);
    
    /**
     * 获取社区统计数据
     */
    Map<String, Object> getCommunityStatistics(String period);
}









