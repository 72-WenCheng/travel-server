package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户浏览历史实体类
 */
@Data
@TableName("user_browse_history")
public class UserBrowseHistory {
    /**
     * 浏览历史ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 浏览类型：1-景点，2-攻略，3-文旅项目，4-活动
     */
    private Integer browseType;

    /**
     * 浏览对象ID
     */
    private Long browseId;

    /**
     * 浏览对象标题
     */
    private String browseTitle;

    /**
     * 浏览对象图片
     */
    private String browseImage;

    /**
     * 浏览时长（秒）
     */
    private Integer duration;

    /**
     * 访问者IP
     */
    private String ipAddress;

    /**
     * 国家/地区代码（ISO-2，例如 CN、US）
     */
    private String countryCode;

    /**
     * 国家/地区名称（中文）
     */
    private String countryName;

    /**
     * 浏览时间
     */
    private LocalDateTime createTime;
}







