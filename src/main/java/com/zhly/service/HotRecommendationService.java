package com.zhly.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.HotRecommendation;

import java.util.List;

/**
 * 热门推荐服务接口
 */
public interface HotRecommendationService extends IService<HotRecommendation> {
    
    /**
     * 根据推荐类型获取推荐列表（用户端）
     */
    List<HotRecommendation> getRecommendationsByType(Integer recommendType);
    
    /**
     * 增加点击次数
     */
    boolean incrementClickCount(Long id);
    
    /**
     * 分页查询（管理端）
     */
    Page<HotRecommendation> pageRecommendations(int page, int size, Integer recommendType, Integer itemType, Integer status);
    
    /**
     * 获取热门推荐统计数据
     */
    java.util.Map<String, Object> getRecommendationStats();
}







