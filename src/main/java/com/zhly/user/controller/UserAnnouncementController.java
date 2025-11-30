package com.zhly.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhly.common.Result;
import com.zhly.entity.Announcement;
import com.zhly.mapper.AnnouncementMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-公告控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "用户端-公告")
@RestController
@RequestMapping("/api/user/announcement")
@RequiredArgsConstructor
public class UserAnnouncementController {
    
    private final AnnouncementMapper announcementMapper;
    
    /**
     * 获取已发布的公告列表
     */
    @Operation(summary = "获取已发布的公告列表")
    @GetMapping("/list")
    public Result<List<Announcement>> getPublishedAnnouncements(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 只获取已发布的
            wrapper.orderByDesc("is_top", "sort_order", "publish_time");
            
            if (type != null) {
                wrapper.eq("type", type);
            }
            
            wrapper.last("LIMIT " + limit);
            
            List<Announcement> announcements = announcementMapper.selectList(wrapper);
            return Result.success("获取公告列表成功", announcements);
        } catch (Exception e) {
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取公告详情
     */
    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        try {
            Announcement announcement = announcementMapper.selectById(id);
            if (announcement == null || announcement.getStatus() != 1) {
                return Result.error("公告不存在或未发布");
            }
            return Result.success("获取公告详情成功", announcement);
        } catch (Exception e) {
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }
}

