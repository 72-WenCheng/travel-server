package com.zhly.admin.controller;

import com.zhly.common.Result;
import com.zhly.admin.service.AdminUserService;
import com.zhly.admin.entity.AdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 管理端用户控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<AdminUser>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            List<AdminUser> users = adminUserService.getUserList(page, size, keyword);
            return Result.success("获取用户列表成功", users);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<AdminUser> getUserById(@PathVariable Long id) {
        try {
            AdminUser user = adminUserService.getUserById(id);
            return Result.success("获取用户详情成功", user);
        } catch (Exception e) {
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            adminUserService.updateUserStatus(id, status);
            return Result.success("更新用户状态成功");
        } catch (Exception e) {
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            return Result.success("删除用户成功");
        } catch (Exception e) {
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteUsers(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的用户");
            }
            adminUserService.batchDeleteUsers(ids);
            return Result.success("批量删除用户成功");
        } catch (Exception e) {
            return Result.error("批量删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody AdminUser user) {
        try {
            user.setId(id);
            adminUserService.updateUser(user);
            return Result.success("更新用户信息成功");
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = adminUserService.getUserStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量更新用户状态
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新用户状态")
    public Result<String> batchUpdateStatus(
            @RequestBody List<Long> ids,
            @RequestParam Integer status) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的用户");
            }
            adminUserService.batchUpdateStatus(ids, status);
            return Result.success("批量更新状态成功");
        } catch (Exception e) {
            return Result.error("批量更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量更新用户角色
     */
    @PutMapping("/batch/role")
    @Operation(summary = "批量更新用户角色")
    public Result<String> batchUpdateRole(
            @RequestBody List<Long> ids,
            @RequestParam Integer role) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的用户");
            }
            adminUserService.batchUpdateRole(ids, role);
            return Result.success("批量更新角色成功");
        } catch (Exception e) {
            return Result.error("批量更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出用户数据")
    public void exportUsers(
            HttpServletResponse response,
            @RequestParam(required = false, defaultValue = "excel") String format,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        try {
            adminUserService.exportUsers(response, format, keyword, userType, role, status);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取用户详情（包含行为数据）
     */
    @GetMapping("/{id}/detail")
    @Operation(summary = "获取用户详情（包含行为数据）")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = adminUserService.getUserDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取用户详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户登录历史
     */
    @GetMapping("/{id}/login-history")
    @Operation(summary = "获取用户登录历史")
    public Result<List<Map<String, Object>>> getUserLoginHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<Map<String, Object>> history = adminUserService.getUserLoginHistory(id, page, size);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取登录历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户操作日志
     */
    @GetMapping("/{id}/operation-logs")
    @Operation(summary = "获取用户操作日志")
    public Result<List<Map<String, Object>>> getUserOperationLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<Map<String, Object>> logs = adminUserService.getUserOperationLogs(id, page, size);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("获取操作日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 调整用户积分
     */
    @PutMapping("/{id}/points")
    @Operation(summary = "调整用户积分")
    public Result<String> adjustUserPoints(
            @PathVariable Long id,
            @RequestParam Integer points,
            @RequestParam String reason) {
        try {
            adminUserService.adjustUserPoints(id, points, reason);
            return Result.success("积分调整成功");
        } catch (Exception e) {
            return Result.error("积分调整失败：" + e.getMessage());
        }
    }
    
    /**
     * 调整用户等级
     */
    @PutMapping("/{id}/level")
    @Operation(summary = "调整用户等级")
    public Result<String> adjustUserLevel(
            @PathVariable Long id,
            @RequestParam Integer level,
            @RequestParam String reason) {
        try {
            adminUserService.adjustUserLevel(id, level, reason);
            return Result.success("等级调整成功");
        } catch (Exception e) {
            return Result.error("等级调整失败：" + e.getMessage());
        }
    }
}









