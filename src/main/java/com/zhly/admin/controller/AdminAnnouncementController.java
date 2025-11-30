package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.Announcement;
import com.zhly.mapper.AnnouncementMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-公告管理控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "管理端-公告管理")
@RestController
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
public class AdminAnnouncementController {
    
    private final AnnouncementMapper announcementMapper;
    
    /**
     * 分页查询公告
     */
    @Operation(summary = "分页查询公告")
    @GetMapping("/page")
    public Result<Map<String, Object>> getAnnouncementPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Announcement> pageObj = new Page<>(page, size);
            QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
            
            if (type != null) {
                wrapper.eq("type", type);
            }
            
            if (status != null) {
                wrapper.eq("status", status);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("title", keyword)
                        .or()
                        .like("content", keyword));
            }
            
            wrapper.orderByDesc("is_top", "sort_order", "publish_time", "create_time");
            
            Page<Announcement> result = announcementMapper.selectPage(pageObj, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("size", result.getSize());
            
            return Result.success("获取公告列表成功", data);
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
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            return Result.success("获取公告详情成功", announcement);
        } catch (Exception e) {
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建公告
     */
    @Operation(summary = "创建公告")
    @PostMapping
    public Result<String> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            announcement.setCreateTime(LocalDateTime.now());
            announcement.setUpdateTime(LocalDateTime.now());
            
            if (announcement.getStatus() == null) {
                announcement.setStatus(0); // 默认草稿
            }
            
            if (announcement.getIsTop() == null) {
                announcement.setIsTop(0); // 默认不置顶
            }
            
            if (announcement.getSortOrder() == null) {
                announcement.setSortOrder(0); // 默认排序权重
            }
            
            if (announcement.getStatus() == 1) {
                // 如果直接发布，设置发布时间
                announcement.setPublishTime(LocalDateTime.now());
            }
            
            announcementMapper.insert(announcement);
            return Result.success("创建公告成功");
        } catch (Exception e) {
            return Result.error("创建公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新公告
     */
    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<String> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        try {
            Announcement existing = announcementMapper.selectById(id);
            if (existing == null) {
                return Result.error("公告不存在");
            }
            
            announcement.setId(id);
            announcement.setUpdateTime(LocalDateTime.now());
            
            // 如果状态从非发布变为发布，设置发布时间
            if (existing.getStatus() != 1 && announcement.getStatus() == 1) {
                announcement.setPublishTime(LocalDateTime.now());
            }
            
            announcementMapper.updateById(announcement);
            return Result.success("更新公告成功");
        } catch (Exception e) {
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除公告
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<String> deleteAnnouncement(@PathVariable Long id) {
        try {
            int result = announcementMapper.deleteById(id);
            if (result > 0) {
                return Result.success("删除公告成功");
            } else {
                return Result.error("删除公告失败");
            }
        } catch (Exception e) {
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除公告
     */
    @Operation(summary = "批量删除公告")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteAnnouncement(@RequestBody List<Long> ids) {
        try {
            announcementMapper.deleteBatchIds(ids);
            return Result.success("批量删除公告成功");
        } catch (Exception e) {
            return Result.error("批量删除公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新公告状态
     */
    @Operation(summary = "更新公告状态")
    @PutMapping("/{id}/status")
    public Result<String> updateAnnouncementStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Announcement announcement = announcementMapper.selectById(id);
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            
            announcement.setStatus(status);
            announcement.setUpdateTime(LocalDateTime.now());
            
            // 如果状态变为发布，设置发布时间
            if (status == 1 && announcement.getPublishTime() == null) {
                announcement.setPublishTime(LocalDateTime.now());
            }
            
            announcementMapper.updateById(announcement);
            return Result.success("更新公告状态成功");
        } catch (Exception e) {
            return Result.error("更新公告状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置/取消置顶
     */
    @Operation(summary = "设置/取消置顶")
    @PutMapping("/{id}/top")
    public Result<String> toggleTop(@PathVariable Long id, @RequestParam Integer isTop) {
        try {
            Announcement announcement = announcementMapper.selectById(id);
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            
            announcement.setIsTop(isTop);
            announcement.setUpdateTime(LocalDateTime.now());
            announcementMapper.updateById(announcement);
            return Result.success(isTop == 1 ? "设置置顶成功" : "取消置顶成功");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}

