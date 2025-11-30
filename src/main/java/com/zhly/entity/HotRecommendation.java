package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 热门推荐实体类
 */
@Data
@TableName("hot_recommendation")
public class HotRecommendation {
    /**
     * 推荐ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 推荐类型：1-热门景点，2-热门攻略，3-新品推荐，4-编辑推荐
     */
    private Integer recommendType;

    /**
     * 项目类型：1-景点，2-攻略，3-文旅项目
     */
    private Integer itemType;

    /**
     * 项目ID
     */
    private Long itemId;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 开始推荐时间
     */
    private LocalDateTime startTime;

    /**
     * 结束推荐时间
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







