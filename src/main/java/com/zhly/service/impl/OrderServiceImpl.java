package com.zhly.service.impl;

import com.zhly.entity.Order;
import com.zhly.mapper.OrderMapper;
import com.zhly.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    public boolean createOrder(Order order) {
        try {
            order.setOrderStatus(0); // 待支付状态
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            return orderMapper.insert(order) > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateOrderStatus(Long id, Integer orderStatus) {
        try {
            return orderMapper.updateOrderStatus(id, orderStatus) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新订单状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean payOrder(Long id, Integer paymentMethod) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            
            if (order.getOrderStatus() != 0) {
                throw new RuntimeException("订单状态不正确，无法支付");
            }
            
            order.setOrderStatus(1); // 已支付
            order.setPaymentMethod(paymentMethod);
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            return orderMapper.updateById(order) > 0;
        } catch (Exception e) {
            throw new RuntimeException("支付订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean cancelOrder(Long id) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            
            if (order.getOrderStatus() == 2) {
                throw new RuntimeException("订单已完成，无法取消");
            }
            
            return updateOrderStatus(id, 3); // 已取消
        } catch (Exception e) {
            throw new RuntimeException("取消订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean completeOrder(Long id) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            
            if (order.getOrderStatus() != 1) {
                throw new RuntimeException("订单状态不正确，无法完成");
            }
            
            return updateOrderStatus(id, 2); // 已完成
        } catch (Exception e) {
            throw new RuntimeException("完成订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserOrders(Long userId, Integer orderStatus, Integer page, Integer size) {
        try {
            List<Order> orders = orderMapper.selectUserOrders(userId, orderStatus, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("list", orders);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取用户订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public Order getOrderById(Long id) {
        try {
            return orderMapper.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("获取订单详情失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getOrderStatistics() {
        try {
            return orderMapper.selectOrderStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取订单统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        try {
            return orderMapper.selectUserOrderStats(userId);
        } catch (Exception e) {
            throw new RuntimeException("获取用户订单统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteOrder(Long id) {
        try {
            return orderMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除订单失败: " + e.getMessage());
        }
    }
}


















