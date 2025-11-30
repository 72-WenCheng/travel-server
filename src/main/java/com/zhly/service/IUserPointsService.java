package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.UserPoints;

/**
 * 用户积分Service接口
 * 
 * @author zhly
 * @since 2025-10-24
 */
public interface IUserPointsService extends IService<UserPoints> {
    
    /**
     * 初始化用户积分
     */
    void initUserPoints(Long userId);
    
    /**
     * 添加积分
     * @param userId 用户ID
     * @param points 积分数
     * @param actionType 行为类型
     * @param actionDesc 行为描述
     * @param relatedType 关联类型
     * @param relatedId 关联ID
     */
    void addPoints(Long userId, Integer points, Integer actionType, String actionDesc, 
                   String relatedType, Long relatedId);
    
    /**
     * 扣除积分
     */
    void deductPoints(Long userId, Integer points, Integer actionType, String actionDesc);
    
    /**
     * 检查用户权限
     * @return true-有权限，false-无权限
     */
    boolean checkPermission(Long userId, String permissionType);
    
    /**
     * 获取用户今日已发布数量
     */
    int getTodayPostCount(Long userId);
    
    /**
     * 获取用户今日已评论数量
     */
    int getTodayCommentCount(Long userId);
    
    /**
     * 获取用户积分信息（带等级）
     */
    UserPoints getUserPointsWithLevel(Long userId);
}







