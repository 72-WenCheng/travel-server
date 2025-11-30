package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 公告实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("announcement")
public class Announcement {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 公告类型（1=系统公告, 2=活动公告, 3=功能更新, 4=维护通知）
     */
    private Integer type;
    
    /**
     * 发布状态（0=草稿, 1=已发布, 2=已下架）
     */
    private Integer status;
    
    /**
     * 是否置顶（0=否, 1=是）
     */
    private Integer isTop;
    
    /**
     * 排序权重（数字越大越靠前）
     */
    private Integer sortOrder;
    
    /**
     * 发布者ID
     */
    private Long publisherId;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

