package com.zhly.mapper;

import com.zhly.entity.PlanAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 攻略审核记录 Mapper 接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface PlanAuditLogMapper extends BaseMapper<PlanAuditLog> {
    
    /**
     * 根据攻略ID获取审核记录
     */
    List<PlanAuditLog> selectAuditLogsByPlanId(@Param("planId") Long planId);
    
    /**
     * 根据审核人ID获取审核记录
     */
    List<PlanAuditLog> selectAuditLogsByAuditorId(@Param("auditorId") Long auditorId);
}


