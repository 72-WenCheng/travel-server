package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 政府项目申请实体
 */
@Data
@TableName("project_application")
public class ProjectApplication {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String applicationNo;
    private Long userId;
    private Long projectId;
    private String projectTitle;
    private Integer applicantType; // 1-个人 2-企业 3-合作社
    private String applicantName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String idCard;
    private String businessLicense;
    private String region;
    private String detailedAddress;
    private BigDecimal landArea;
    private Integer householdCount;
    private BigDecimal applyAmount;
    private String projectDescription;
    private String implementationPlan;
    private String expectedBenefits;
    private String attachmentUrls;
    private Integer status; // 1-待审核 2-审核中 3-已通过 4-已拒绝 5-已撤回
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
    private String reviewOpinion;
    private String rejectReason;
    private BigDecimal approvalAmount;
    private String contractNo;
    private LocalDateTime contractSignTime;
    private LocalDate projectStartTime;
    private LocalDate projectEndTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












































































