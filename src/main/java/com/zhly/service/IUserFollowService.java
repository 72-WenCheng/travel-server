package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.UserFollow;

import java.util.List;

/**
 * 用户关注Service接口
 * 
 * @author zhly
 * @since 2025-10-24
 */
public interface IUserFollowService extends IService<UserFollow> {
    
    /**
     * 关注用户
     */
    void followUser(Long followerId, Long followeeId);
    
    /**
     * 取消关注
     */
    void unfollowUser(Long followerId, Long followeeId);
    
    /**
     * 检查是否关注
     */
    boolean isFollowing(Long followerId, Long followeeId);
    
    /**
     * 获取关注列表
     */
    List<UserFollow> getFollowingList(Long userId, Integer page, Integer limit);
    
    /**
     * 获取粉丝列表
     */
    List<UserFollow> getFollowersList(Long userId, Integer page, Integer limit);
    
    /**
     * 获取关注数
     */
    Integer getFollowingCount(Long userId);
    
    /**
     * 获取粉丝数
     */
    Integer getFollowersCount(Long userId);
}







