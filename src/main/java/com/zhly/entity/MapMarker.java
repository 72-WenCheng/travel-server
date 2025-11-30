package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地图标记实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("map_marker")
public class MapMarker implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标记类型（1-景点，2-酒店，3-餐厅，4-交通站点，5-停车场，6-厕所，7-ATM，8-医院）
     */
    private Integer markerType;
    
    private String name;
    private String province;
    private String city;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String icon;
    private String description;
    private Long relatedId;
    private String apiSource;
    private String apiPoiId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







