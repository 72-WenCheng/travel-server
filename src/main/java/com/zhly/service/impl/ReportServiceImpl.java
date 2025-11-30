package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.Report;
import com.zhly.mapper.ReportMapper;
import com.zhly.service.ICommentService;
import com.zhly.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 举报 Service 实现
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_RESOLVED = 2;
    private static final int STATUS_REJECTED = 3;
    private static final int TYPE_COMMENT = 3;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ICommentService commentService;

    @Override
    @Transactional
    public void submitReport(Report report) {
        report.setStatus(STATUS_PENDING);
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);
    }

    @Override
    @Transactional
    public void processReport(Long reportId, Long handlerId, String actionType, String remark) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != null && report.getStatus() >= STATUS_RESOLVED) {
            throw new RuntimeException("该举报已处理，请勿重复操作");
        }

        if (report.getReportedType() == null || report.getReportedType() != TYPE_COMMENT) {
            throw new RuntimeException("当前举报类型不受理或已过期");
        }

        String normalizedAction = actionType != null ? actionType.trim().toUpperCase() : "";
        String finalRemark = StringUtils.hasText(remark) ? remark.trim() : null;

        switch (normalizedAction) {
            case "RESOLVE":
            case "PROCESS":
            case "HANDLE":
                try {
                    commentService.deleteComment(handlerId, report.getReportedId(), true);
                } catch (Exception e) {
                    throw new RuntimeException("处理举报失败（删除评论时出错）: " + e.getMessage(), e);
                }
                report.setStatus(STATUS_RESOLVED);
                report.setHandleResult(StringUtils.hasText(finalRemark) ? finalRemark : "评论已删除");
                break;
            case "REJECT":
                report.setStatus(STATUS_REJECTED);
                report.setHandleResult(StringUtils.hasText(finalRemark) ? finalRemark : "举报已驳回");
                break;
            default:
                throw new RuntimeException("不支持的处理动作，请选择处理或驳回");
        }

        report.setHandlerId(handlerId != null ? handlerId : 0L);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    @Override
    public boolean isReportedTooMuch(Integer reportedType, Long reportedId) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_type", reportedType)
               .eq("entity_id", reportedId)
               .eq("status", STATUS_PENDING);
        return reportMapper.selectCount(wrapper) >= 3;
    }

}

