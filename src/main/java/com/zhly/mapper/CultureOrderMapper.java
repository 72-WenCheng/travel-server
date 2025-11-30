package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.entity.CultureOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文旅订单Mapper
 */
@Mapper
public interface CultureOrderMapper extends BaseMapper<CultureOrder> {
    
    /**
     * 分页查询订单
     */
    Page<CultureOrder> selectOrderPage(
            Page<CultureOrder> page,
            @Param("orderNo") String orderNo,
            @Param("productType") Integer productType,
            @Param("orderStatus") Integer orderStatus);
    
    /**
     * 查询订单列表用于导出
     */
    List<CultureOrder> selectOrderListForExport(
            @Param("orderNo") String orderNo,
            @Param("productType") Integer productType,
            @Param("orderStatus") Integer orderStatus);
}

