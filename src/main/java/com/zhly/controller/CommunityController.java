package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.TravelPlan;
import com.zhly.entity.User;
import com.zhly.mapper.TravelPlanMapper;
import com.zhly.mapper.UserMapper;
import com.zhly.mapper.CommentMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区相关控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TravelPlanMapper travelPlanMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    /**
     * 获取热门作者排行榜
     */
    @GetMapping("/top-authors")
    public Result<List<Map<String, Object>>> getTopAuthors(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 获取所有已发布攻略，按作者分组统计
            QueryWrapper<TravelPlan> planWrapper = new QueryWrapper<>();
            planWrapper.eq("status", 1);
            planWrapper.select("author_id", 
                "COUNT(*) as plan_count",
                "SUM(view_count) as total_views",
                "SUM(like_count) as total_likes");
            planWrapper.groupBy("author_id");
            planWrapper.orderByDesc("plan_count", "total_likes");
            planWrapper.last("LIMIT " + limit);
            
            List<Map<String, Object>> authorStats = travelPlanMapper.selectMaps(planWrapper);
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Map<String, Object> stat : authorStats) {
                Long authorId = ((Number) stat.get("author_id")).longValue();
                User user = userMapper.selectById(authorId);
                
                if (user != null) {
                    Map<String, Object> author = new HashMap<>();
                    author.put("id", user.getId());
                    author.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    author.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
                    author.put("planCount", stat.get("plan_count") != null ? 
                        ((Number) stat.get("plan_count")).longValue() : 0L);
                    author.put("totalLikes", stat.get("total_likes") != null ? 
                        ((Number) stat.get("total_likes")).longValue() : 0L);
                    result.add(author);
                }
            }
            
            return Result.success("获取热门作者成功", result);
        } catch (Exception e) {
            return Result.error("获取热门作者失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门文章（支持时间段筛选）
     */
    @GetMapping("/hot-articles")
    public Result<List<Map<String, Object>>> getHotArticles(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            
            // 根据时间段筛选
            LocalDateTime now = LocalDateTime.now();
            if ("today".equals(period)) {
                LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
                wrapper.ge("create_time", todayStart);
            } else if ("week".equals(period)) {
                LocalDateTime weekStart = now.minusDays(7);
                wrapper.ge("create_time", weekStart);
            } else if ("month".equals(period)) {
                LocalDateTime monthStart = now.minusDays(30);
                wrapper.ge("create_time", monthStart);
            }
            
            wrapper.orderByDesc("view_count", "like_count");
            wrapper.last("LIMIT " + limit);
            
            List<TravelPlan> plans = travelPlanMapper.selectList(wrapper);
            List<Map<String, Object>> result = plans.stream().map(plan -> {
                Map<String, Object> article = new HashMap<>();
                article.put("id", plan.getId());
                article.put("title", plan.getTitle());
                article.put("viewCount", plan.getViewCount() != null ? plan.getViewCount() : 0);
                article.put("likeCount", plan.getLikeCount() != null ? plan.getLikeCount() : 0);
                
                // 获取作者信息
                if (plan.getAuthorId() != null) {
                    User author = userMapper.selectById(plan.getAuthorId());
                    if (author != null) {
                        article.put("authorName", author.getNickname() != null ? 
                            author.getNickname() : author.getUsername());
                    } else {
                        article.put("authorName", "匿名用户");
                    }
                } else {
                    article.put("authorName", "匿名用户");
                }
                
                return article;
            }).collect(Collectors.toList());
            
            return Result.success("获取热门文章成功", result);
        } catch (Exception e) {
            return Result.error("获取热门文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最新评论（关联攻略标题）
     */
    @GetMapping("/latest-comments")
    public Result<List<Map<String, Object>>> getLatestComments(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            QueryWrapper<com.zhly.entity.Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("content_type", "PLAN");
            wrapper.orderByDesc("created_time");
            wrapper.last("LIMIT " + limit);
            
            List<com.zhly.entity.Comment> comments = commentMapper.selectList(wrapper);
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (com.zhly.entity.Comment comment : comments) {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("id", comment.getId());
                
                // 获取用户信息
                if (comment.getUserId() != null) {
                    User user = userMapper.selectById(comment.getUserId());
                    if (user != null) {
                        commentData.put("username", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        commentData.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
                    } else {
                        commentData.put("username", "匿名用户");
                        commentData.put("avatar", "");
                    }
                } else {
                    commentData.put("username", "匿名用户");
                    commentData.put("avatar", "");
                }
                
                commentData.put("content", comment.getContent());
                commentData.put("createdTime", comment.getCreatedTime());
                commentData.put("contentId", comment.getContentId());
                
                // 获取攻略标题
                if (comment.getContentId() != null) {
                    TravelPlan plan = travelPlanMapper.selectById(comment.getContentId());
                    if (plan != null) {
                        commentData.put("planTitle", plan.getTitle());
                    } else {
                        commentData.put("planTitle", "攻略");
                    }
                } else {
                    commentData.put("planTitle", "攻略");
                }
                
                result.add(commentData);
            }
            
            return Result.success("获取最新评论成功", result);
        } catch (Exception e) {
            return Result.error("获取最新评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取推荐社区列表
     */
    @GetMapping("/recommended")
    public Result<List<Map<String, Object>>> getRecommendedCommunities() {
        try {
            List<Map<String, Object>> communities = new ArrayList<>();
            
            // 定义推荐社区（基于标签分类）
            String[][] communityData = {
                {"旅行达人社区", "分享旅行经验", "旅", "旅行"},
                {"摄影爱好者", "旅行摄影技巧", "摄", "摄影"},
                {"美食探索", "发现各地美食", "美", "美食"},
                {"自驾游联盟", "自驾游攻略", "自", "自驾"}
            };
            
            // 为每个社区统计相关攻略数量
            for (String[] data : communityData) {
                String name = data[0];
                String description = data[1];
                String icon = data[2];
                String tag = data[3];
                
                // 统计包含该标签的攻略数量
                QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
                wrapper.eq("status", 1);
                wrapper.like("tags", tag);
                Long planCount = travelPlanMapper.selectCount(wrapper);
                
                Map<String, Object> community = new HashMap<>();
                community.put("id", communities.size() + 1);
                community.put("name", name);
                community.put("description", description);
                community.put("icon", icon);
                community.put("tag", tag);
                community.put("planCount", planCount != null ? planCount : 0L);
                community.put("avatar", ""); // 可以后续添加头像URL
                
                communities.add(community);
            }
            
            return Result.success("获取推荐社区成功", communities);
        } catch (Exception e) {
            return Result.error("获取推荐社区失败: " + e.getMessage());
        }
    }
}

