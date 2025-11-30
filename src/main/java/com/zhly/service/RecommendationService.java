package com.zhly.service;

import java.util.List;
import java.util.Map;

/**
 * 智能推荐服务接口
 * 基于用户画像的大数据推荐系统
 * 
 * 用户画像数据来源：
 * - 浏览行为（浏览次数、停留时间、浏览类型）
 * - 搜索记录（搜索关键词、搜索频次）
 * - 收藏偏好（收藏内容类型、收藏时间）
 * - 消费行为（消费金额、消费频次）
 * - 地理位置（常去城市、地域偏好）
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface RecommendationService {
    
    /**
     * 基于用户画像推荐景点
     * 通过分析用户的浏览行为、收藏偏好、搜索记录等数据，智能推荐符合用户兴趣的景点
     */
    List<Map<String, Object>> recommendAttractions(Long userId, String city, Integer limit);
    
    /**
     * 基于用户画像推荐攻略
     * 通过分析用户的浏览行为、收藏偏好、搜索记录等数据，智能推荐符合用户兴趣的攻略
     */
    List<Map<String, Object>> recommendPlans(Long userId, String destination, Integer limit);
    
    /**
     * 基于协同过滤推荐内容
     */
    List<Map<String, Object>> collaborativeFiltering(Long userId, String type, Integer limit);
    
    /**
     * 基于内容推荐
     */
    List<Map<String, Object>> contentBasedRecommendation(Long userId, String type, Integer limit);
    
    /**
     * 基于地理位置推荐
     */
    List<Map<String, Object>> locationBasedRecommendation(Double latitude, Double longitude, String type, Integer limit);
    
    /**
     * 基于时间推荐
     */
    List<Map<String, Object>> timeBasedRecommendation(String season, String weather, String type, Integer limit);
    
    /**
     * 混合推荐算法
     */
    List<Map<String, Object>> hybridRecommendation(Long userId, String type, Integer limit);
    
    /**
     * 获取推荐解释
     */
    String getRecommendationExplanation(Long userId, String itemId, String type);
    
    /**
     * 更新用户偏好
     */
    boolean updateUserPreferences(Long userId, Map<String, Object> preferences);
    
    /**
     * 获取用户画像
     */
    Map<String, Object> getUserProfile(Long userId);
    
    /**
     * 根据相同/相似地点推荐相关攻略
     * @param currentPlanId 当前攻略ID（用于排除自己）
     * @param destination 目的地
     * @param limit 返回数量限制
     * @return 相关攻略列表
     */
    List<Map<String, Object>> getRelatedPlansByDestination(Long currentPlanId, String destination, Integer limit);
    
    /**
     * 根据相同/相似地点推荐相关景点
     * @param destination 目的地
     * @param limit 返回数量限制
     * @return 相关景点列表（按热度排序）
     */
    List<Map<String, Object>> getRelatedAttractionsByDestination(String destination, Integer limit);
}






