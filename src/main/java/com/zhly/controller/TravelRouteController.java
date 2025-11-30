package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.TravelRoute;
import com.zhly.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 旅游路线控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/travel-route")
public class TravelRouteController {
    
    @Autowired
    private TravelRouteService travelRouteService;
    
    /**
     * 添加路线
     */
    @PostMapping
    public Result<String> addRoute(@RequestBody TravelRoute route) {
        try {
            boolean success = travelRouteService.addRoute(route);
            if (success) {
                return Result.success("添加路线成功");
            } else {
                return Result.error("添加路线失败");
            }
        } catch (Exception e) {
            return Result.error("添加路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新路线
     */
    @PutMapping("/{id}")
    public Result<String> updateRoute(@PathVariable Long id, @RequestBody TravelRoute route) {
        try {
            route.setId(id);
            boolean success = travelRouteService.updateRoute(route);
            if (success) {
                return Result.success("更新路线成功");
            } else {
                return Result.error("更新路线失败");
            }
        } catch (Exception e) {
            return Result.error("更新路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除路线
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRoute(@PathVariable Long id) {
        try {
            boolean success = travelRouteService.deleteRoute(id);
            if (success) {
                return Result.success("删除路线成功");
            } else {
                return Result.error("删除路线失败");
            }
        } catch (Exception e) {
            return Result.error("删除路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取攻略的所有路线
     */
    @GetMapping("/plan/{planId}")
    public Result<List<TravelRoute>> getPlanRoutes(@PathVariable Long planId) {
        try {
            List<TravelRoute> routes = travelRouteService.getPlanRoutes(planId);
            return Result.success("获取攻略路线成功", routes);
        } catch (Exception e) {
            return Result.error("获取攻略路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取攻略某天的路线
     */
    @GetMapping("/plan/{planId}/day/{dayNumber}")
    public Result<List<TravelRoute>> getPlanDayRoutes(
            @PathVariable Long planId,
            @PathVariable Integer dayNumber) {
        try {
            List<TravelRoute> routes = travelRouteService.getPlanDayRoutes(planId, dayNumber);
            return Result.success("获取攻略某天路线成功", routes);
        } catch (Exception e) {
            return Result.error("获取攻略某天路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量添加路线
     */
    @PostMapping("/batch")
    public Result<String> batchAddRoutes(@RequestBody List<TravelRoute> routes) {
        try {
            boolean success = travelRouteService.batchAddRoutes(routes);
            if (success) {
                return Result.success("批量添加路线成功");
            } else {
                return Result.error("批量添加路线失败");
            }
        } catch (Exception e) {
            return Result.error("批量添加路线失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新路线顺序
     */
    @PostMapping("/{id}/order")
    public Result<String> updateRouteOrder(@PathVariable Long id, @RequestParam Integer orderNumber) {
        try {
            boolean success = travelRouteService.updateRouteOrder(id, orderNumber);
            if (success) {
                return Result.success("更新路线顺序成功");
            } else {
                return Result.error("更新路线顺序失败");
            }
        } catch (Exception e) {
            return Result.error("更新路线顺序失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取路线统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getRouteStatistics() {
        try {
            Map<String, Object> result = travelRouteService.getRouteStatistics();
            return Result.success("获取路线统计成功", result);
        } catch (Exception e) {
            return Result.error("获取路线统计失败: " + e.getMessage());
        }
    }
}


















