package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report")
public class Report implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;
    
    /**
     * 被举报类型（1-攻略，2-评论，3-用户，4-景点评价）
     * 映射到数据库字段 entity_type
     */
    @TableField("entity_type")
    private Integer reportedType;
    
    /**
     * 被举报内容ID
     * 映射到数据库字段 entity_id
     */
    @TableField("entity_id")
    private Long reportedId;
    
    /**
     * 被举报用户ID（数据库表中可能不存在，保留以便未来扩展）
     */
    @TableField(exist = false)
    private Long reportedUserId;
    
    /**
     * 举报原因类型（1-违规内容，2-广告营销，3-虚假信息，4-恶意攻击，5-侵权，6-其他）
     * 注意：数据库表中只有 reason 字段（varchar），这里需要转换
     */
    @TableField(exist = false)
    private Integer reasonType;
    
    /**
     * 举报详细描述
     * 映射到数据库字段 description
     */
    @TableField("description")
    private String reasonDesc;
    
    /**
     * 证据图片（JSON）（数据库表中可能不存在）
     */
    @TableField(exist = false)
    private String evidenceImages;
    
    /**
     * 处理状态（0-待处理，1-处理中，2-已处理，3-已驳回）
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;
    
    /**
     * 处理结果
     */
    @TableField("handle_result")
    private String handleResult;
    
    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    
    /**
     * 举报时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 数据库表中的 reason 字段（varchar）
     * 用于存储举报原因
     */
    @TableField("reason")
    private String reason;
    
    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    private Integer deleted;
}







