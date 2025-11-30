package com.zhly.admin.controller;

import com.zhly.common.Result;
import com.zhly.entity.CultureExperience;
import com.zhly.service.CultureExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端-文化体验管理控制器
 */
@Tag(name = "管理端-文化体验管理")
@RestController
@RequestMapping("/api/admin/culture/experiences")
@RequiredArgsConstructor
public class AdminCultureExperienceController {
    
    private final CultureExperienceService cultureExperienceService;
    
    @Operation(summary = "分页查询文化体验")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageExperiences(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String status) {
        try {
            Map<String, Object> result = cultureExperienceService.getExperienceList(page, size, keyword, categoryName, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取体验详情")
    @GetMapping("/{id}")
    public Result<CultureExperience> getExperience(@PathVariable Long id) {
        try {
            CultureExperience experience = cultureExperienceService.getExperienceById(id);
            if (experience == null) {
                return Result.error("体验不存在");
            }
            return Result.success(experience);
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "新增文化体验")
    @PostMapping
    public Result<String> createExperience(@RequestBody CultureExperience experience) {
        try {
            boolean success = cultureExperienceService.createExperience(experience);
            return success ? Result.success("新增成功") : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error("新增失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新文化体验")
    @PutMapping("/{id}")
    public Result<String> updateExperience(@PathVariable Long id, @RequestBody CultureExperience experience) {
        try {
            experience.setId(id);
            boolean success = cultureExperienceService.updateExperience(experience);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除文化体验")
    @DeleteMapping("/{id}")
    public Result<String> deleteExperience(@PathVariable Long id) {
        try {
            boolean success = cultureExperienceService.deleteExperience(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "切换体验状态")
    @PutMapping("/{id}/status")
    public Result<String> toggleExperienceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusInfo) {
        try {
            String status = statusInfo.get("status");
            boolean success = cultureExperienceService.toggleExperienceStatus(id, status);
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取体验统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getExperienceStats() {
        try {
            Map<String, Object> stats = cultureExperienceService.getExperienceStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }
}











































































