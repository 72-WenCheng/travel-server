package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.Activity;
import com.zhly.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-活动管理控制器
 */
@Tag(name = "管理端-活动管理")
@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class AdminActivityController {
    
    private final ActivityService activityService;
    
    @Operation(summary = "分页查询活动")
    @GetMapping("/page")
    public Result<Page<Activity>> pageActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer activityType,
            @RequestParam(required = false) Integer status) {
        Page<Activity> pageResult = activityService.pageActivities(page, size, title, activityType, status);
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Result<Activity> getActivity(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }
    
    @Operation(summary = "新增活动")
    @PostMapping
    public Result<String> createActivity(@RequestBody Activity activity) {
        boolean success = activityService.save(activity);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }
    
    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    public Result<String> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        boolean success = activityService.updateById(activity);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        boolean success = activityService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    @Operation(summary = "批量删除活动")
    @DeleteMapping("/batch")
    public Result<String> deleteActivities(@RequestBody java.util.List<Long> ids) {
        boolean success = activityService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }
    
    @Operation(summary = "更新活动状态")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setStatus(status);
        boolean success = activityService.updateById(activity);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
    
    @Operation(summary = "获取活动统计")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        try {
            java.util.Map<String, Object> stats = activityService.getActivityStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
}







