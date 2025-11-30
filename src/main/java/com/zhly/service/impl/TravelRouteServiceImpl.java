package com.zhly.service.impl;

import com.zhly.entity.TravelRoute;
import com.zhly.mapper.TravelRouteMapper;
import com.zhly.service.TravelRouteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游路线服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class TravelRouteServiceImpl extends ServiceImpl<TravelRouteMapper, TravelRoute> implements TravelRouteService {
    
    @Autowired
    private TravelRouteMapper travelRouteMapper;
    
    @Override
    public boolean addRoute(TravelRoute route) {
        try {
            route.setCreateTime(LocalDateTime.now());
            route.setUpdateTime(LocalDateTime.now());
            return travelRouteMapper.insert(route) > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateRoute(TravelRoute route) {
        try {
            route.setUpdateTime(LocalDateTime.now());
            return travelRouteMapper.updateById(route) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteRoute(Long id) {
        try {
            return travelRouteMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<TravelRoute> getPlanRoutes(Long planId) {
        try {
            return travelRouteMapper.selectPlanRoutes(planId);
        } catch (Exception e) {
            throw new RuntimeException("获取攻略路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<TravelRoute> getPlanDayRoutes(Long planId, Integer dayNumber) {
        try {
            return travelRouteMapper.selectPlanDayRoutes(planId, dayNumber);
        } catch (Exception e) {
            throw new RuntimeException("获取攻略某天路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean batchAddRoutes(List<TravelRoute> routes) {
        try {
            for (TravelRoute route : routes) {
                addRoute(route);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量添加路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateRouteOrder(Long id, Integer orderNumber) {
        try {
            return travelRouteMapper.updateRouteOrder(id, orderNumber) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新路线顺序失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getRouteStatistics() {
        try {
            return travelRouteMapper.selectRouteStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取路线统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getRouteList(Integer page, Integer size, String keyword, String startCity, String endCity, Integer status) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 获取路线列表
            List<TravelRoute> routes = travelRouteMapper.selectRouteList(offset, size, keyword, startCity, endCity, status);
            
            // 获取总数
            Long total = travelRouteMapper.selectRouteCount(keyword, startCity, endCity, status);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("routes", routes);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取路线列表失败: " + e.getMessage());
        }
    }
}

