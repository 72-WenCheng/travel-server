package com.zhly.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端用户实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class User {
    
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer userType;
    private Integer travelPreference;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String avatar;
    private String gender;
    private Integer age;
    private String location;
    private List<String> interests;
    private List<String> travelTypes;
    private String budget;
    private String relationship;
    private String travelStyle;
    private Integer points;
    private Integer level;
}



















