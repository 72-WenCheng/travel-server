package com.zhly.service;

import com.zhly.entity.UserCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserCollectService extends IService<UserCollect> {
    
    /**
     * 添加收藏
     */
    boolean addCollect(Long userId, Integer collectType, Long collectId);
    
    /**
     * 取消收藏
     */
    boolean removeCollect(Long userId, Integer collectType, Long collectId);
    
    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, Integer collectType, Long collectId);
    
    /**
     * 获取用户收藏列表
     */
    Map<String, Object> getUserCollects(Long userId, Integer collectType, Integer page, Integer size);
    
    /**
     * 获取用户收藏统计
     */
    Map<String, Object> getUserCollectStats(Long userId);
    
    /**
     * 批量删除收藏
     */
    boolean batchRemoveCollects(Long userId, List<Long> collectIds);
    
    /**
     * 减少攻略收藏数
     */
    void decreasePlanCollectCount(Long planId);
}



















