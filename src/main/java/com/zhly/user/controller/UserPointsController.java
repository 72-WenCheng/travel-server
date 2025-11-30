package com.zhly.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.R;
import com.zhly.entity.PointsLog;
import com.zhly.entity.UserLevel;
import com.zhly.entity.UserPoints;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.mapper.UserLevelMapper;
import com.zhly.service.IUserPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端-积分Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/user/points")
public class UserPointsController {
    
    @Autowired
    private IUserPointsService userPointsService;
    
    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired
    private PointsLogMapper pointsLogMapper;
    
    /**
     * 获取我的积分信息
     */
    @GetMapping("/my")
    public R<Map<String, Object>> getMyPoints(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 用户积分信息（会自动同步等级信息）
        UserPoints userPoints = userPointsService.getUserPointsWithLevel(userId);
        result.put("userPoints", userPoints);
        
        // 根据积分获取当前等级（确保等级信息准确）
        int totalPoints = userPoints.getTotalPoints() != null ? userPoints.getTotalPoints() : 0;
        UserLevel currentLevel = userLevelMapper.getLevelByPoints(totalPoints);
        if (currentLevel == null) {
            // 如果没有找到等级，使用默认等级（青铜旅行者，level_code=1）
            currentLevel = userLevelMapper.getLevelByCode(1);
        }
        result.put("currentLevel", currentLevel);
        
        // 下一等级信息
        Integer currentLevelCode = currentLevel != null ? currentLevel.getLevelCode() : 1;
        UserLevel nextLevel = userLevelMapper.getLevelByCode(currentLevelCode + 1);
        result.put("nextLevel", nextLevel);
        
        // 距离下一等级还需积分
        if (nextLevel != null && currentLevel != null) {
            int neededPoints = nextLevel.getRequiredPoints() - totalPoints;
            result.put("neededPoints", Math.max(0, neededPoints));
        } else {
            result.put("neededPoints", 0);
            result.put("isMaxLevel", true);
        }
        
        // 今日行为统计
        int todayPosts = userPointsService.getTodayPostCount(userId);
        int todayComments = userPointsService.getTodayCommentCount(userId);
        if (currentLevel != null) {
            result.put("dailyPostLimit", currentLevel.getDailyPostLimit());
            result.put("dailyCommentLimit", currentLevel.getDailyCommentLimit());
        } else {
            result.put("dailyPostLimit", 0);
            result.put("dailyCommentLimit", 0);
        }
        result.put("todayPosts", todayPosts);
        result.put("todayComments", todayComments);
        
        return R.ok(result);
    }
    
    /**
     * 获取我的积分明细
     */
    @GetMapping("/my-log")
    public R<Page<PointsLog>> getMyLog(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        Page<PointsLog> pageObj = new Page<>(page, limit);
        QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        pointsLogMapper.selectPage(pageObj, wrapper);
        
        return R.ok(pageObj);
    }
    
    /**
     * 获取所有等级列表（用于展示升级路径）
     */
    @GetMapping("/levels")
    public R<List<UserLevel>> getLevels() {
        QueryWrapper<UserLevel> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("level_code");
        List<UserLevel> list = userLevelMapper.selectList(wrapper);
        return R.ok(list);
    }
    
    /**
     * 检查权限
     */
    @GetMapping("/check-permission")
    public R<Map<String, Boolean>> checkPermission(
            @RequestParam Long userId,
            @RequestParam String permissionType) {
        
        boolean hasPermission = userPointsService.checkPermission(userId, permissionType);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        
        return R.ok(result);
    }
    
    /**
     * 签到获取积分
     */
    @PostMapping("/daily-checkin")
    public R<String> dailyCheckin(@RequestParam Long userId) {
        // TODO: 检查今日是否已签到
        // TODO: 连续签到额外奖励
        
        userPointsService.addPoints(userId, 5, 1, "每日签到", null, null);
        return R.ok("签到成功，获得5积分");
    }
}







