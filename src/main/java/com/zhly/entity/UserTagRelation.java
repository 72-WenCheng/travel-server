package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户标签关联实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_tag_relation")
public class UserTagRelation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    /**
     * 标签分类（travel_mode/preference/interest/city/season等）
     */
    private String tagCategory;
    
    private String tagName;
    private String tagValue;
    /**
     * 权重（用于推荐算法）
     */
    private Integer weight;
    
    /**
     * 来源（1-用户设置，2-系统分析）
     */
    private Integer source;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







