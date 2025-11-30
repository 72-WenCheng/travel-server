package com.zhly.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 管理端用户实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class AdminUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer userType;        // 个性化标签类型：1-个人, 2-情侣, 3-家庭, 4-团队
    private Integer role;            // 用户角色：1-管理员, 2-普通用户
    private Integer travelPreference;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private String avatar;
    private Integer points;          // 积分
    private Integer level;           // 等级
    private String interestTags;      // 兴趣爱好标签
    private String frequentCities;   // 常去城市
    private Integer deleted;          // 逻辑删除：0-未删除，1-已删除
}









