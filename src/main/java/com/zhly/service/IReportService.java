package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.Report;

/**
 * 举报Service接口
 * 
 * @author zhly
 * @since 2025-10-24
 */
public interface IReportService extends IService<Report> {
    
    /**
     * 提交举报
     */
    void submitReport(Report report);
    
    /**
     * 处理举报（执行处罚/驳回等动作）
     */
    void processReport(Long reportId, Long handlerId, String actionType, String remark);
    
    /**
     * 检查内容是否被举报过多次
     */
    boolean isReportedTooMuch(Integer reportedType, Long reportedId);
}







