package com.zhly.service;

import com.zhly.entity.TravelPlan;
import com.zhly.entity.PlanAuditLog;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 旅游攻略服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface TravelPlanService extends IService<TravelPlan> {
    
    /**
     * 获取攻略列表（支持更多筛选条件）
     * 
     * @param page 页码
     * @param size 每页数量
     * @param keyword 标题关键词
     * @param destination 目的地
     * @param status 发布状态
     * @param auditStatus 审核状态
     * @param authorId 作者ID
     * @param authorName 作者昵称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 攻略列表和分页信息
     */
    Map<String, Object> getPlanList(
        Integer page, 
        Integer size, 
        String keyword, 
        String destination, 
        Integer status,
        Integer auditStatus,
        Long authorId,
        String authorName,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
    
    /**
     * 获取攻略详情
     * @param id 攻略ID
     * @param skipViewCount 是否跳过增加浏览量（管理员查看时设为true）
     */
    TravelPlan getPlanById(Long id, Boolean skipViewCount);
    
    /**
     * 创建攻略
     */
    boolean createPlan(TravelPlan plan);
    
    /**
     * 更新攻略
     */
    boolean updatePlan(TravelPlan plan);
    
    /**
     * 删除攻略
     */
    boolean deletePlan(Long id);
    
    /**
     * 获取热门攻略
     */
    List<TravelPlan> getHotPlans(Integer limit);
    
    /**
     * 获取用户攻略列表
     */
    Map<String, Object> getUserPlans(Long userId, Integer page, Integer size);
    
    /**
     * 更新攻略统计
     */
    boolean updatePlanStats(Long id, String type);
    
    /**
     * 审核攻略（简化版，兼容旧接口）
     * 
     * @deprecated 建议使用 auditPlanWithLog 方法
     */
    @Deprecated
    boolean auditPlan(Long id, Integer status);
    
    /**
     * 审核攻略（完整版，记录审核日志）
     * 
     * @param planId 攻略ID
     * @param auditorId 审核人ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param rejectReason 拒绝原因（拒绝时必填）
     * @return 是否成功
     */
    boolean auditPlanWithLog(Long planId, Long auditorId, Integer auditStatus, String rejectReason);
    
    /**
     * 获取攻略的审核记录
     */
    List<PlanAuditLog> getAuditLogs(Long planId);
    
    /**
     * 用户提交攻略审核
     */
    boolean submitForAudit(Long id, Long userId);
    
    /**
     * 用户重新提交攻略（已驳回后）
     */
    boolean resubmitPlan(Long id, Long userId);
    
    /**
     * 用户撤回审核（待审核状态撤回到草稿）
     */
    boolean withdrawPlan(Long id, Long userId);
}


















