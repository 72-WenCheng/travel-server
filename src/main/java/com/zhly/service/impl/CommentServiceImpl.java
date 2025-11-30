package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.*;
import com.zhly.mapper.CommentLikeMapper;
import com.zhly.mapper.CommentMapper;
import com.zhly.mapper.UserLevelMapper;
import com.zhly.mapper.UserMapper;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.service.ICommentService;
import com.zhly.service.INotificationService;
import com.zhly.service.IUserPointsService;
import com.zhly.service.TravelPlanService;
import com.zhly.service.AttractionService;
import com.zhly.service.CultureProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired(required = false)
    private IUserPointsService userPointsService;
    
    @Autowired(required = false)
    private INotificationService notificationService;
    
    @Autowired(required = false)
    private PointsLogMapper pointsLogMapper;
    
    @Autowired(required = false)
    private TravelPlanService travelPlanService;
    
    @Autowired(required = false)
    private AttractionService attractionService;
    
    @Autowired(required = false)
    private CultureProjectService cultureProjectService;
    
    /**
     * 发布评论（带权限检查和每日限制检查）
     */
    @Override
    @Transactional
    public Comment publishComment(Long userId, Comment comment) throws Exception {
        // 1. 权限检查
        if (!checkCommentPermission(userId)) {
            throw new Exception("您的等级不足，无法发布评论");
        }
        
        // 2. 检查今日评论次数限制
        if (userPointsService != null) {
            int todayComments = userPointsService.getTodayCommentCount(userId);
            UserPoints userPoints = userPointsService.getUserPointsWithLevel(userId);
            if (userPoints != null && userPoints.getCurrentLevelId() != null) {
                UserLevel currentLevel = userLevelMapper.selectById(userPoints.getCurrentLevelId());
                if (currentLevel != null && currentLevel.getDailyCommentLimit() != null) {
                    int dailyLimit = currentLevel.getDailyCommentLimit();
                    if (dailyLimit > 0 && todayComments >= dailyLimit) {
                        throw new Exception("今日评论次数已达上限（" + dailyLimit + "条），请明天再试");
                    }
                }
            }
        }
        
        // 设置评论信息
        comment.setUserId(userId);
        comment.setStatus("PUBLISHED");
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setIsTop(false);
        comment.setIsHot(false);
        comment.setCreatedTime(LocalDateTime.now());
        
        // 保存评论
        this.save(comment);
        
        // 更新内容的评论数
        commentMapper.updateContentCommentCount(comment.getContentType(), comment.getContentId());
        
        // 增加用户积分（每条有效评论奖励2积分）
        if (userPointsService != null) {
            try {
                // 参数：userId, points, actionType, actionDesc, relatedType, relatedId
                // 行为类型：3-评论（根据PointsLog实体定义）
                userPointsService.addPoints(userId, 2, 3, "发布评论", "COMMENT", comment.getId());
            } catch (Exception e) {
                log.error("增加评论积分失败", e);
            }
        }
        
        return comment;
    }
    
    /**
     * 回复评论（带权限检查）
     */
    @Override
    @Transactional
    public Comment replyComment(Long userId, Long parentId, Long replyToUserId, String content) throws Exception {
        // 权限检查
        if (!checkCommentPermission(userId)) {
            throw new Exception("您的等级不足，无法回复评论");
        }
        
        // 获取父评论
        Comment parentComment = this.getById(parentId);
        if (parentComment == null) {
            throw new Exception("父评论不存在");
        }
        
        // 禁止三级回复：如果父评论本身是回复（有parentId），则不能再回复
        if (parentComment.getParentId() != null) {
            throw new Exception("不能对回复进行回复，请直接回复原评论");
        }
        
        // 创建回复评论
        Comment reply = new Comment();
        reply.setContentType(parentComment.getContentType());
        reply.setContentId(parentComment.getContentId());
        reply.setUserId(userId);
        reply.setParentId(parentId);
        reply.setReplyToUserId(replyToUserId);
        reply.setContent(content);
        reply.setStatus("PUBLISHED");
        reply.setLikeCount(0);
        reply.setReplyCount(0);
        reply.setIsTop(false);
        reply.setIsHot(false);
        reply.setCreatedTime(LocalDateTime.now());
        
        // 保存回复
        this.save(reply);
        
        // 增加父评论的回复数
        commentMapper.increaseReplyCount(parentId);
        
        // 更新内容的评论数
        commentMapper.updateContentCommentCount(reply.getContentType(), reply.getContentId());
        
        // 增加用户积分
        if (userPointsService != null) {
            try {
                // 参数：userId, points, actionType, actionDesc, relatedType, relatedId
                userPointsService.addPoints(userId, 3, 2, "回复评论", "COMMENT", reply.getId());
            } catch (Exception e) {
                log.error("增加回复积分失败", e);
            }
        }
        
        // 发送通知给被回复的用户
        if (notificationService != null && !userId.equals(replyToUserId)) {
            try {
                // 参数：userId, notifyType, title, content, senderId, relatedType, relatedId, linkUrl
                notificationService.sendNotification(
                    replyToUserId,              // 接收者ID
                    1,                          // 通知类型：1=评论回复
                    "收到新回复",                // 标题
                    "有人回复了您的评论",        // 内容
                    userId,                     // 发送者ID
                    "COMMENT",                  // 关联类型
                    reply.getId(),              // 关联ID（回复ID）
                    "/comment/" + reply.getId() // 链接地址
                );
            } catch (Exception e) {
                log.error("发送回复通知失败", e);
            }
        }
        
        return reply;
    }
    
    /**
     * 获取内容的评论列表（分页，带回复）
     */
    @Override
    public Map<String, Object> getCommentsByContent(String contentType, Long contentId, Long currentUserId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询顶级评论
        List<Comment> topComments = commentMapper.selectTopComments(contentType, contentId, currentUserId, offset, pageSize);
        
        // 为每个顶级评论查询回复列表（只显示前3条，更多通过"查看更多"加载）
        for (Comment comment : topComments) {
            if (comment.getReplyCount() > 0) {
                List<Comment> replies = commentMapper.selectReplies(comment.getId(), currentUserId);
                // 只返回前3条回复
                if (replies.size() > 3) {
                    comment.setReplies(replies.subList(0, 3));
                } else {
                    comment.setReplies(replies);
                }
            }
        }
        
        // 查询总数
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getContentType, contentType)
                   .eq(Comment::getContentId, contentId)
                   .isNull(Comment::getParentId)
                   .eq(Comment::getStatus, "PUBLISHED");
        long total = this.count(queryWrapper);
        
        result.put("list", topComments);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }
    
    /**
     * 获取用户的评论列表
     */
    @Override
    public Map<String, Object> getUserComments(Long userId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        int offset = (page - 1) * pageSize;
        List<Comment> comments = commentMapper.selectUserComments(userId, offset, pageSize);
        
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                   .eq(Comment::getStatus, "PUBLISHED");
        long total = this.count(queryWrapper);
        
        result.put("list", comments);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }
    
    /**
     * 删除评论（用户只能删除自己的评论）
     */
    @Override
    @Transactional
    public boolean deleteComment(Long userId, Long commentId, boolean isAdmin) throws Exception {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new Exception("评论不存在");
        }
        
        // 权限检查：管理员可以删除任何评论，用户只能删除自己的评论
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new Exception("无权删除此评论");
        }
        
        // 软删除：修改状态为DELETED
        comment.setStatus("DELETED");
        comment.setUpdatedTime(LocalDateTime.now());
        this.updateById(comment);
        
        // 如果是回复，减少父评论的回复数
        if (comment.getParentId() != null) {
            commentMapper.decreaseReplyCount(comment.getParentId());
        }
        
        // 更新内容的评论数
        commentMapper.updateContentCommentCount(comment.getContentType(), comment.getContentId());
        
        return true;
    }
    
    /**
     * 点赞/取消点赞
     */
    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long commentId) throws Exception {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new Exception("评论不存在");
        }
        
        // 检查是否已点赞
        int likeCount = commentLikeMapper.checkUserLike(commentId, userId);
        
        if (likeCount > 0) {
            // 已点赞，取消点赞
            commentLikeMapper.deleteLike(commentId, userId);
            commentMapper.decreaseLikeCount(commentId);
            return false;
        } else {
            // 未点赞，添加点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreatedTime(LocalDateTime.now());
            commentLikeMapper.insert(like);
            commentMapper.increaseLikeCount(commentId);
            
            // 给评论作者增加积分（每个点赞都给积分）
            if (userPointsService != null && !userId.equals(comment.getUserId())) {
                try {
                    // 每个点赞都给积分，因为点赞记录本身就是唯一的（点赞者userId + 评论commentId的组合是唯一的）
                    // 参数：userId, points, actionType, actionDesc, relatedType, relatedId
                    userPointsService.addPoints(comment.getUserId(), 1, 4, "评论获得点赞", "COMMENT", commentId);
                    System.out.println("✅ 评论 " + commentId + " 获得点赞（点赞者: " + userId + "），已给作者 " + comment.getUserId() + " 奖励1积分");
                } catch (Exception e) {
                    log.error("增加点赞积分失败", e);
                }
            }
            
            // 发送通知给评论作者
            if (notificationService != null && !userId.equals(comment.getUserId())) {
                try {
                    // 参数：userId, notifyType, title, content, senderId, relatedType, relatedId, linkUrl
                    notificationService.sendNotification(
                        comment.getUserId(),        // 接收者ID（评论作者）
                        2,                          // 通知类型：2=评论点赞
                        "收到新点赞",                // 标题
                        "有人点赞了您的评论",        // 内容
                        userId,                     // 发送者ID（点赞的用户）
                        "COMMENT",                  // 关联类型
                        commentId,                  // 关联ID
                        "/comment/" + commentId     // 链接地址
                    );
                } catch (Exception e) {
                    log.error("发送点赞通知失败", e);
                }
            }
            
            return true;
        }
    }
    
    /**
     * 管理员操作：置顶/取消置顶
     */
    @Override
    public boolean toggleTop(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setIsTop(!comment.getIsTop());
            comment.setUpdatedTime(LocalDateTime.now());
            return this.updateById(comment);
        }
        return false;
    }
    
    /**
     * 管理员操作：隐藏评论
     */
    @Override
    @Transactional
    public boolean hideComment(Long commentId) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setStatus("HIDDEN");
            comment.setUpdatedTime(LocalDateTime.now());
            this.updateById(comment);
            
            // 更新内容的评论数
            commentMapper.updateContentCommentCount(comment.getContentType(), comment.getContentId());
            
            return true;
        }
        return false;
    }
    
    /**
     * 管理员操作：批量删除评论
     */
    @Override
    @Transactional
    public boolean batchDelete(List<Long> commentIds) {
        for (Long commentId : commentIds) {
            try {
                deleteComment(null, commentId, true);
            } catch (Exception e) {
                log.error("批量删除评论失败: " + commentId, e);
            }
        }
        return true;
    }
    
    /**
     * 获取管理端评论列表（所有评论）
     */
    @Override
    public Map<String, Object> getAdminComments(String contentType, String keyword, String status, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        
        // 条件筛选
        if (contentType != null && !contentType.isEmpty() && !"ALL".equals(contentType)) {
            queryWrapper.eq("content_type", contentType);
        }
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            queryWrapper.eq("status", status);
        }
        
        // 关键词搜索：支持评论内容和用户名搜索
        if (keyword != null && !keyword.isEmpty()) {
            // 先查询匹配用户名的用户ID列表
            List<Long> userIds = new ArrayList<>();
            if (userMapper != null) {
                try {
                    QueryWrapper<User> userQuery = new QueryWrapper<>();
                    userQuery.like("username", keyword).or().like("nickname", keyword);
                    List<User> users = userMapper.selectList(userQuery);
                    for (User user : users) {
                        userIds.add(user.getId());
                    }
                } catch (Exception e) {
                    log.error("查询用户失败", e);
                }
            }
            
            // 构建评论搜索条件
            if (!userIds.isEmpty()) {
                queryWrapper.and(wrapper -> {
                    wrapper.like("content", keyword);
                    if (!userIds.isEmpty()) {
                        wrapper.or().in("user_id", userIds);
                    }
                });
            } else {
                queryWrapper.like("content", keyword);
            }
        }
        
        // 排序：置顶优先，然后按创建时间倒序
        queryWrapper.orderByDesc("is_top").orderByDesc("created_time");
        
        // 分页查询
        Page<Comment> pageObj = new Page<>(page, pageSize);
        Page<Comment> pageResult = this.page(pageObj, queryWrapper);
        
        // 转换为包含用户信息和内容标题的Map列表
        List<Map<String, Object>> list = new ArrayList<>();
        for (Comment comment : pageResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("userId", comment.getUserId());
            map.put("contentType", comment.getContentType());
            map.put("contentId", comment.getContentId());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("likeCount", comment.getLikeCount() != null ? comment.getLikeCount() : 0);
            map.put("replyCount", comment.getReplyCount() != null ? comment.getReplyCount() : 0);
            map.put("status", comment.getStatus());
            map.put("isTop", comment.getIsTop() != null ? comment.getIsTop() : false);
            map.put("isHot", comment.getIsHot() != null ? comment.getIsHot() : false);
            map.put("createdTime", comment.getCreatedTime());
            
            // 关联查询用户信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                map.put("username", user.getUsername());
                map.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                map.put("username", "用户" + comment.getUserId());
                map.put("avatar", "");
            }
            
            // 关联查询内容标题
            String contentTitle = getContentTitle(comment.getContentType(), comment.getContentId());
            map.put("contentTitle", contentTitle);
            
            list.add(map);
        }
        
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }
    
    /**
     * 获取内容标题（根据内容类型和ID）
     */
    private String getContentTitle(String contentType, Long contentId) {
        if (contentId == null) {
            return "内容已删除";
        }
        
        try {
            if ("PLAN".equals(contentType) && travelPlanService != null) {
                TravelPlan plan = travelPlanService.getPlanById(contentId, true);
                return plan != null ? plan.getTitle() : "内容已删除";
            } else if ("ATTRACTION".equals(contentType) && attractionService != null) {
                Attraction attraction = attractionService.getAttractionById(contentId);
                return attraction != null ? attraction.getName() : "内容已删除";
            } else if ("CULTURE".equals(contentType) && cultureProjectService != null) {
                CultureProject project = cultureProjectService.getProjectById(contentId);
                return project != null ? project.getName() : "内容已删除";
            }
        } catch (Exception e) {
            System.err.println("获取内容标题失败: contentType=" + contentType + ", contentId=" + contentId + ", error=" + e.getMessage());
        }
        
        return "内容已删除";
    }
    
    /**
     * 检查用户评论权限
     */
    @Override
    public boolean checkCommentPermission(Long userId) throws Exception {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new Exception("用户不存在");
        }
        
        // 查询用户积分信息（包含等级）
        if (userPointsService != null) {
            try {
                UserPoints userPoints = userPointsService.getUserPointsWithLevel(userId);
                if (userPoints != null && userPoints.getCurrentLevelId() != null) {
                UserLevel level = userLevelMapper.selectById(userPoints.getCurrentLevelId());
                if (level != null) {
                    // 检查是否有评论权限（0-否，1-是）
                    return level.getCanComment() != null && level.getCanComment() == 1;
                }
                }
            } catch (Exception e) {
                log.error("检查评论权限失败", e);
            }
        }
        
        // 默认允许评论
        return true;
    }
    
    /**
     * 获取评论统计数据
     */
    @Override
    public Map<String, Object> getCommentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总评论数
            LambdaQueryWrapper<Comment> totalQuery = new LambdaQueryWrapper<>();
            long totalCount = this.count(totalQuery);
            stats.put("totalCount", totalCount);
            
            // 已发布
            LambdaQueryWrapper<Comment> publishedQuery = new LambdaQueryWrapper<>();
            publishedQuery.eq(Comment::getStatus, "PUBLISHED");
            long publishedCount = this.count(publishedQuery);
            stats.put("publishedCount", publishedCount);
            
            // 已隐藏
            LambdaQueryWrapper<Comment> hiddenQuery = new LambdaQueryWrapper<>();
            hiddenQuery.eq(Comment::getStatus, "HIDDEN");
            long hiddenCount = this.count(hiddenQuery);
            stats.put("hiddenCount", hiddenCount);
            
            // 已删除
            LambdaQueryWrapper<Comment> deletedQuery = new LambdaQueryWrapper<>();
            deletedQuery.eq(Comment::getStatus, "DELETED");
            long deletedCount = this.count(deletedQuery);
            stats.put("deletedCount", deletedCount);
            
            // 按内容类型统计
            Map<String, Long> contentTypeStats = new HashMap<>();
            LambdaQueryWrapper<Comment> planQuery = new LambdaQueryWrapper<>();
            planQuery.eq(Comment::getContentType, "PLAN");
            contentTypeStats.put("PLAN", this.count(planQuery));
            
            LambdaQueryWrapper<Comment> attractionQuery = new LambdaQueryWrapper<>();
            attractionQuery.eq(Comment::getContentType, "ATTRACTION");
            contentTypeStats.put("ATTRACTION", this.count(attractionQuery));
            
            LambdaQueryWrapper<Comment> cultureQuery = new LambdaQueryWrapper<>();
            cultureQuery.eq(Comment::getContentType, "CULTURE");
            contentTypeStats.put("CULTURE", this.count(cultureQuery));
            
            stats.put("contentTypeStats", contentTypeStats);
            
        } catch (Exception e) {
            log.error("获取评论统计失败", e);
            // 返回默认值
            stats.put("totalCount", 0L);
            stats.put("publishedCount", 0L);
            stats.put("hiddenCount", 0L);
            stats.put("deletedCount", 0L);
        }
        
        return stats;
    }
    
    /**
     * 获取评论的所有回复列表
     */
    @Override
    public List<Comment> getRepliesByParentId(Long parentId, Long currentUserId) {
        try {
            return commentMapper.selectReplies(parentId, currentUserId);
        } catch (Exception e) {
            log.error("获取回复列表失败", e);
            return new ArrayList<>();
        }
    }
}
