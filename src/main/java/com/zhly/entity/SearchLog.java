package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索日志实体类
 */
@Data
@TableName("search_log")
public class SearchLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID(可为空，支持未登录用户搜索)
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索类型(global/attractions/travel-plans/culture-projects/travel-routes)
     */
    private String searchType;

    /**
     * 结果数量
     */
    private Integer resultCount;

    /**
     * 点击的结果ID
     */
    private Long clickResultId;

    /**
     * 结果类型(attraction/plan/culture-project/travel-route)
     */
    private String clickResultType;

    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;
}













































































