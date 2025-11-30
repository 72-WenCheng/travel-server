package com.zhly.service;

import com.zhly.entity.Attraction;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 景点服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface AttractionService extends IService<Attraction> {
    
    /**
     * 获取景点列表
     */
    Map<String, Object> getAttractionList(Integer page, Integer size, String keyword, String city, Integer type, Integer status, String startDate, String endDate);
    
    /**
     * 获取景点详情
     */
    Attraction getAttractionById(Long id);
    
    /**
     * 创建景点
     */
    boolean createAttraction(Attraction attraction);
    
    /**
     * 更新景点
     */
    boolean updateAttraction(Attraction attraction);
    
    /**
     * 删除景点
     */
    boolean deleteAttraction(Long id);
    
    /**
     * 获取热门景点
     */
    List<Attraction> getHotAttractions(Integer limit);
    
    /**
     * 获取城市景点列表
     */
    List<Attraction> getCityAttractions(String city, Integer limit);
    
    /**
     * 更新景点统计
     */
    boolean updateAttractionStats(Long id, String type);
    
    /**
     * 切换景点状态
     */
    boolean toggleAttractionStatus(Long id, Integer status);

    /**
     * 用户提交景点评分
     */
    Map<String, Object> submitRating(Long attractionId, Long userId, Integer score);

    /**
     * 获取景点评分概览
     */
    Map<String, Object> getRatingSummary(Long attractionId, Long userId);
}























