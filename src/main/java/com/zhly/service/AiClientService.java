package com.zhly.service;

import java.util.Map;

/**
 * AI客户端服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface AiClientService {
    
    /**
     * 调用AI API生成内容
     */
    String generateContent(String prompt, String modelName, Integer maxTokens, Double temperature);
    
    /**
     * 生成旅游攻略
     */
    String generateTravelPlan(Map<String, Object> params);
    
    /**
     * 智能问答
     */
    String chat(String question);
    
    /**
     * 生成景点推荐
     */
    String generateAttractionRecommendation(String destination, String interests);
    
    /**
     * 生成美食推荐
     */
    String generateFoodRecommendation(String destination, String preferences);
}






