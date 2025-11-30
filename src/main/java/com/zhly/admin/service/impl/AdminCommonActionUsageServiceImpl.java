package com.zhly.admin.service.impl;

import com.zhly.admin.entity.AdminCommonActionUsage;
import com.zhly.admin.mapper.AdminCommonActionUsageMapper;
import com.zhly.admin.service.AdminCommonActionUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员常用操作统计服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminCommonActionUsageServiceImpl implements AdminCommonActionUsageService {

    private final AdminCommonActionUsageMapper actionUsageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(Long adminId, AdminCommonActionUsage usage) {
        if (adminId == null || usage == null) {
            return;
        }
        usage.setAdminId(adminId);
        actionUsageMapper.upsertUsage(usage);
    }

    @Override
    public List<AdminCommonActionUsage> listCommonActions(Long adminId, Integer limit) {
        if (adminId == null) {
            return List.of();
        }
        int queryLimit = (limit == null || limit <= 0) ? 3 : Math.min(limit, 10);
        return actionUsageMapper.selectTopActions(adminId, queryLimit);
    }
}


