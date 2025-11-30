package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("banner")
public class Banner {
    /**
     * 轮播图ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 标签/徽章（显示在轮播图角标）
     */
    private String label;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 显示位置：HOME-用户平台首页，CULTURE-用户平台文化页面，ADMIN_HOME-管理平台首页
     */
    private String position;

    /**
     * 链接类型：0-无链接，1-内部页面，2-外部链接，3-景点详情，4-攻略详情，5-活动详情
     */
    private Integer linkType;

    /**
     * 链接值（页面路径/URL/ID等）
     */
    private String linkValue;

    /**
     * 显示顺序（数字越小越靠前）
     */
    private Integer displayOrder;

    /**
     * 开始展示时间
     */
    private LocalDateTime startTime;

    /**
     * 结束展示时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}







