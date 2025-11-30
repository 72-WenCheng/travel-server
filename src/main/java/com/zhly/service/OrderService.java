package com.zhly.service;

import com.zhly.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    boolean createOrder(Order order);
    
    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer orderStatus);
    
    /**
     * 支付订单
     */
    boolean payOrder(Long id, Integer paymentMethod);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long id);
    
    /**
     * 完成订单
     */
    boolean completeOrder(Long id);
    
    /**
     * 获取用户订单列表
     */
    Map<String, Object> getUserOrders(Long userId, Integer orderStatus, Integer page, Integer size);
    
    /**
     * 获取订单详情
     */
    Order getOrderById(Long id);
    
    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStatistics();
    
    /**
     * 获取用户订单统计
     */
    Map<String, Object> getUserOrderStats(Long userId);
    
    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);
}


















