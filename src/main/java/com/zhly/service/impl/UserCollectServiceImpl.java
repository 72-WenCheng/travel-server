package com.zhly.service.impl;

import com.zhly.entity.UserCollect;
import com.zhly.entity.TravelPlan;
import com.zhly.mapper.UserCollectMapper;
import com.zhly.mapper.TravelPlanMapper;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.entity.PointsLog;
import com.zhly.service.UserCollectService;
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
 * 用户收藏服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class UserCollectServiceImpl extends ServiceImpl<UserCollectMapper, UserCollect> implements UserCollectService {
    
    @Autowired
    private UserCollectMapper userCollectMapper;
    
    @Autowired
    private TravelPlanMapper travelPlanMapper;
    
    @Autowired(required = false)
    private IUserPointsService userPointsService;
    
    @Autowired(required = false)
    private PointsLogMapper pointsLogMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCollect(Long userId, Integer collectType, Long collectId) {
        try {
            // 检查是否已经收藏
            if (isCollected(userId, collectType, collectId)) {
                return false;
            }
            
            UserCollect collect = new UserCollect();
            collect.setUserId(userId);
            collect.setCollectType(collectType);
            collect.setCollectId(collectId);
            collect.setCreateTime(LocalDateTime.now());
            
            boolean success = userCollectMapper.insert(collect) > 0;
            
            // 添加收藏成功后，给用户奖励1积分（避免重复奖励）
            if (success && userPointsService != null) {
                try {
                    // 检查是否已经给过积分（通过查询积分日志）
                    QueryWrapper<PointsLog> pointsLogWrapper = new QueryWrapper<>();
                    pointsLogWrapper.eq("user_id", userId)
                                   .eq("action_type", 5)  // 5-收藏
                                   .eq("related_type", getCollectTypeName(collectType))
                                   .eq("related_id", collectId);
                    Long existingLogCount = pointsLogMapper != null ? pointsLogMapper.selectCount(pointsLogWrapper) : 0L;
                    
                    if (existingLogCount == 0) {
                        // 还没有给过积分，现在奖励
                        userPointsService.addPoints(
                            userId,
                            1,  // 奖励1积分
                            5,  // 行为类型：5-收藏
                            "收藏内容",
                            getCollectTypeName(collectType),  // 关联类型：plan/attraction/culture
                            collectId   // 关联ID
                        );
                        System.out.println("✅ 用户 " + userId + " 收藏成功，已奖励1积分，类型: " + collectType + ", ID: " + collectId);
                    } else {
                        System.out.println("ℹ️ 收藏 " + collectId + " 已经给过积分奖励，跳过重复奖励");
                    }
                } catch (Exception e) {
                    // 积分奖励失败不影响收藏流程，只记录日志
                    System.err.println("⚠️ 收藏成功，但积分奖励失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return success;
        } catch (Exception e) {
            throw new RuntimeException("添加收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取收藏类型名称（用于积分日志的relatedType）
     */
    private String getCollectTypeName(Integer collectType) {
        if (collectType == null) {
            return "unknown";
        }
        switch (collectType) {
            case 1:
                return "plan";  // 攻略
            case 2:
                return "attraction";  // 景点
            case 3:
                return "culture";  // 文旅项目
            default:
                return "unknown";
        }
    }
    
    @Override
    public boolean removeCollect(Long userId, Integer collectType, Long collectId) {
        try {
            QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("collect_type", collectType)
                   .eq("collect_id", collectId);
            
            return userCollectMapper.delete(wrapper) > 0;
        } catch (Exception e) {
            throw new RuntimeException("取消收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isCollected(Long userId, Integer collectType, Long collectId) {
        try {
            QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("collect_type", collectType)
                   .eq("collect_id", collectId);
            
            return userCollectMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            throw new RuntimeException("检查收藏状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserCollects(Long userId, Integer collectType, Integer page, Integer size) {
        try {
            QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            
            if (collectType != null) {
                wrapper.eq("collect_type", collectType);
            }
            
            wrapper.orderByDesc("create_time");
            
            // 分页查询
            int offset = (page - 1) * size;
            wrapper.last("LIMIT " + offset + ", " + size);
            
            List<UserCollect> collects = userCollectMapper.selectList(wrapper);
            Long total = userCollectMapper.selectCount(wrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", collects);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取用户收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserCollectStats(Long userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计各类型收藏数量
            QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            
            Long totalCollects = userCollectMapper.selectCount(wrapper);
            stats.put("total", totalCollects);
            
            // 攻略收藏数
            wrapper.clear();
            wrapper.eq("user_id", userId).eq("collect_type", 1);
            Long planCollects = userCollectMapper.selectCount(wrapper);
            stats.put("planCollects", planCollects);
            
            // 景点收藏数
            wrapper.clear();
            wrapper.eq("user_id", userId).eq("collect_type", 2);
            Long attractionCollects = userCollectMapper.selectCount(wrapper);
            stats.put("attractionCollects", attractionCollects);
            
            // 文旅项目收藏数
            wrapper.clear();
            wrapper.eq("user_id", userId).eq("collect_type", 3);
            Long projectCollects = userCollectMapper.selectCount(wrapper);
            stats.put("projectCollects", projectCollects);
            
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取用户收藏统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean batchRemoveCollects(Long userId, List<Long> collectIds) {
        try {
            QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .in("id", collectIds);
            
            return userCollectMapper.delete(wrapper) > 0;
        } catch (Exception e) {
            throw new RuntimeException("批量删除收藏失败: " + e.getMessage());
        }
    }
    
    @Override
    public void decreasePlanCollectCount(Long planId) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(planId);
            if (plan != null && plan.getLikeCount() > 0) {
                plan.setLikeCount(plan.getLikeCount() - 1);
                travelPlanMapper.updateById(plan);
            }
        } catch (Exception e) {
            throw new RuntimeException("减少攻略收藏数失败: " + e.getMessage());
        }
    }
}