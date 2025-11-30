package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.CultureBooking;
import com.zhly.mapper.CultureBookingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理端-文旅预订管理控制器
 */
@Tag(name = "管理端-文旅预订管理")
@RestController
@RequestMapping("/api/admin/culture/booking")
@RequiredArgsConstructor
public class AdminCultureBookingController {
    
    private final CultureBookingMapper cultureBookingMapper;
    
    @Operation(summary = "分页查询预订")
    @GetMapping("/page")
    public Result<Page<CultureBooking>> pageBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bookingNo,
            @RequestParam(required = false) Integer bookingType,
            @RequestParam(required = false) Integer status) {
        try {
            Page<CultureBooking> pageParam = new Page<>(page, size);
            Page<CultureBooking> result = cultureBookingMapper.selectBookingPage(pageParam, bookingNo, bookingType, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询预订失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取预订详情")
    @GetMapping("/{id}")
    public Result<CultureBooking> getBooking(@PathVariable Long id) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            return Result.success(booking);
        } catch (Exception e) {
            return Result.error("获取预订详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "确认预订")
    @PutMapping("/{id}/confirm")
    public Result<String> confirmBooking(@PathVariable Long id) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            if (booking.getStatus() != 1) {
                return Result.error("只有待确认预订才能确认");
            }
            
            booking.setStatus(2);
            booking.setConfirmTime(LocalDateTime.now());
            cultureBookingMapper.updateById(booking);
            return Result.success("确认成功");
        } catch (Exception e) {
            return Result.error("确认失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "完成预订")
    @PutMapping("/{id}/complete")
    public Result<String> completeBooking(@PathVariable Long id) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            if (booking.getStatus() != 2) {
                return Result.error("只有已确认预订才能完成");
            }
            
            booking.setStatus(3);
            booking.setCompleteTime(LocalDateTime.now());
            cultureBookingMapper.updateById(booking);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "取消预订")
    @PutMapping("/{id}/cancel")
    public Result<String> cancelBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> cancelInfo) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            if (booking.getStatus() != 1 && booking.getStatus() != 2) {
                return Result.error("只有待确认或已确认预订才能取消");
            }
            
            booking.setStatus(4);
            booking.setCancelTime(LocalDateTime.now());
            booking.setCancelReason(cancelInfo.get("cancelReason"));
            cultureBookingMapper.updateById(booking);
            return Result.success("预订已取消");
        } catch (Exception e) {
            return Result.error("取消失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "导出预订")
    @GetMapping("/export")
    public void exportBookings(
            HttpServletResponse response,
            @RequestParam(required = false) String bookingNo,
            @RequestParam(required = false) Integer bookingType,
            @RequestParam(required = false) Integer status) {
        try {
            List<CultureBooking> bookings = cultureBookingMapper.selectBookingListForExport(bookingNo, bookingType, status);
            exportToCSV(response, bookings);
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                // ignore
            }
        }
    }
    
    private void exportToCSV(HttpServletResponse response, List<CultureBooking> bookings) throws IOException {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=bookings_" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        response.setCharacterEncoding("UTF-8");
        
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);
        
        PrintWriter writer = response.getWriter();
        writer.println("预订编号,预订类型,项目名称,预订日期,预订时间,人数,价格,总金额,状态,联系人,联系电话,创建时间");
        
        for (CultureBooking booking : bookings) {
            writer.println(String.format("%s,%s,%s,%s,%s,%d,%.2f,%.2f,%s,%s,%s,%s",
                escapeCSV(booking.getBookingNo()),
                getBookingTypeText(booking.getBookingType()),
                escapeCSV(booking.getItemName()),
                booking.getBookingDate() != null ? booking.getBookingDate().toString() : "",
                booking.getBookingTime() != null ? booking.getBookingTime().toString() : "",
                booking.getPeopleCount(),
                booking.getPrice() != null ? booking.getPrice().doubleValue() : 0.0,
                booking.getTotalAmount() != null ? booking.getTotalAmount().doubleValue() : 0.0,
                getBookingStatusText(booking.getStatus()),
                escapeCSV(booking.getContactName()),
                escapeCSV(booking.getContactPhone()),
                booking.getCreateTime() != null ? booking.getCreateTime().toString() : ""
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
    
    private String getBookingTypeText(Integer type) {
        if (type == null) return "";
        Map<Integer, String> types = Map.of(1, "体验", 2, "服务");
        return types.getOrDefault(type, "");
    }
    
    private String getBookingStatusText(Integer status) {
        if (status == null) return "";
        Map<Integer, String> statuses = Map.of(1, "待确认", 2, "已确认", 3, "已完成", 4, "已取消");
        return statuses.getOrDefault(status, "");
    }
}

