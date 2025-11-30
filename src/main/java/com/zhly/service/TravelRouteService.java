package com.zhly.service;

import com.zhly.entity.TravelRoute;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 旅游路线服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface TravelRouteService extends IService<TravelRoute> {
    
    /**
     * 添加路线
     */
    boolean addRoute(TravelRoute route);
    
    /**
     * 更新路线
     */
    boolean updateRoute(TravelRoute route);
    
    /**
     * 删除路线
     */
    boolean deleteRoute(Long id);
    
    /**
     * 获取攻略的所有路线
     */
    List<TravelRoute> getPlanRoutes(Long planId);
    
    /**
     * 获取攻略某天的路线
     */
    List<TravelRoute> getPlanDayRoutes(Long planId, Integer dayNumber);
    
    /**
     * 批量添加路线
     */
    boolean batchAddRoutes(List<TravelRoute> routes);
    
    /**
     * 更新路线顺序
     */
    boolean updateRouteOrder(Long id, Integer orderNumber);
    
    /**
     * 获取路线统计
     */
    Map<String, Object> getRouteStatistics();
    
    /**
     * 获取路线列表
     */
    Map<String, Object> getRouteList(Integer page, Integer size, String keyword, String startCity, String endCity, Integer status);
}

