package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文旅项目实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("culture_project")
public class CultureProject {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Integer type;
    private String region;
    private String location;
    private String description;
    private String image;
    private java.math.BigDecimal price;
    private java.math.BigDecimal rating;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;
    private Integer viewCount;
    private Integer orderCount;
    private java.math.BigDecimal revenue;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

