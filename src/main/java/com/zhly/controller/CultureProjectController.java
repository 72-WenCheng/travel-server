package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.CultureProject;
import com.zhly.service.CultureProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文旅项目控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/culture-project")
public class CultureProjectController {
    
    @Autowired
    private CultureProjectService cultureProjectService;
    
    /**
     * 获取文旅项目列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getProjectList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = cultureProjectService.getProjectList(page, size, keyword, region, type, status);
            return Result.success("获取文旅项目列表成功", result);
        } catch (Exception e) {
            return Result.error("获取文旅项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文旅项目详情
     */
    @GetMapping("/{id}")
    public Result<CultureProject> getProjectById(@PathVariable Long id) {
        try {
            CultureProject project = cultureProjectService.getProjectById(id);
            if (project == null) {
                return Result.notFound("文旅项目不存在");
            }
            return Result.success("获取文旅项目详情成功", project);
        } catch (Exception e) {
            return Result.error("获取文旅项目详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建文旅项目
     */
    @PostMapping
    public Result<String> createProject(@RequestBody CultureProject project) {
        try {
            boolean success = cultureProjectService.createProject(project);
            if (success) {
                return Result.success("创建文旅项目成功");
            } else {
                return Result.error("创建文旅项目失败");
            }
        } catch (Exception e) {
            return Result.error("创建文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文旅项目
     */
    @PutMapping("/{id}")
    public Result<String> updateProject(@PathVariable Long id, @RequestBody CultureProject project) {
        try {
            project.setId(id);
            boolean success = cultureProjectService.updateProject(project);
            if (success) {
                return Result.success("更新文旅项目成功");
            } else {
                return Result.error("更新文旅项目失败");
            }
        } catch (Exception e) {
            return Result.error("更新文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文旅项目
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable Long id) {
        try {
            boolean success = cultureProjectService.deleteProject(id);
            if (success) {
                return Result.success("删除文旅项目成功");
            } else {
                return Result.error("删除文旅项目失败");
            }
        } catch (Exception e) {
            return Result.error("删除文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门文旅项目
     */
    @GetMapping("/hot")
    public Result<List<CultureProject>> getHotProjects(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<CultureProject> projects = cultureProjectService.getHotProjects(limit);
            return Result.success("获取热门文旅项目成功", projects);
        } catch (Exception e) {
            return Result.error("获取热门文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取地区文旅项目
     */
    @GetMapping("/region/{region}")
    public Result<List<CultureProject>> getRegionProjects(
            @PathVariable String region,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<CultureProject> projects = cultureProjectService.getRegionProjects(region, limit);
            return Result.success("获取地区文旅项目成功", projects);
        } catch (Exception e) {
            return Result.error("获取地区文旅项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文旅项目统计
     */
    @PostMapping("/{id}/stats")
    public Result<String> updateProjectStats(@PathVariable Long id, @RequestParam String type) {
        try {
            boolean success = cultureProjectService.updateProjectStats(id, type);
            if (success) {
                return Result.success("更新文旅项目统计成功");
            } else {
                return Result.error("更新文旅项目统计失败");
            }
        } catch (Exception e) {
            return Result.error("更新文旅项目统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换文旅项目状态
     */
    @PostMapping("/{id}/status")
    public Result<String> toggleProjectStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = cultureProjectService.toggleProjectStatus(id, status);
            if (success) {
                return Result.success("切换文旅项目状态成功");
            } else {
                return Result.error("切换文旅项目状态失败");
            }
        } catch (Exception e) {
            return Result.error("切换文旅项目状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文旅项目统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getProjectStatistics() {
        try {
            Map<String, Object> result = cultureProjectService.getProjectStatistics();
            return Result.success("获取文旅项目统计成功", result);
        } catch (Exception e) {
            return Result.error("获取文旅项目统计失败: " + e.getMessage());
        }
    }
}