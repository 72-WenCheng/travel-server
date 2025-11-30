package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.ProjectApplication;
import com.zhly.mapper.ProjectApplicationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理端-政府项目申请管理控制器
 */
@Tag(name = "管理端-政府项目申请管理")
@RestController
@RequestMapping("/api/admin/culture/application")
@RequiredArgsConstructor
public class AdminProjectApplicationController {
    
    private final ProjectApplicationMapper projectApplicationMapper;
    
    @Operation(summary = "分页查询申请")
    @GetMapping("/page")
    public Result<Page<ProjectApplication>> pageApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String applicationNo,
            @RequestParam(required = false) Integer applicantType,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ProjectApplication> pageParam = new Page<>(page, size);
            Page<ProjectApplication> result = projectApplicationMapper.selectApplicationPage(pageParam, applicationNo, applicantType, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取申请详情")
    @GetMapping("/{id}")
    public Result<ProjectApplication> getApplication(@PathVariable Long id) {
        try {
            ProjectApplication application = projectApplicationMapper.selectById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }
            return Result.success(application);
        } catch (Exception e) {
            return Result.error("获取申请详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "审核通过")
    @PutMapping("/{id}/approve")
    public Result<String> approveApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> approveInfo) {
        try {
            ProjectApplication application = projectApplicationMapper.selectById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }
            if (application.getStatus() != 1 && application.getStatus() != 2) {
                return Result.error("只有待审核或审核中申请才能通过");
            }
            
            application.setStatus(3);
            application.setReviewTime(LocalDateTime.now());
            if (approveInfo.containsKey("approvalAmount")) {
                application.setApprovalAmount(new java.math.BigDecimal(approveInfo.get("approvalAmount").toString()));
            }
            if (approveInfo.containsKey("reviewOpinion")) {
                application.setReviewOpinion(approveInfo.get("reviewOpinion").toString());
            }
            if (approveInfo.containsKey("projectStartTime")) {
                application.setProjectStartTime(LocalDate.parse(approveInfo.get("projectStartTime").toString()));
            }
            if (approveInfo.containsKey("projectEndTime")) {
                application.setProjectEndTime(LocalDate.parse(approveInfo.get("projectEndTime").toString()));
            }
            if (approveInfo.containsKey("remark")) {
                application.setRemark(approveInfo.get("remark").toString());
            }
            
            projectApplicationMapper.updateById(application);
            return Result.success("审核通过");
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "审核拒绝")
    @PutMapping("/{id}/reject")
    public Result<String> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> rejectInfo) {
        try {
            ProjectApplication application = projectApplicationMapper.selectById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }
            if (application.getStatus() != 1 && application.getStatus() != 2) {
                return Result.error("只有待审核或审核中申请才能拒绝");
            }
            
            application.setStatus(4);
            application.setReviewTime(LocalDateTime.now());
            application.setRejectReason(rejectInfo.get("rejectReason"));
            projectApplicationMapper.updateById(application);
            return Result.success("已拒绝申请");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "合同管理")
    @PutMapping("/{id}/contract")
    public Result<String> manageContract(
            @PathVariable Long id,
            @RequestBody Map<String, Object> contractInfo) {
        try {
            ProjectApplication application = projectApplicationMapper.selectById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }
            if (application.getStatus() != 3) {
                return Result.error("只有已通过申请才能管理合同");
            }
            
            if (contractInfo.containsKey("contractNo")) {
                application.setContractNo(contractInfo.get("contractNo").toString());
            }
            if (contractInfo.containsKey("contractSignTime")) {
                application.setContractSignTime(LocalDateTime.parse(contractInfo.get("contractSignTime").toString()));
            }
            if (contractInfo.containsKey("projectStartTime")) {
                application.setProjectStartTime(LocalDate.parse(contractInfo.get("projectStartTime").toString()));
            }
            if (contractInfo.containsKey("projectEndTime")) {
                application.setProjectEndTime(LocalDate.parse(contractInfo.get("projectEndTime").toString()));
            }
            if (contractInfo.containsKey("remark")) {
                application.setRemark(contractInfo.get("remark").toString());
            }
            
            projectApplicationMapper.updateById(application);
            return Result.success("合同信息已保存");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "导出申请")
    @GetMapping("/export")
    public void exportApplications(
            HttpServletResponse response,
            @RequestParam(required = false) String applicationNo,
            @RequestParam(required = false) Integer applicantType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long projectId) {
        try {
            List<ProjectApplication> applications = projectApplicationMapper.selectApplicationListForExport(
                applicationNo, applicantType, status, projectId);
            exportToCSV(response, applications);
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                // ignore
            }
        }
    }
    
    private void exportToCSV(HttpServletResponse response, List<ProjectApplication> applications) throws IOException {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=applications_" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        response.setCharacterEncoding("UTF-8");
        
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);
        
        PrintWriter writer = response.getWriter();
        writer.println("申请编号,项目名称,申请人类型,申请人/企业,联系人,联系电话,所在地区,申请金额(万元),批准金额(万元),状态,提交时间");
        
        for (ProjectApplication application : applications) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%s,%s",
                escapeCSV(application.getApplicationNo()),
                escapeCSV(application.getProjectTitle()),
                getApplicantTypeText(application.getApplicantType()),
                escapeCSV(application.getApplicantName()),
                escapeCSV(application.getContactName()),
                escapeCSV(application.getContactPhone()),
                escapeCSV(application.getRegion()),
                application.getApplyAmount() != null ? application.getApplyAmount().doubleValue() : 0.0,
                application.getApprovalAmount() != null ? application.getApprovalAmount().doubleValue() : 0.0,
                getApplicationStatusText(application.getStatus()),
                application.getSubmitTime() != null ? application.getSubmitTime().toString() : ""
            ));
        }
        
        writer.flush();
        writer.close();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String getApplicantTypeText(Integer type) {
        if (type == null) return "";
        Map<Integer, String> types = Map.of(1, "个人", 2, "企业", 3, "合作社");
        return types.getOrDefault(type, "");
    }
    
    private String getApplicationStatusText(Integer status) {
        if (status == null) return "";
        Map<Integer, String> statuses = Map.of(1, "待审核", 2, "审核中", 3, "已通过", 4, "已拒绝", 5, "已撤回");
        return statuses.getOrDefault(status, "");
    }
}

