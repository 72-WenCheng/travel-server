package com.zhly.mapper;

import com.zhly.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 订单数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 获取用户订单列表
     */
    List<Order> selectUserOrders(@Param("userId") Long userId,
                                 @Param("orderStatus") Integer orderStatus,
                                 @Param("page") Integer page,
                                 @Param("size") Integer size);
    
    /**
     * 获取订单统计
     */
    Map<String, Object> selectOrderStatistics();
    
    /**
     * 获取用户订单统计
     */
    Map<String, Object> selectUserOrderStats(@Param("userId") Long userId);
    
    /**
     * 更新订单状态
     */
    int updateOrderStatus(@Param("id") Long id, @Param("orderStatus") Integer orderStatus);
}


















