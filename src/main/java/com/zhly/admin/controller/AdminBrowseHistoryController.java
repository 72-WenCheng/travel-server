package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.UserBrowseHistory;
import com.zhly.mapper.UserBrowseHistoryMapper;
import com.zhly.service.UserBrowseHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-浏览历史控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "管理端-浏览历史管理")
@RestController
@RequestMapping("/api/admin/browse-history")
@RequiredArgsConstructor
public class AdminBrowseHistoryController {
    
    private final UserBrowseHistoryService historyService;
    private final UserBrowseHistoryMapper historyMapper;
    
    /**
     * 分页查询浏览历史
     */
    @Operation(summary = "分页查询浏览历史")
    @GetMapping("/page")
    public Result<Map<String, Object>> getBrowseHistoryPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer browseType,
            @RequestParam(required = false) String keyword) {
        try {
            Page<UserBrowseHistory> pageObj = new Page<>(page, size);
            QueryWrapper<UserBrowseHistory> wrapper = new QueryWrapper<>();
            
            if (userId != null) {
                wrapper.eq("user_id", userId);
            }
            
            if (browseType != null) {
                wrapper.eq("browse_type", browseType);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("browse_title", keyword)
                        .or()
                        .like("browse_image", keyword));
            }
            
            wrapper.orderByDesc("create_time");
            
            Page<UserBrowseHistory> result = historyService.page(pageObj, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("size", result.getSize());
            
            return Result.success("获取浏览历史成功", data);
        } catch (Exception e) {
            return Result.error("获取浏览历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取浏览历史统计
     */
    @Operation(summary = "获取浏览历史统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBrowseHistoryStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总浏览记录数
            long totalCount = historyService.count();
            stats.put("totalCount", totalCount);
            
            // 按类型统计
            QueryWrapper<UserBrowseHistory> typeWrapper = new QueryWrapper<>();
            typeWrapper.select("browse_type", "COUNT(*) as count");
            typeWrapper.groupBy("browse_type");
            List<Map<String, Object>> typeStats = historyMapper.selectMaps(typeWrapper);
            stats.put("typeStats", typeStats);
            
            // 今日浏览记录数
            QueryWrapper<UserBrowseHistory> todayWrapper = new QueryWrapper<>();
            todayWrapper.apply("DATE(create_time) = CURDATE()");
            long todayCount = historyService.count(todayWrapper);
            stats.put("todayCount", todayCount);
            
            // 平均停留时长
            QueryWrapper<UserBrowseHistory> durationWrapper = new QueryWrapper<>();
            durationWrapper.select("AVG(duration) as avgDuration");
            durationWrapper.isNotNull("duration");
            durationWrapper.gt("duration", 0);
            List<Map<String, Object>> avgDurationList = historyMapper.selectMaps(durationWrapper);
            if (!avgDurationList.isEmpty() && avgDurationList.get(0).get("avgDuration") != null) {
                stats.put("avgDuration", avgDurationList.get(0).get("avgDuration"));
            } else {
                stats.put("avgDuration", 0);
            }
            
            return Result.success("获取统计信息成功", stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取浏览历史详情
     */
    @Operation(summary = "获取浏览历史详情")
    @GetMapping("/{id}")
    public Result<UserBrowseHistory> getBrowseHistoryById(@PathVariable Long id) {
        try {
            UserBrowseHistory history = historyService.getById(id);
            if (history == null) {
                return Result.error("浏览历史不存在");
            }
            return Result.success("获取浏览历史详情成功", history);
        } catch (Exception e) {
            return Result.error("获取浏览历史详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除浏览历史
     */
    @Operation(summary = "删除浏览历史")
    @DeleteMapping("/{id}")
    public Result<String> deleteBrowseHistory(@PathVariable Long id) {
        try {
            boolean success = historyService.removeById(id);
            if (success) {
                return Result.success("删除浏览历史成功");
            } else {
                return Result.error("删除浏览历史失败");
            }
        } catch (Exception e) {
            return Result.error("删除浏览历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除浏览历史
     */
    @Operation(summary = "批量删除浏览历史")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteBrowseHistory(@RequestBody List<Long> ids) {
        try {
            boolean success = historyService.removeByIds(ids);
            if (success) {
                return Result.success("批量删除浏览历史成功");
            } else {
                return Result.error("批量删除浏览历史失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除浏览历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除用户的所有浏览历史
     */
    @Operation(summary = "清除用户的所有浏览历史")
    @DeleteMapping("/user/{userId}")
    public Result<String> clearUserBrowseHistory(@PathVariable Long userId) {
        try {
            boolean success = historyService.clearHistory(userId);
            if (success) {
                return Result.success("清除用户浏览历史成功");
            } else {
                return Result.error("清除用户浏览历史失败");
            }
        } catch (Exception e) {
            return Result.error("清除用户浏览历史失败: " + e.getMessage());
        }
    }
}

