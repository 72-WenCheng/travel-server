package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.Comment;
import com.zhly.entity.Report;
import com.zhly.service.ICommentService;
import com.zhly.service.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-举报审核Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Tag(name = "管理端-举报审核")
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private static final int TYPE_COMMENT = 3;

    private final IReportService reportService;
    private final ICommentService commentService;

    /**
     * 举报列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询举报列表")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reportedType) {
        try {
            Page<Report> pageObj = new Page<>(page, size);
            QueryWrapper<Report> wrapper = new QueryWrapper<>();

            if (status != null) {
                wrapper.eq("status", status);
            }
            if (reportedType != null) {
                wrapper.eq("entity_type", reportedType);
            } else {
                wrapper.eq("entity_type", TYPE_COMMENT);
            }

            wrapper.orderByDesc("create_time");
            reportService.page(pageObj, wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageObj.getRecords());
            result.put("total", pageObj.getTotal());
            result.put("page", page);
            result.put("size", size);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 举报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取举报详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        try {
            Report report = reportService.getById(id);
            if (report == null) {
                return Result.error("举报不存在");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("report", report);
            if (report.getReportedId() != null) {
                Comment comment = commentService.getById(report.getReportedId());
                result.put("comment", comment);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报
     */
    @PostMapping("/{id}/handle")
    @Operation(summary = "处理举报")
    public Result<String> handle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        try {
            Long handlerId = params.get("handlerId") != null ?
                Long.valueOf(params.get("handlerId").toString()) : 1L;
            String action = params.get("action") != null ?
                params.get("action").toString() : "";
            String remark = params.get("remark") != null ?
                params.get("remark").toString() : "";

            reportService.processReport(id, handlerId, action, remark);

            return Result.success("处理成功");
        } catch (Exception e) {
            return Result.error("处理失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除举报
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除举报")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的举报");
            }
            boolean success = reportService.removeByIds(ids);
            return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 举报统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取举报统计")
    public Result<Map<String, Object>> stats() {
        try {
            Map<String, Object> result = new HashMap<>();

            result.put("pending", countByStatus(0));
            result.put("resolved", countByStatus(2));
            result.put("rejected", countByStatus(3));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    private Long countByStatus(int status) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_type", TYPE_COMMENT);
        wrapper.eq("status", status);
        return reportService.count(wrapper);
    }
}
