package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.Order;
import com.zhly.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<String> createOrder(@RequestBody Order order) {
        try {
            boolean success = orderService.createOrder(order);
            if (success) {
                return Result.success("创建订单成功");
            } else {
                return Result.error("创建订单失败");
            }
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新订单状态
     */
    @PostMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestParam Integer orderStatus) {
        try {
            boolean success = orderService.updateOrderStatus(id, orderStatus);
            if (success) {
                return Result.success("更新订单状态成功");
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<String> payOrder(@PathVariable Long id, @RequestParam Integer paymentMethod) {
        try {
            boolean success = orderService.payOrder(id, paymentMethod);
            if (success) {
                return Result.success("支付订单成功");
            } else {
                return Result.error("支付订单失败");
            }
        } catch (Exception e) {
            return Result.error("支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.cancelOrder(id);
            if (success) {
                return Result.success("取消订单成功");
            } else {
                return Result.error("取消订单失败");
            }
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成订单
     */
    @PostMapping("/{id}/complete")
    public Result<String> completeOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.completeOrder(id);
            if (success) {
                return Result.success("完成订单成功");
            } else {
                return Result.error("完成订单失败");
            }
        } catch (Exception e) {
            return Result.error("完成订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = orderService.getUserOrders(userId, orderStatus, page, size);
            return Result.success("获取用户订单成功", result);
        } catch (Exception e) {
            return Result.error("获取用户订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return Result.notFound("订单不存在");
            }
            return Result.success("获取订单详情成功", order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> result = orderService.getOrderStatistics();
            return Result.success("获取订单统计成功", result);
        } catch (Exception e) {
            return Result.error("获取订单统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单统计
     */
    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> getUserOrderStats(@PathVariable Long userId) {
        try {
            Map<String, Object> result = orderService.getUserOrderStats(userId);
            return Result.success("获取用户订单统计成功", result);
        } catch (Exception e) {
            return Result.error("获取用户订单统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.deleteOrder(id);
            if (success) {
                return Result.success("删除订单成功");
            } else {
                return Result.error("删除订单失败");
            }
        } catch (Exception e) {
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }
}


















