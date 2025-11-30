package com.zhly.service;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface SearchService {
    
    /**
     * 全局搜索
     */
    Map<String, Object> globalSearch(String keyword, Integer page, Integer size);
    
    /**
     * 搜索景点
     */
    Map<String, Object> searchAttractions(String keyword, String city, Integer page, Integer size);
    
    /**
     * 搜索旅游攻略
     */
    Map<String, Object> searchTravelPlans(String keyword, String destination, Integer page, Integer size);
    
    /**
     * 搜索文旅项目
     */
    Map<String, Object> searchCultureProjects(String keyword, String region, Integer page, Integer size);
    
    /**
     * 搜索旅游路线
     */
    Map<String, Object> searchTravelRoutes(String keyword, String startCity, String endCity, Integer page, Integer size);
    
    /**
     * 获取搜索建议
     */
    List<String> getSearchSuggestions(String keyword, Integer limit);
    
    /**
     * 记录搜索历史
     */
    boolean recordSearchHistory(Long userId, String keyword, String searchType);
    
    /**
     * 获取热门搜索词
     */
    List<Map<String, Object>> getHotSearchWords(Integer limit);
    
    /**
     * 获取用户搜索历史
     */
    List<String> getUserSearchHistory(Long userId, Integer limit);
    
    /**
     * 清除用户搜索历史
     */
    boolean clearUserSearchHistory(Long userId);
}


















