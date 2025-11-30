package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 攻略审核记录实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("plan_audit_log")
public class PlanAuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long planId;                     // 攻略ID
    private Long auditorId;                  // 审核人ID
    private Integer auditStatus;             // 审核结果：1-通过，2-拒绝
    private String rejectReason;             // 拒绝原因
    private LocalDateTime auditTime;         // 审核时间
    
    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private String auditorName;              // 审核人姓名
    
    @TableField(exist = false)
    private String planTitle;                // 攻略标题
}


