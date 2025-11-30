package com.zhly.service.impl;

import com.zhly.entity.TravelPlan;
import com.zhly.entity.PlanAuditLog;
import com.zhly.entity.UserLevel;
import com.zhly.entity.UserPoints;
import com.zhly.mapper.TravelPlanMapper;
import com.zhly.mapper.PlanAuditLogMapper;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.mapper.UserLevelMapper;
import com.zhly.entity.PointsLog;
import com.zhly.service.TravelPlanService;
import com.zhly.service.IUserPointsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游攻略服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements TravelPlanService {
    
    @Autowired
    private TravelPlanMapper travelPlanMapper;
    
    @Autowired
    private PlanAuditLogMapper planAuditLogMapper;
    
    @Autowired
    private IUserPointsService userPointsService;
    
    @Autowired
    private PointsLogMapper pointsLogMapper;
    
    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Override
    public Map<String, Object> getPlanList(
            Integer page, 
            Integer size, 
            String keyword, 
            String destination, 
            Integer status,
            Integer auditStatus,
            Long authorId,
            String authorName,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            List<TravelPlan> plans = travelPlanMapper.selectPlanListWithFilters(
                page, size, keyword, destination, status, auditStatus, 
                authorId, authorName, startTime, endTime);
            
            // 为每个攻略设置coverImage（从images字段提取第一张图片）
            for (TravelPlan plan : plans) {
                setCoverImageFromImages(plan);
            }
            
            Long total = travelPlanMapper.selectPlanCountWithFilters(
                keyword, destination, status, auditStatus, 
                authorId, authorName, startTime, endTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", plans);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取攻略列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 从images字段提取第一张图片作为coverImage
     */
    private void setCoverImageFromImages(TravelPlan plan) {
        if (plan == null) {
            return;
        }
        
        String images = plan.getImages();
        if (images != null && !images.trim().isEmpty()) {
            // 分割图片字符串（逗号分隔）
            String[] imageArray = images.split(",");
            if (imageArray.length > 0) {
                String firstImage = imageArray[0].trim();
                if (!firstImage.isEmpty()) {
                    plan.setCoverImage(firstImage);
                    return;
                }
            }
        }
        
        // 如果没有图片，设置为null或默认图片
        plan.setCoverImage(null);
    }
    
    @Override
    public TravelPlan getPlanById(Long id, Boolean skipViewCount) {
        try {
            // 使用关联查询获取作者信息
            TravelPlan plan = travelPlanMapper.selectPlanByIdWithAuthor(id);
            if (plan != null) {
                // 设置coverImage（从images字段提取第一张图片）
                setCoverImageFromImages(plan);
                // 如果不是跳过浏览量增加（管理员查看时跳过），则增加查看数
                if (skipViewCount == null || !skipViewCount) {
                    updatePlanStats(id, "view");
                }
            }
            return plan;
        } catch (Exception e) {
            throw new RuntimeException("获取攻略详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean createPlan(TravelPlan plan) {
        try {
            // 如果前端没有设置 auditStatus，保持 null（草稿状态）
            // 如果前端设置了 auditStatus，使用前端的值（如：0=待审核）
            
            // 如果状态是待审核或已发布（status=1），需要检查权限和限制
            if (plan.getStatus() != null && plan.getStatus() == 1) {
                Long authorId = plan.getAuthorId();
                if (authorId == null) {
                    throw new RuntimeException("作者ID不能为空");
                }
                
                // 1. 检查是否有发布权限
                if (userPointsService != null) {
                    boolean hasPermission = userPointsService.checkPermission(authorId, "POST_PLAN");
                    if (!hasPermission) {
                        throw new RuntimeException("您的等级不足，无法发布攻略。请先完成任务升级到白银探索者");
                    }
                    
                    // 2. 检查今日发布次数
                    int todayPosts = userPointsService.getTodayPostCount(authorId);
                    UserPoints userPoints = userPointsService.getUserPointsWithLevel(authorId);
                    if (userPoints != null && userPoints.getCurrentLevelId() != null) {
                        UserLevel currentLevel = userLevelMapper.selectById(userPoints.getCurrentLevelId());
                        if (currentLevel != null && currentLevel.getDailyPostLimit() != null) {
                            int dailyLimit = currentLevel.getDailyPostLimit();
                            if (dailyLimit > 0 && todayPosts >= dailyLimit) {
                                throw new RuntimeException("今日发布次数已达上限（" + dailyLimit + "篇），请明天再试");
                            }
                        }
                    }
                }
            }
            
            // 确保基础字段有默认值
            if (plan.getStatus() == null) {
                plan.setStatus(0);
            }
            if (plan.getViewCount() == null) {
                plan.setViewCount(0);
            }
            if (plan.getLikeCount() == null) {
                plan.setLikeCount(0);
            }
            if (plan.getCommentCount() == null) {
                plan.setCommentCount(0);
            }
            
            return travelPlanMapper.insert(plan) > 0;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updatePlan(TravelPlan plan) {
        try {
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deletePlan(Long id) {
        try {
            // 物理删除
            return travelPlanMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<TravelPlan> getHotPlans(Integer limit) {
        try {
            List<TravelPlan> plans = travelPlanMapper.selectHotPlans(limit);
            // 为每个攻略设置coverImage（从images字段提取第一张图片）
            for (TravelPlan plan : plans) {
                setCoverImageFromImages(plan);
            }
            return plans;
        } catch (Exception e) {
            throw new RuntimeException("获取热门攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserPlans(Long userId, Integer page, Integer size) {
        try {
            List<TravelPlan> plans = travelPlanMapper.selectUserPlans(userId, page, size);
            // 为每个攻略设置coverImage（从images字段提取第一张图片）
            for (TravelPlan plan : plans) {
                setCoverImageFromImages(plan);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("list", plans);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取用户攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updatePlanStats(Long id, String type) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(id);
            if (plan == null) {
                return false;
            }
            
            switch (type) {
                case "view":
                    plan.setViewCount(plan.getViewCount() + 1);
                    break;
                case "like":
                    plan.setLikeCount(plan.getLikeCount() + 1);
                    break;
                case "comment":
                    plan.setCommentCount(plan.getCommentCount() + 1);
                    break;
                case "collect":
                    plan.setLikeCount(plan.getLikeCount() + 1);
                    break;
                default:
                    return false;
            }
            
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新攻略统计失败: " + e.getMessage());
        }
    }
    
    @Override
    @Deprecated
    public boolean auditPlan(Long id, Integer status) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(id);
            if (plan == null) {
                return false;
            }
            plan.setStatus(status);
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("审核攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPlanWithLog(Long planId, Long auditorId, Integer auditStatus, String rejectReason) {
        try {
            // 1. 查询攻略
            TravelPlan plan = travelPlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("攻略不存在");
            }
            
            // 2. 更新攻略状态
            if (auditStatus == 1) {
                // 审核通过：设置为已发布
                plan.setStatus(1);
                plan.setAuditStatus(1);
                plan.setPublishTime(LocalDateTime.now());
                plan.setRejectReason(null);
            } else if (auditStatus == 2) {
                // 审核拒绝：设置为已驳回
                plan.setStatus(0);
                plan.setAuditStatus(2);
                if (rejectReason == null || rejectReason.trim().isEmpty()) {
                    throw new RuntimeException("拒绝原因不能为空");
                }
                plan.setRejectReason(rejectReason);
            } else {
                throw new RuntimeException("无效的审核状态");
            }
            
            boolean updateSuccess = travelPlanMapper.updateById(plan) > 0;
            if (!updateSuccess) {
                throw new RuntimeException("更新攻略状态失败");
            }
            
            // 3. 记录审核日志
            PlanAuditLog auditLog = new PlanAuditLog();
            auditLog.setPlanId(planId);
            auditLog.setAuditorId(auditorId);
            auditLog.setAuditStatus(auditStatus);
            auditLog.setRejectReason(rejectReason);
            auditLog.setAuditTime(LocalDateTime.now());
            
            planAuditLogMapper.insert(auditLog);
            
            // 4. 审核通过时，给作者奖励20积分（避免重复奖励）
            if (auditStatus == 1) {
                Long authorId = plan.getAuthorId();
                if (authorId != null) {
                    // 检查是否已经给过积分（通过查询积分日志）
                    QueryWrapper<PointsLog> pointsLogWrapper = new QueryWrapper<>();
                    pointsLogWrapper.eq("user_id", authorId)
                                   .eq("action_type", 2)  // 2-发布攻略
                                   .eq("related_type", "plan")
                                   .eq("related_id", planId);
                    Long existingLogCount = pointsLogMapper.selectCount(pointsLogWrapper);
                    
                    if (existingLogCount == 0) {
                        // 还没有给过积分，现在奖励
                        try {
                            userPointsService.addPoints(
                                authorId,
                                20,  // 奖励20积分
                                2,   // 行为类型：2-发布攻略
                                "攻略审核通过奖励",
                                "plan",  // 关联类型
                                planId   // 关联ID
                            );
                            System.out.println("✅ 攻略审核通过，已给作者 " + authorId + " 奖励20积分，攻略ID: " + planId);
                        } catch (Exception e) {
                            // 积分奖励失败不影响审核流程，只记录日志
                            System.err.println("⚠️ 攻略审核通过，但积分奖励失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("ℹ️ 攻略 " + planId + " 已经给过积分奖励，跳过重复奖励");
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("审核攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<PlanAuditLog> getAuditLogs(Long planId) {
        try {
            return planAuditLogMapper.selectAuditLogsByPlanId(planId);
        } catch (Exception e) {
            throw new RuntimeException("获取审核记录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForAudit(Long id, Long userId) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(id);
            if (plan == null) {
                throw new RuntimeException("攻略不存在");
            }
            
            if (!plan.getAuthorId().equals(userId)) {
                throw new RuntimeException("只能提交自己的攻略");
            }
            
            // 只有草稿状态（auditStatus为null）的攻略才能提交审核
            if (plan.getAuditStatus() != null) {
                throw new RuntimeException("只有草稿状态的攻略才能提交审核");
            }
            
            // 设置为待审核状态
            plan.setStatus(0);
            plan.setAuditStatus(0);
            plan.setUpdateTime(LocalDateTime.now());
            
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("提交审核失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resubmitPlan(Long id, Long userId) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(id);
            if (plan == null) {
                throw new RuntimeException("攻略不存在");
            }
            
            if (!plan.getAuthorId().equals(userId)) {
                throw new RuntimeException("只能重新提交自己的攻略");
            }
            
            // 只有已驳回状态（auditStatus = 2）的攻略才能重新提交
            if (plan.getAuditStatus() == null || plan.getAuditStatus() != 2) {
                throw new RuntimeException("只有已驳回状态的攻略才能重新提交");
            }
            
            // 直接设置为待审核状态（不经过草稿），清空拒绝原因
            plan.setStatus(0);
            plan.setAuditStatus(0);  // 直接到待审核
            plan.setRejectReason(null);
            plan.setUpdateTime(LocalDateTime.now());
            
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("重新提交失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawPlan(Long id, Long userId) {
        try {
            TravelPlan plan = travelPlanMapper.selectById(id);
            if (plan == null) {
                throw new RuntimeException("攻略不存在");
            }
            
            if (!plan.getAuthorId().equals(userId)) {
                throw new RuntimeException("只能撤回自己的攻略");
            }
            
            // 只有待审核状态（auditStatus = 0）的攻略才能撤回
            if (plan.getAuditStatus() == null || plan.getAuditStatus() != 0) {
                throw new RuntimeException("只有待审核状态的攻略才能撤回");
            }
            
            // 撤回到草稿状态
            plan.setAuditStatus(null);  // 设置为 NULL（草稿）
            plan.setStatus(0);
            plan.setUpdateTime(LocalDateTime.now());
            
            return travelPlanMapper.updateById(plan) > 0;
        } catch (Exception e) {
            throw new RuntimeException("撤回失败: " + e.getMessage());
        }
    }
}