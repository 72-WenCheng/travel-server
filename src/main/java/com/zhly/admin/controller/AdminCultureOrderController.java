package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.CultureOrder;
import com.zhly.mapper.CultureOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理端-文旅订单管理控制器
 */
@Tag(name = "管理端-文旅订单管理")
@RestController
@RequestMapping("/api/admin/culture/order")
@RequiredArgsConstructor
public class AdminCultureOrderController {
    
    private final CultureOrderMapper cultureOrderMapper;
    
    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<Page<CultureOrder>> pageOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) Integer orderStatus) {
        try {
            Page<CultureOrder> pageParam = new Page<>(page, size);
            Page<CultureOrder> result = cultureOrderMapper.selectOrderPage(pageParam, orderNo, productType, orderStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<CultureOrder> getOrder(@PathVariable Long id) {
        try {
            CultureOrder order = cultureOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "订单发货")
    @PutMapping("/{id}/ship")
    public Result<String> shipOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> shipInfo) {
        try {
            CultureOrder order = cultureOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (order.getOrderStatus() != 2) {
                return Result.error("只有已支付订单才能发货");
            }
            
            order.setOrderStatus(3);
            order.setShipCompany(shipInfo.get("shipCompany"));
            order.setShipNo(shipInfo.get("shipNo"));
            order.setShipTime(LocalDateTime.now());
            if (shipInfo.containsKey("remark")) {
                order.setRemark(shipInfo.get("remark"));
            }
            
            cultureOrderMapper.updateById(order);
            return Result.success("发货成功");
        } catch (Exception e) {
            return Result.error("发货失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<String> cancelOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> cancelInfo) {
        try {
            CultureOrder order = cultureOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (order.getOrderStatus() != 1) {
                return Result.error("只有待支付订单才能取消");
            }
            
            order.setOrderStatus(5);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason(cancelInfo.get("cancelReason"));
            
            cultureOrderMapper.updateById(order);
            return Result.success("订单已取消");
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取订单统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", cultureOrderMapper.selectCount(null));
            stats.put("pendingOrders", cultureOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CultureOrder>()
                    .eq("order_status", 1)));
            stats.put("paidOrders", cultureOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CultureOrder>()
                    .eq("order_status", 2)));
            stats.put("shippedOrders", cultureOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CultureOrder>()
                    .eq("order_status", 3)));
            stats.put("completedOrders", cultureOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CultureOrder>()
                    .eq("order_status", 4)));
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "导出订单")
    @GetMapping("/export")
    public void exportOrders(
            HttpServletResponse response,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) Integer orderStatus) {
        try {
            List<CultureOrder> orders = cultureOrderMapper.selectOrderListForExport(orderNo, productType, orderStatus);
            exportToCSV(response, orders);
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                // ignore
            }
        }
    }
    
    private void exportToCSV(HttpServletResponse response, List<CultureOrder> orders) throws IOException {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=orders_" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        response.setCharacterEncoding("UTF-8");
        
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);
        
        PrintWriter writer = response.getWriter();
        writer.println("订单编号,产品类型,产品名称,数量,订单金额,优惠金额,实付金额,订单状态,联系人,联系电话,创建时间");
        
        for (CultureOrder order : orders) {
            writer.println(String.format("%s,%s,%s,%d,%.2f,%.2f,%.2f,%s,%s,%s,%s",
                escapeCSV(order.getOrderNo()),
                getProductTypeText(order.getProductType()),
                escapeCSV(order.getProductName()),
                order.getQuantity(),
                order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0,
                order.getCouponDiscount() != null ? order.getCouponDiscount().doubleValue() : 0.0,
                order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0,
                getOrderStatusText(order.getOrderStatus()),
                escapeCSV(order.getContactName()),
                escapeCSV(order.getContactPhone()),
                order.getCreateTime() != null ? order.getCreateTime().toString() : ""
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
    
    private String getProductTypeText(Integer type) {
        if (type == null) return "";
        Map<Integer, String> types = Map.of(1, "特色产品", 2, "文化体验", 3, "服务");
        return types.getOrDefault(type, "");
    }
    
    private String getOrderStatusText(Integer status) {
        if (status == null) return "";
        Map<Integer, String> statuses = Map.of(1, "待支付", 2, "已支付", 3, "已发货", 4, "已完成", 5, "已取消");
        return statuses.getOrDefault(status, "");
    }
}

