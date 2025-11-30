package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.entity.CultureBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文旅预订Mapper
 */
@Mapper
public interface CultureBookingMapper extends BaseMapper<CultureBooking> {
    
    /**
     * 分页查询预订
     */
    Page<CultureBooking> selectBookingPage(
            Page<CultureBooking> page,
            @Param("bookingNo") String bookingNo,
            @Param("bookingType") Integer bookingType,
            @Param("status") Integer status);
    
    /**
     * 查询预订列表用于导出
     */
    List<CultureBooking> selectBookingListForExport(
            @Param("bookingNo") String bookingNo,
            @Param("bookingType") Integer bookingType,
            @Param("status") Integer status);
}

