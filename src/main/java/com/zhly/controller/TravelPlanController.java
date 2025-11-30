package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.TravelPlan;
import com.zhly.entity.PlanAuditLog;
import com.zhly.entity.User;
import com.zhly.service.TravelPlanService;
import com.zhly.mapper.UserMapper;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 旅游攻略控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/travel-plan")
public class TravelPlanController {
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取攻略列表（支持更多筛选条件）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getPlanList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Map<String, Object> result = travelPlanService.getPlanList(
                page, size, keyword, destination, status, 
                auditStatus, authorId, authorName, startTime, endTime);
            return Result.success("获取攻略列表成功", result);
        } catch (Exception e) {
            return Result.error("获取攻略列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的攻略列表
     */
    @GetMapping("/my-plans")
    public Result<Map<String, Object>> getMyPlans(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                userId = jwtUtil.getUserIdFromToken(jwtToken);
            }
            
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> result = travelPlanService.getUserPlans(userId, page, size);
            return Result.success("获取我的攻略成功", result);
        } catch (Exception e) {
            return Result.error("获取我的攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据作者ID获取攻略列表
     */
    @GetMapping("/author/{authorId}")
    public Result<Map<String, Object>> getPlansByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer status) {
        try {
            // 如果status未指定，默认只查询已发布的攻略
            if (status == null) {
                status = 1;
            }
            
            Map<String, Object> result = travelPlanService.getPlanList(
                page, limit, null, null, status, 
                null, authorId, null, null, null);
            return Result.success("获取用户攻略成功", result);
        } catch (Exception e) {
            return Result.error("获取用户攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取攻略详情
     */
    @GetMapping("/{id}")
    public Result<TravelPlan> getPlanById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 判断是否为管理员查看（管理员查看时不增加浏览量）
            Boolean skipViewCount = false;
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwtToken = token.substring(7);
                    if (jwtUtil.validateToken(jwtToken)) {
                        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
                        if (userId != null) {
                            // 查询用户信息
                            User user = userMapper.selectById(userId);
                            // 如果用户角色是管理员（role=1），则跳过浏览量增加
                            if (user != null && user.getRole() != null && user.getRole() == 1) {
                                skipViewCount = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果解析token失败，忽略错误，继续正常流程（会增加浏览量）
                    System.out.println("解析token失败: " + e.getMessage());
                }
            }
            
            TravelPlan plan = travelPlanService.getPlanById(id, skipViewCount);
            if (plan == null) {
                return Result.notFound("攻略不存在");
            }
            return Result.success("获取攻略详情成功", plan);
        } catch (Exception e) {
            return Result.error("获取攻略详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建攻略
     */
    @PostMapping
    public Result<String> createPlan(@RequestBody TravelPlan plan) {
        try {
            boolean success = travelPlanService.createPlan(plan);
            if (success) {
                return Result.success("创建攻略成功");
            } else {
                return Result.error("创建攻略失败");
            }
        } catch (Exception e) {
            return Result.error("创建攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新攻略
     */
    @PutMapping("/{id}")
    public Result<String> updatePlan(@PathVariable Long id, @RequestBody TravelPlan plan) {
        try {
            plan.setId(id);
            boolean success = travelPlanService.updatePlan(plan);
            if (success) {
                return Result.success("更新攻略成功");
            } else {
                return Result.error("更新攻略失败");
            }
        } catch (Exception e) {
            return Result.error("更新攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除攻略
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePlan(@PathVariable Long id) {
        try {
            boolean success = travelPlanService.deletePlan(id);
            if (success) {
                return Result.success("删除攻略成功");
            } else {
                return Result.error("删除攻略失败");
            }
        } catch (Exception e) {
            return Result.error("删除攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门攻略
     */
    @GetMapping("/hot")
    public Result<List<TravelPlan>> getHotPlans(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<TravelPlan> plans = travelPlanService.getHotPlans(limit);
            return Result.success("获取热门攻略成功", plans);
        } catch (Exception e) {
            return Result.error("获取热门攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户攻略列表
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserPlans(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = travelPlanService.getUserPlans(userId, page, size);
            return Result.success("获取用户攻略成功", result);
        } catch (Exception e) {
            return Result.error("获取用户攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新攻略统计
     */
    @PostMapping("/{id}/stats")
    public Result<String> updatePlanStats(@PathVariable Long id, @RequestParam String type) {
        try {
            boolean success = travelPlanService.updatePlanStats(id, type);
            if (success) {
                return Result.success("更新攻略统计成功");
            } else {
                return Result.error("更新攻略统计失败");
            }
        } catch (Exception e) {
            return Result.error("更新攻略统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核攻略（旧版本，保留兼容性）
     * @deprecated 建议使用 auditPlanWithLog 方法
     */
    @PostMapping("/{id}/audit")
    @Deprecated
    public Result<String> auditPlan(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = travelPlanService.auditPlan(id, status);
            if (success) {
                return Result.success("审核攻略成功");
            } else {
                return Result.error("审核攻略失败");
            }
        } catch (Exception e) {
            return Result.error("审核攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核攻略（完整版，记录审核日志）
     */
    @PostMapping("/{id}/audit-with-log")
    public Result<String> auditPlanWithLog(
            @PathVariable Long id,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String rejectReason,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取审核人ID
            Long auditorId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                auditorId = jwtUtil.getUserIdFromToken(jwtToken);
            }
            
            if (auditorId == null) {
                return Result.error("未登录或token无效");
            }
            
            // 审核拒绝时必须提供拒绝原因
            if (auditStatus == 2 && (rejectReason == null || rejectReason.trim().isEmpty())) {
                return Result.error("审核拒绝时必须填写拒绝原因");
            }
            
            boolean success = travelPlanService.auditPlanWithLog(
                id, auditorId, auditStatus, rejectReason);
            
            if (success) {
                String message = auditStatus == 1 ? "审核通过成功" : "审核拒绝成功";
                return Result.success(message);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取攻略审核记录
     */
    @GetMapping("/{id}/audit-logs")
    public Result<List<PlanAuditLog>> getAuditLogs(@PathVariable Long id) {
        try {
            List<PlanAuditLog> logs = travelPlanService.getAuditLogs(id);
            return Result.success("获取审核记录成功", logs);
        } catch (Exception e) {
            return Result.error("获取审核记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户提交审核
     */
    @PostMapping("/{id}/submit-audit")
    public Result<String> submitForAudit(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                userId = jwtUtil.getUserIdFromToken(jwtToken);
            }
            
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            boolean success = travelPlanService.submitForAudit(id, userId);
            
            if (success) {
                return Result.success("提交审核成功");
            } else {
                return Result.error("提交审核失败");
            }
        } catch (Exception e) {
            return Result.error("提交审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户重新提交（已驳回后）
     */
    @PostMapping("/{id}/resubmit")
    public Result<String> resubmitPlan(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                userId = jwtUtil.getUserIdFromToken(jwtToken);
            }
            
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            boolean success = travelPlanService.resubmitPlan(id, userId);
            
            if (success) {
                return Result.success("重新提交成功");
            } else {
                return Result.error("重新提交失败");
            }
        } catch (Exception e) {
            return Result.error("重新提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户撤回审核（待审核状态撤回到草稿）
     */
    @PutMapping("/{id}/withdraw")
    public Result<String> withdrawPlan(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                userId = jwtUtil.getUserIdFromToken(jwtToken);
            }
            
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            boolean success = travelPlanService.withdrawPlan(id, userId);
            
            if (success) {
                return Result.success("撤回成功");
            } else {
                return Result.error("撤回失败");
            }
        } catch (Exception e) {
            return Result.error("撤回失败: " + e.getMessage());
        }
    }
}