package com.zhly.service;

import com.zhly.entity.AiGenerateLog;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 *
 * @author zhly
 * @since 2024-01-01
 */
public interface AiService extends IService<AiGenerateLog> {

    /**
     * 获取AI日志列表
     */
    Map<String, Object> getLogList(Integer page, Integer size, Long userId, Integer status);

    /**
     * 获取AI统计信息
     */
    Map<String, Object> getAiStatistics();

    /**
     * 获取用户AI统计
     */
    Map<String, Object> getUserAiStats(Long userId);

    /**
     * 生成AI内容
     */
    String generateContent(Long userId, String request, String modelName);

    /**
     * 记录AI生成日志
     */
    boolean logAiGenerate(Long userId, String request, String response, Integer status, Integer responseTime, String modelName, Integer tokensUsed, Double cost);

    /**
     * 清理过期日志
     */
    boolean cleanExpiredLogs(Integer days);

    /**
     * 删除AI日志
     */
    boolean deleteLog(Long id);
}