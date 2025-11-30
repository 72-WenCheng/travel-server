package com.zhly.service;

import com.zhly.entity.CultureExperience;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 文化体验服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface CultureExperienceService extends IService<CultureExperience> {
    
    /**
     * 获取文化体验列表
     */
    Map<String, Object> getExperienceList(Integer page, Integer size, String keyword, String categoryName, String status);
    
    /**
     * 获取文化体验详情
     */
    CultureExperience getExperienceById(Long id);
    
    /**
     * 创建文化体验
     */
    boolean createExperience(CultureExperience experience);
    
    /**
     * 更新文化体验
     */
    boolean updateExperience(CultureExperience experience);
    
    /**
     * 删除文化体验
     */
    boolean deleteExperience(Long id);
    
    /**
     * 获取热门文化体验
     */
    List<CultureExperience> getHotExperiences(Integer limit);
    
    /**
     * 根据分类获取文化体验列表
     */
    List<CultureExperience> getExperiencesByCategory(String categoryName, Integer limit);
    
    /**
     * 更新浏览量
     */
    boolean updateViewCount(Long id);
    
    /**
     * 更新预订量
     */
    boolean updateOrderCount(Long id);
    
    /**
     * 切换文化体验状态
     */
    boolean toggleExperienceStatus(Long id, String status);
    
    /**
     * 获取文化体验统计
     */
    Map<String, Object> getExperienceStatistics();
}











































































