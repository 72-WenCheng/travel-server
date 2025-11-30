package com.zhly.admin.controller;

import com.zhly.common.Result;
import com.zhly.entity.CultureProject;
import com.zhly.service.CultureProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端-文旅内容管理控制器
 */
@Tag(name = "管理端-文旅内容管理")
@RestController
@RequestMapping("/api/admin/culture/project")
@RequiredArgsConstructor
public class AdminCultureProjectController {
    
    private final CultureProjectService cultureProjectService;
    
    @Operation(summary = "分页查询文旅项目")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = cultureProjectService.getProjectList(page, size, keyword, region, type, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<CultureProject> getProject(@PathVariable Long id) {
        try {
            CultureProject project = cultureProjectService.getProjectById(id);
            if (project == null) {
                return Result.error("项目不存在");
            }
            return Result.success(project);
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "新增项目")
    @PostMapping
    public Result<String> createProject(@RequestBody CultureProject project) {
        try {
            boolean success = cultureProjectService.createProject(project);
            return success ? Result.success("新增成功") : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error("新增失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<String> updateProject(@PathVariable Long id, @RequestBody CultureProject project) {
        try {
            project.setId(id);
            boolean success = cultureProjectService.updateProject(project);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable Long id) {
        try {
            boolean success = cultureProjectService.deleteProject(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "切换项目状态")
    @PutMapping("/{id}/status")
    public Result<String> toggleProjectStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> statusInfo) {
        try {
            boolean success = cultureProjectService.toggleProjectStatus(id, statusInfo.get("status"));
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取项目统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getProjectStats() {
        try {
            Map<String, Object> stats = cultureProjectService.getProjectStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }
}

