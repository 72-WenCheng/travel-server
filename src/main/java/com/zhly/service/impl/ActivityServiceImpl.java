package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.Activity;
import com.zhly.mapper.ActivityMapper;
import com.zhly.service.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {
    
    @Override
    public List<Activity> getActiveActivities() {
        return baseMapper.selectActiveActivities();
    }
    
    @Override
    public boolean incrementViewCount(Long id) {
        return baseMapper.incrementViewCount(id) > 0;
    }
    
    @Override
    public boolean incrementParticipantCount(Long id) {
        return baseMapper.incrementParticipantCount(id) > 0;
    }
    
    @Override
    public Page<Activity> pageActivities(int page, int size, String title, Integer activityType, Integer status) {
        Page<Activity> pageInfo = new Page<>(page, size);
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(title)) {
            queryWrapper.like("title", title);
        }
        
        if (activityType != null) {
            queryWrapper.eq("activity_type", activityType);
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return this.page(pageInfo, queryWrapper);
    }
    
    @Override
    public Activity getActivityDetail(Long id) {
        Activity activity = this.getById(id);
        if (activity != null) {
            // 增加浏览量
            incrementViewCount(id);
        }
        return activity;
    }
    
    @Override
    public Map<String, Object> getActivityStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 总活动数
            long total = this.count();
            stats.put("total", total);
            
            // 进行中的活动数
            LambdaQueryWrapper<Activity> activeQuery = new LambdaQueryWrapper<>();
            activeQuery.eq(Activity::getStatus, 1);
            long active = this.count(activeQuery);
            stats.put("active", active);
            
            // 总浏览量
            List<Activity> allActivities = this.list();
            int totalViews = allActivities.stream()
                .mapToInt(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
            stats.put("totalViews", totalViews);
            
            // 总参与人数
            int totalParticipants = allActivities.stream()
                .mapToInt(a -> a.getParticipantCount() != null ? a.getParticipantCount() : 0)
                .sum();
            stats.put("totalParticipants", totalParticipants);
        } catch (Exception e) {
            stats.put("total", 0L);
            stats.put("active", 0L);
            stats.put("totalViews", 0);
            stats.put("totalParticipants", 0);
        }
        return stats;
    }
}







