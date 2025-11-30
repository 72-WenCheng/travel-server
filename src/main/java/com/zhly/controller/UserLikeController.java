package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.service.UserLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户点赞控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user-like")
public class UserLikeController {
    
    @Autowired
    private UserLikeService userLikeService;
    
    /**
     * 添加点赞
     */
    @PostMapping("/add")
    public Result<String> addLike(
            @RequestParam Long userId,
            @RequestParam Integer likeType,
            @RequestParam Long likeId) {
        try {
            boolean success = userLikeService.addLike(userId, likeType, likeId);
            if (success) {
                return Result.success("点赞成功");
            } else {
                return Result.error("点赞失败");
            }
        } catch (Exception e) {
            return Result.error("点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消点赞
     */
    @PostMapping("/remove")
    public Result<String> removeLike(
            @RequestParam Long userId,
            @RequestParam Integer likeType,
            @RequestParam Long likeId) {
        try {
            boolean success = userLikeService.removeLike(userId, likeType, likeId);
            if (success) {
                return Result.success("取消点赞成功");
            } else {
                return Result.error("取消点赞失败");
            }
        } catch (Exception e) {
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否已点赞
     */
    @GetMapping("/check")
    public Result<Boolean> isLiked(
            @RequestParam Long userId,
            @RequestParam Integer likeType,
            @RequestParam Long likeId) {
        try {
            boolean isLiked = userLikeService.isLiked(userId, likeType, likeId);
            return Result.success("检查点赞状态成功", isLiked);
        } catch (Exception e) {
            return Result.error("检查点赞状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户点赞列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserLikes(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer likeType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = userLikeService.getUserLikes(userId, likeType, page, size);
            return Result.success("获取用户点赞成功", result);
        } catch (Exception e) {
            return Result.error("获取用户点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取点赞统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getLikeStatistics() {
        try {
            Map<String, Object> result = userLikeService.getLikeStatistics();
            return Result.success("获取点赞统计成功", result);
        } catch (Exception e) {
            return Result.error("获取点赞统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户点赞统计
     */
    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> getUserLikeStats(@PathVariable Long userId) {
        try {
            Map<String, Object> result = userLikeService.getUserLikeStats(userId);
            return Result.success("获取用户点赞统计成功", result);
        } catch (Exception e) {
            return Result.error("获取用户点赞统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量取消点赞
     */
    @PostMapping("/batch-remove")
    public Result<String> batchRemoveLikes(
            @RequestParam Long userId,
            @RequestBody List<Long> likeIds) {
        try {
            boolean success = userLikeService.batchRemoveLikes(userId, likeIds);
            if (success) {
                return Result.success("批量取消点赞成功");
            } else {
                return Result.error("批量取消点赞失败");
            }
        } catch (Exception e) {
            return Result.error("批量取消点赞失败: " + e.getMessage());
        }
    }
}


















