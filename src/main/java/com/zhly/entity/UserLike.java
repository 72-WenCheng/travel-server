package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户点赞实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_like")
public class UserLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer likeType;
    private Long likeId;
    private LocalDateTime createTime;
}


















