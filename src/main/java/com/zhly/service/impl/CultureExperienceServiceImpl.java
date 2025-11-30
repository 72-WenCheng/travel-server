package com.zhly.service.impl;

import com.zhly.entity.CultureExperience;
import com.zhly.mapper.CultureExperienceMapper;
import com.zhly.service.CultureExperienceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文化体验服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class CultureExperienceServiceImpl extends ServiceImpl<CultureExperienceMapper, CultureExperience> implements CultureExperienceService {
    
    @Autowired
    private CultureExperienceMapper cultureExperienceMapper;
    
    @Override
    public Map<String, Object> getExperienceList(Integer page, Integer size, String keyword, String categoryName, String status) {
        try {
            List<CultureExperience> experiences = cultureExperienceMapper.selectExperienceList(page, size, keyword, categoryName, status);
            Long total = cultureExperienceMapper.selectExperienceCount(keyword, categoryName, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", experiences != null ? experiences : java.util.Collections.emptyList());
            result.put("total", total != null ? total : 0L);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取文化体验列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public CultureExperience getExperienceById(Long id) {
        try {
            return cultureExperienceMapper.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("获取文化体验详情失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean createExperience(CultureExperience experience) {
        try {
            if (experience.getStatus() == null || experience.getStatus().isEmpty()) {
                experience.setStatus("active");
            }
            if (experience.getViewCount() == null) {
                experience.setViewCount(0);
            }
            if (experience.getOrderCount() == null) {
                experience.setOrderCount(0);
            }
            if (experience.getRating() == null) {
                experience.setRating(new java.math.BigDecimal("5.0"));
            }
            return cultureExperienceMapper.insert(experience) > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建文化体验失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateExperience(CultureExperience experience) {
        try {
            return cultureExperienceMapper.updateById(experience) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新文化体验失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteExperience(Long id) {
        try {
            CultureExperience experience = cultureExperienceMapper.selectById(id);
            if (experience == null) {
                throw new RuntimeException("文化体验不存在");
            }
            return cultureExperienceMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除文化体验失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<CultureExperience> getHotExperiences(Integer limit) {
        try {
            return cultureExperienceMapper.selectHotExperiences(limit);
        } catch (Exception e) {
            throw new RuntimeException("获取热门文化体验失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<CultureExperience> getExperiencesByCategory(String categoryName, Integer limit) {
        try {
            return cultureExperienceMapper.selectExperiencesByCategory(categoryName, limit);
        } catch (Exception e) {
            throw new RuntimeException("获取分类文化体验失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateViewCount(Long id) {
        try {
            return cultureExperienceMapper.updateViewCount(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新浏览量失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateOrderCount(Long id) {
        try {
            return cultureExperienceMapper.updateOrderCount(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新预订量失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean toggleExperienceStatus(Long id, String status) {
        try {
            CultureExperience experience = cultureExperienceMapper.selectById(id);
            if (experience == null) {
                return false;
            }
            experience.setStatus(status);
            return cultureExperienceMapper.updateById(experience) > 0;
        } catch (Exception e) {
            throw new RuntimeException("切换文化体验状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getExperienceStatistics() {
        try {
            return cultureExperienceMapper.selectExperienceStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取文化体验统计失败: " + e.getMessage());
        }
    }
}











































































