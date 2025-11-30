package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.PointsLog;
import com.zhly.entity.UserLevel;
import com.zhly.entity.UserPoints;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.mapper.UserLevelMapper;
import com.zhly.mapper.UserPointsMapper;
import com.zhly.service.IUserPointsService;
import com.zhly.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用户积分Service实现
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements IUserPointsService {
    
    @Autowired
    private UserPointsMapper userPointsMapper;
    
    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired
    private PointsLogMapper pointsLogMapper;
    
    @Autowired(required = false)
    private INotificationService notificationService;
    
    @Override
    @Transactional
    public void initUserPoints(Long userId) {
        // 检查是否已存在
        UserPoints existing = userPointsMapper.getByUserId(userId);
        if (existing != null) {
            return;
        }
        
        // 创建初始积分记录
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(userId);
        userPoints.setTotalPoints(0);
        userPoints.setAvailablePoints(0);
        userPoints.setFrozenPoints(0);
        // 不再设置等级字段，等级完全由积分实时计算（根据升级指南）
        userPoints.setCreateTime(LocalDateTime.now());
        userPoints.setUpdateTime(LocalDateTime.now());
        
        userPointsMapper.insert(userPoints);
    }
    
    @Override
    @Transactional
    public void addPoints(Long userId, Integer points, Integer actionType, String actionDesc, 
                         String relatedType, Long relatedId) {
        // 获取用户积分信息
        UserPoints userPoints = userPointsMapper.getByUserId(userId);
        if (userPoints == null) {
            initUserPoints(userId);
            userPoints = userPointsMapper.getByUserId(userId);
        }
        
        // 更新积分
        int newTotalPoints = userPoints.getTotalPoints() + points;
        int newAvailablePoints = userPoints.getAvailablePoints() + points;
        
        // 保存旧等级信息，用于判断是否升级
        Long oldLevelId = userPoints.getCurrentLevelId();
        
        userPoints.setTotalPoints(newTotalPoints);
        userPoints.setAvailablePoints(newAvailablePoints);
        
        // 根据新积分自动计算并更新等级
        UserLevel newLevel = userLevelMapper.getLevelByPoints(newTotalPoints);
        if (newLevel != null) {
            // 检查等级是否发生变化
            boolean levelChanged = false;
            if (oldLevelId == null || !oldLevelId.equals(newLevel.getId())) {
                levelChanged = true;
            }
            
            // 更新等级信息
            userPoints.setCurrentLevelId(newLevel.getId());
            userPoints.setLevelCode(newLevel.getLevelCode());
            userPoints.setLevelName(newLevel.getLevelName());
            
            // 如果等级提升，发送升级通知
            if (levelChanged && oldLevelId != null && notificationService != null) {
                try {
                    notificationService.sendNotification(
                        userId,
                        7,  // 通知类型：7=等级升级
                        "等级提升",
                        "恭喜！您已升级到 " + newLevel.getLevelName() + "，继续加油！",
                        null,  // 系统通知，无发送者
                        "LEVEL",  // 关联类型
                        newLevel.getId(),  // 等级ID
                        "/user/level-guide"  // 链接地址
                    );
                } catch (Exception e) {
                    // 通知发送失败不影响积分更新
                    System.err.println("发送升级通知失败: " + e.getMessage());
                }
            }
        }
        
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分明细
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setActionType(actionType);
        log.setActionDesc(actionDesc);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        log.setBalanceAfter(newAvailablePoints);
        log.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(log);
    }
    
    @Override
    @Transactional
    public void deductPoints(Long userId, Integer points, Integer actionType, String actionDesc) {
        UserPoints userPoints = userPointsMapper.getByUserId(userId);
        if (userPoints == null || userPoints.getAvailablePoints() < points) {
            throw new RuntimeException("积分不足");
        }
        
        int newAvailablePoints = userPoints.getAvailablePoints() - points;
        userPoints.setAvailablePoints(newAvailablePoints);
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分明细
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(-points);
        log.setActionType(actionType);
        log.setActionDesc(actionDesc);
        log.setBalanceAfter(newAvailablePoints);
        log.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(log);
    }
    
    @Override
    public boolean checkPermission(Long userId, String permissionType) {
        UserPoints userPoints = userPointsMapper.getByUserId(userId);
        if (userPoints == null) {
            return false;
        }
        
        // 统一使用积分计算等级（根据升级指南）
        int totalPoints = userPoints.getTotalPoints() != null ? userPoints.getTotalPoints() : 0;
        UserLevel level = userLevelMapper.getLevelByPoints(totalPoints);
        if (level == null) {
            return false;
        }
        
        switch (permissionType) {
            case "POST_PLAN":
                return level.getCanPostPlan() == 1;
            case "COMMENT":
                return level.getCanComment() == 1;
            case "VIEW_PREMIUM":
                return level.getCanViewPremium() == 1;
            default:
                return false;
        }
    }
    
    @Override
    public int getTodayPostCount(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("action_type", 2)  // 发布攻略
               .between("create_time", startOfDay, endOfDay);
        
        return pointsLogMapper.selectCount(wrapper).intValue();
    }
    
    @Override
    public int getTodayCommentCount(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("action_type", 3)  // 评论
               .between("create_time", startOfDay, endOfDay);
        
        return pointsLogMapper.selectCount(wrapper).intValue();
    }
    
    @Override
    public UserPoints getUserPointsWithLevel(Long userId) {
        UserPoints userPoints = userPointsMapper.getByUserId(userId);
        if (userPoints == null) {
            initUserPoints(userId);
            userPoints = userPointsMapper.getByUserId(userId);
        }
        
        // 确保等级信息与积分同步（如果等级信息为空或不匹配，自动更新）
        if (userPoints != null && userPoints.getTotalPoints() != null) {
            UserLevel currentLevel = userLevelMapper.getLevelByPoints(userPoints.getTotalPoints());
            if (currentLevel != null) {
                // 如果等级信息为空或不匹配，更新等级信息
                if (userPoints.getCurrentLevelId() == null || 
                    !userPoints.getCurrentLevelId().equals(currentLevel.getId())) {
                    userPoints.setCurrentLevelId(currentLevel.getId());
                    userPoints.setLevelCode(currentLevel.getLevelCode());
                    userPoints.setLevelName(currentLevel.getLevelName());
                    userPoints.setUpdateTime(LocalDateTime.now());
                    userPointsMapper.updateById(userPoints);
                }
            }
        }
        
        return userPoints;
    }
}







