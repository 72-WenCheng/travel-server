package com.zhly.service.impl;

import com.zhly.entity.UserLike;
import com.zhly.entity.TravelPlan;
import com.zhly.mapper.UserLikeMapper;
import com.zhly.mapper.TravelPlanMapper;
import com.zhly.service.UserLikeService;
import com.zhly.service.IUserPointsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户点赞服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class UserLikeServiceImpl extends ServiceImpl<UserLikeMapper, UserLike> implements UserLikeService {
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired(required = false)
    private IUserPointsService userPointsService;
    
    @Autowired(required = false)
    private TravelPlanMapper travelPlanMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addLike(Long userId, Integer likeType, Long likeId) {
        try {
            // 检查是否已经点赞
            if (isLiked(userId, likeType, likeId)) {
                return false;
            }
            
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setLikeType(likeType);
            userLike.setLikeId(likeId);
            userLike.setCreateTime(LocalDateTime.now());
            
            boolean success = userLikeMapper.insert(userLike) > 0;
            
            // 点赞成功后，给内容作者奖励1积分（每个点赞都给积分）
            // likeType: 1-攻略, 2-景点, 3-评论
            if (success && userPointsService != null && likeType == 1) {
                // 攻略点赞，给攻略作者奖励积分
                try {
                    TravelPlan plan = travelPlanMapper != null ? travelPlanMapper.selectById(likeId) : null;
                    if (plan != null && plan.getAuthorId() != null && !plan.getAuthorId().equals(userId)) {
                        // 检查这个点赞操作是否已经给过积分（通过查询积分日志，使用点赞用户ID作为标识）
                        // 注意：这里需要检查的是这个点赞操作（点赞者userId + 内容likeId）是否已经给过积分
                        // 由于积分日志只记录作者ID，我们需要用其他方式判断
                        // 简单方式：每个点赞都给积分，因为点赞记录本身就是唯一的
                        userPointsService.addPoints(
                            plan.getAuthorId(),
                            1,  // 奖励1积分
                            4,  // 行为类型：4-点赞
                            "攻略获得点赞",
                            "plan",  // 关联类型
                            likeId   // 关联ID
                        );
                        System.out.println("✅ 攻略 " + likeId + " 获得点赞（点赞者: " + userId + "），已给作者 " + plan.getAuthorId() + " 奖励1积分");
                    }
                } catch (Exception e) {
                    // 积分奖励失败不影响点赞流程，只记录日志
                    System.err.println("⚠️ 点赞成功，但积分奖励失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return success;
        } catch (Exception e) {
            throw new RuntimeException("添加点赞失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean removeLike(Long userId, Integer likeType, Long likeId) {
        try {
            QueryWrapper<UserLike> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("like_type", likeType)
                   .eq("like_id", likeId);
            
            return userLikeMapper.delete(wrapper) > 0;
        } catch (Exception e) {
            throw new RuntimeException("取消点赞失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isLiked(Long userId, Integer likeType, Long likeId) {
        try {
            return userLikeMapper.checkUserLike(userId, likeType, likeId);
        } catch (Exception e) {
            throw new RuntimeException("检查点赞状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserLikes(Long userId, Integer likeType, Integer page, Integer size) {
        try {
            List<UserLike> likes = userLikeMapper.selectUserLikes(userId, likeType, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("list", likes);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取用户点赞失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getLikeStatistics() {
        try {
            return userLikeMapper.selectLikeStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取点赞统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserLikeStats(Long userId) {
        try {
            return userLikeMapper.selectUserLikeStats(userId);
        } catch (Exception e) {
            throw new RuntimeException("获取用户点赞统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean batchRemoveLikes(Long userId, List<Long> likeIds) {
        try {
            QueryWrapper<UserLike> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .in("id", likeIds);
            
            return userLikeMapper.delete(wrapper) > 0;
        } catch (Exception e) {
            throw new RuntimeException("批量取消点赞失败: " + e.getMessage());
        }
    }
}


















