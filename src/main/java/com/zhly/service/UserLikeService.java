package com.zhly.service;

import com.zhly.entity.UserLike;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 用户点赞服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserLikeService extends IService<UserLike> {
    
    /**
     * 添加点赞
     */
    boolean addLike(Long userId, Integer likeType, Long likeId);
    
    /**
     * 取消点赞
     */
    boolean removeLike(Long userId, Integer likeType, Long likeId);
    
    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Integer likeType, Long likeId);
    
    /**
     * 获取用户点赞列表
     */
    Map<String, Object> getUserLikes(Long userId, Integer likeType, Integer page, Integer size);
    
    /**
     * 获取点赞统计
     */
    Map<String, Object> getLikeStatistics();
    
    /**
     * 获取用户点赞统计
     */
    Map<String, Object> getUserLikeStats(Long userId);
    
    /**
     * 批量取消点赞
     */
    boolean batchRemoveLikes(Long userId, List<Long> likeIds);
}


















