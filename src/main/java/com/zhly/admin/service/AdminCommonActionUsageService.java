package com.zhly.admin.service;

import com.zhly.admin.entity.AdminCommonActionUsage;

import java.util.List;

/**
 * 管理员常用操作统计服务
 */
public interface AdminCommonActionUsageService {

    /**
     * 记录管理员一次操作
     */
    void recordUsage(Long adminId, AdminCommonActionUsage usage);

    /**
     * 查询管理员的常用操作
     */
    List<AdminCommonActionUsage> listCommonActions(Long adminId, Integer limit);
}


