package com.zhly.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.Activity;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {
    
    /**
     * 获取进行中的活动列表（用户端）
     */
    List<Activity> getActiveActivities();
    
    /**
     * 增加浏览量
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 增加参与人数
     */
    boolean incrementParticipantCount(Long id);
    
    /**
     * 分页查询（管理端）
     */
    Page<Activity> pageActivities(int page, int size, String title, Integer activityType, Integer status);
    
    /**
     * 获取活动详情
     */
    Activity getActivityDetail(Long id);
    
    /**
     * 获取活动统计数据
     */
    java.util.Map<String, Object> getActivityStats();
}







