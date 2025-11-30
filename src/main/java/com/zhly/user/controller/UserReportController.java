package com.zhly.user.controller;

import com.zhly.common.R;
import com.zhly.entity.Report;
import com.zhly.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户端-举报Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/user/report")
public class UserReportController {
    
    @Autowired
    private IReportService reportService;
    
    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody java.util.Map<String, Object> params, HttpServletRequest request) {
        // 从JWT token中获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.error("请先登录");
        }
        
        // 创建举报对象
        Report report = new Report();
        report.setReporterId(userId);
        
        // 转换前端数据格式
        // targetType: 'COMMENT' -> entity_type: 3 (评论)
        // 注意：数据库字段是 entity_type，不是 reported_type
        String targetType = (String) params.get("targetType");
        if (targetType != null) {
            switch (targetType) {
                case "PLAN":
                    report.setReportedType(1); // 攻略 -> entity_type: 1
                    break;
                case "COMMENT":
                    report.setReportedType(3); // 评论 -> entity_type: 3
                    break;
                case "USER":
                    report.setReportedType(4); // 用户 -> entity_type: 4
                    break;
                case "ATTRACTION":
                    report.setReportedType(2); // 景点 -> entity_type: 2
                    break;
                default:
                    return R.error("无效的举报类型");
            }
        } else {
            // 如果没有targetType，尝试从reportedType或entityType获取
            Object reportedTypeObj = params.get("reportedType");
            Object entityTypeObj = params.get("entityType");
            if (reportedTypeObj != null) {
                report.setReportedType(Integer.valueOf(reportedTypeObj.toString()));
            } else if (entityTypeObj != null) {
                report.setReportedType(Integer.valueOf(entityTypeObj.toString()));
            } else {
                return R.error("请指定举报类型");
            }
        }
        
        // 设置被举报内容ID（数据库字段是 entity_id）
        Object targetId = params.get("targetId");
        Object reportedId = params.get("reportedId");
        Object entityId = params.get("entityId");
        if (targetId != null) {
            report.setReportedId(Long.valueOf(targetId.toString()));
        } else if (reportedId != null) {
            report.setReportedId(Long.valueOf(reportedId.toString()));
        } else if (entityId != null) {
            report.setReportedId(Long.valueOf(entityId.toString()));
        } else {
            return R.error("请指定被举报内容ID");
        }
        
        // 转换举报原因
        // reason: 'SPAM', 'PORN', etc. -> reason 字段（varchar）
        // 数据库表中是 reason 字段（varchar），不是 reason_type
        String reason = (String) params.get("reason");
        if (reason != null) {
            // 将英文代码转换为中文描述
            switch (reason) {
                case "SPAM":
                    report.setReason("垃圾广告");
                    break;
                case "PORN":
                    report.setReason("色情低俗");
                    break;
                case "POLITICAL":
                    report.setReason("政治敏感");
                    break;
                case "ABUSE":
                    report.setReason("恶意辱骂");
                    break;
                case "OTHER":
                    report.setReason("其他");
                    break;
                default:
                    // 如果已经是中文或其他格式，直接使用
                    report.setReason(reason);
            }
        } else {
            // 尝试从其他字段获取
            Object reasonObj = params.get("reason");
            if (reasonObj != null) {
                report.setReason(reasonObj.toString());
            } else {
                return R.error("请选择举报原因");
            }
        }
        
        // 设置详细描述（数据库字段是 description）
        String description = (String) params.get("description");
        String reasonDesc = (String) params.get("reasonDesc");
        if (description != null) {
            report.setReasonDesc(description);
        } else if (reasonDesc != null) {
            report.setReasonDesc(reasonDesc);
        }
        
        try {
            reportService.submitReport(report);
            return R.ok("举报已提交，我们会尽快处理");
        } catch (Exception e) {
            return R.error("举报失败：" + e.getMessage());
        }
    }
}







