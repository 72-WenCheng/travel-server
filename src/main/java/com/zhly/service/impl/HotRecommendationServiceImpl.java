package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.HotRecommendation;
import com.zhly.mapper.HotRecommendationMapper;
import com.zhly.service.HotRecommendationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热门推荐服务实现类
 */
@Service
public class HotRecommendationServiceImpl extends ServiceImpl<HotRecommendationMapper, HotRecommendation> implements HotRecommendationService {
    
    @Override
    public List<HotRecommendation> getRecommendationsByType(Integer recommendType) {
        return baseMapper.selectByRecommendType(recommendType);
    }
    
    @Override
    public boolean incrementClickCount(Long id) {
        return baseMapper.incrementClickCount(id) > 0;
    }
    
    @Override
    public Page<HotRecommendation> pageRecommendations(int page, int size, Integer recommendType, Integer itemType, Integer status) {
        Page<HotRecommendation> pageInfo = new Page<>(page, size);
        QueryWrapper<HotRecommendation> queryWrapper = new QueryWrapper<>();
        
        if (recommendType != null) {
            queryWrapper.eq("recommend_type", recommendType);
        }
        
        if (itemType != null) {
            queryWrapper.eq("item_type", itemType);
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("display_order").orderByDesc("click_count");
        
        return this.page(pageInfo, queryWrapper);
    }
    
    @Override
    public Map<String, Object> getRecommendationStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 总推荐数
            long total = this.count();
            stats.put("total", total);
            
            // 启用中的推荐数
            LambdaQueryWrapper<HotRecommendation> activeQuery = new LambdaQueryWrapper<>();
            activeQuery.eq(HotRecommendation::getStatus, 1);
            long active = this.count(activeQuery);
            stats.put("active", active);
            
            // 总点击量
            List<HotRecommendation> allRecommendations = this.list();
            int totalClicks = allRecommendations.stream()
                .mapToInt(r -> r.getClickCount() != null ? r.getClickCount() : 0)
                .sum();
            stats.put("totalClicks", totalClicks);
            
            // 平均评分
            double avgScore = allRecommendations.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);
            stats.put("avgScore", String.format("%.1f", avgScore));
        } catch (Exception e) {
            stats.put("total", 0L);
            stats.put("active", 0L);
            stats.put("totalClicks", 0);
            stats.put("avgScore", "0.0");
        }
        return stats;
    }
}







