package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.UserFollow;
import com.zhly.mapper.UserFollowMapper;
import com.zhly.service.IUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户关注Service实现
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements IUserFollowService {
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Override
    @Transactional
    public void followUser(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        
        // 检查是否已关注
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
               .eq("followee_id", followeeId);
        UserFollow existing = userFollowMapper.selectOne(wrapper);
        
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new RuntimeException("已经关注过了");
            } else {
                // 重新关注
                existing.setStatus(1);
                existing.setUpdateTime(LocalDateTime.now());
                userFollowMapper.updateById(existing);
            }
        } else {
            // 新增关注
            UserFollow follow = new UserFollow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setStatus(1);
            follow.setCreateTime(LocalDateTime.now());
            follow.setUpdateTime(LocalDateTime.now());
            userFollowMapper.insert(follow);
        }
    }
    
    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
               .eq("followee_id", followeeId);
        UserFollow follow = userFollowMapper.selectOne(wrapper);
        
        if (follow != null) {
            follow.setStatus(0);
            follow.setUpdateTime(LocalDateTime.now());
            userFollowMapper.updateById(follow);
        }
    }
    
    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return userFollowMapper.isFollowing(followerId, followeeId);
    }
    
    @Override
    public List<UserFollow> getFollowingList(Long userId, Integer page, Integer limit) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId)
               .eq("status", 1)
               .orderByDesc("create_time");
        
        if (page != null && limit != null) {
            int offset = (page - 1) * limit;
            wrapper.last("LIMIT " + offset + ", " + limit);
        }
        
        return userFollowMapper.selectList(wrapper);
    }
    
    @Override
    public List<UserFollow> getFollowersList(Long userId, Integer page, Integer limit) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("followee_id", userId)
               .eq("status", 1)
               .orderByDesc("create_time");
        
        if (page != null && limit != null) {
            int offset = (page - 1) * limit;
            wrapper.last("LIMIT " + offset + ", " + limit);
        }
        
        return userFollowMapper.selectList(wrapper);
    }
    
    @Override
    public Integer getFollowingCount(Long userId) {
        return userFollowMapper.getFollowingCount(userId);
    }
    
    @Override
    public Integer getFollowersCount(Long userId) {
        return userFollowMapper.getFollowersCount(userId);
    }
}







