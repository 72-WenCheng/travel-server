package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.R;
import com.zhly.entity.PointsLog;
import com.zhly.entity.UserPoints;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.mapper.UserPointsMapper;
import com.zhly.service.IUserPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-积分管理Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/admin/points")
public class AdminUserPointsController {
    
    @Autowired
    private IUserPointsService userPointsService;
    
    @Autowired
    private UserPointsMapper userPointsMapper;
    
    @Autowired
    private PointsLogMapper pointsLogMapper;
    
    /**
     * 查询用户积分列表
     */
    @GetMapping("/list")
    public R<Page<UserPoints>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer levelCode) {
        
        Page<UserPoints> pageObj = new Page<>(page, limit);
        QueryWrapper<UserPoints> wrapper = new QueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (levelCode != null) {
            wrapper.eq("level_code", levelCode);
        }
        
        wrapper.orderByDesc("total_points");
        userPointsMapper.selectPage(pageObj, wrapper);
        
        return R.ok(pageObj);
    }
    
    /**
     * 查询积分明细
     */
    @GetMapping("/log")
    public R<Page<PointsLog>> log(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer actionType) {
        
        Page<PointsLog> pageObj = new Page<>(page, limit);
        QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (actionType != null) {
            wrapper.eq("action_type", actionType);
        }
        
        wrapper.orderByDesc("create_time");
        pointsLogMapper.selectPage(pageObj, wrapper);
        
        return R.ok(pageObj);
    }
    
    /**
     * 手动调整积分（管理员）
     */
    @PostMapping("/adjust")
    public R<String> adjust(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer points = Integer.valueOf(params.get("points").toString());
        String reason = params.get("reason").toString();
        
        if (points > 0) {
            userPointsService.addPoints(userId, points, 10, reason, null, null);
        } else {
            userPointsService.deductPoints(userId, Math.abs(points), 10, reason);
        }
        
        return R.ok("积分调整成功");
    }
    
    /**
     * 批量删除用户积分记录
     */
    @DeleteMapping("/batch")
    public R<String> batchDelete(@RequestBody List<Long> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                return R.error("请选择要删除的用户");
            }
            QueryWrapper<UserPoints> wrapper = new QueryWrapper<>();
            wrapper.in("user_id", userIds);
            int deleted = userPointsMapper.delete(wrapper);
            return R.ok("批量删除成功，共删除 " + deleted + " 条记录");
        } catch (Exception e) {
            return R.error("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 积分统计
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 总用户数（有积分记录的用户）
            Long totalUsers = userPointsMapper.selectCount(null);
            result.put("totalUsers", totalUsers != null ? totalUsers : 0L);
            
            // 总积分发放量（所有增加的积分）
            QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
            wrapper.gt("points", 0);
            List<PointsLog> positiveLogs = pointsLogMapper.selectList(wrapper);
            long totalPointsIssued = positiveLogs.stream()
                .mapToLong(log -> log.getPoints() != null ? log.getPoints() : 0L)
                .sum();
            result.put("totalPointsIssued", totalPointsIssued);
            
            // 今日积分发放
            wrapper = new QueryWrapper<>();
            wrapper.gt("points", 0);
            wrapper.apply("DATE(create_time) = CURDATE()");
            List<PointsLog> todayLogs = pointsLogMapper.selectList(wrapper);
            long todayPointsIssued = todayLogs.stream()
                .mapToLong(log -> log.getPoints() != null ? log.getPoints() : 0L)
                .sum();
            result.put("todayPointsIssued", todayPointsIssued);
            
            // 平均积分
            if (totalUsers > 0) {
                long totalPoints = userPointsMapper.selectList(null).stream()
                    .mapToLong(up -> up.getTotalPoints() != null ? up.getTotalPoints() : 0L)
                    .sum();
                result.put("averagePoints", totalPoints / totalUsers);
            } else {
                result.put("averagePoints", 0L);
            }
            
            return R.ok(result);
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalUsers", 0L);
            defaultStats.put("totalPointsIssued", 0L);
            defaultStats.put("todayPointsIssued", 0L);
            defaultStats.put("averagePoints", 0L);
            return R.ok(defaultStats);
        }
    }
}







