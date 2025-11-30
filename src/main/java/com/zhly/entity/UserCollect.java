package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户收藏实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_collect")
public class UserCollect {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer collectType; // 1-攻略, 2-景点, 3-文旅项目
    private Long collectId;
    private LocalDateTime createTime;
}



















