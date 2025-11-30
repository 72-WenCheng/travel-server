package com.zhly.user.controller;

import com.zhly.common.R;
import com.zhly.entity.UserFollow;
import com.zhly.entity.User;
import com.zhly.mapper.UserMapper;
import com.zhly.service.IUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端-关注Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/user/follow")
public class UserFollowController {
    
    @Autowired
    private IUserFollowService userFollowService;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public R<String> follow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        userFollowService.followUser(followerId, followeeId);
        return R.ok("关注成功");
    }
    
    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    public R<String> unfollow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        userFollowService.unfollowUser(followerId, followeeId);
        return R.ok("已取消关注");
    }
    
    /**
     * 检查是否关注
     */
    @GetMapping("/is-following")
    public R<Map<String, Boolean>> isFollowing(@RequestParam Long followerId, @RequestParam Long followeeId) {
        boolean following = userFollowService.isFollowing(followerId, followeeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", following);
        return R.ok(result);
    }
    
    /**
     * 获取我的关注列表（返回用户详细信息）
     */
    @GetMapping("/following-list")
    public R<List<Map<String, Object>>> getFollowingList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<UserFollow> followList = userFollowService.getFollowingList(userId, page, limit);
        List<Map<String, Object>> result = followList.stream().map(follow -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", follow.getId());
            User followee = userMapper.selectById(follow.getFolloweeId());
            if (followee != null) {
                item.put("followeeId", followee.getId());
                item.put("followeeName", followee.getNickname() != null ? followee.getNickname() : followee.getUsername());
                item.put("followeeAvatar", followee.getAvatar());
            }
            item.put("createTime", follow.getCreateTime());
            return item;
        }).collect(Collectors.toList());
        return R.ok(result);
    }
    
    /**
     * 获取我的粉丝列表（返回用户详细信息）
     */
    @GetMapping("/followers-list")
    public R<List<Map<String, Object>>> getFollowersList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<UserFollow> followList = userFollowService.getFollowersList(userId, page, limit);
        List<Map<String, Object>> result = followList.stream().map(follow -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", follow.getId());
            User follower = userMapper.selectById(follow.getFollowerId());
            if (follower != null) {
                item.put("followerId", follower.getId());
                item.put("followerName", follower.getNickname() != null ? follower.getNickname() : follower.getUsername());
                item.put("followerAvatar", follower.getAvatar());
            }
            item.put("createTime", follow.getCreateTime());
            return item;
        }).collect(Collectors.toList());
        return R.ok(result);
    }
    
    /**
     * 获取关注统计
     */
    @GetMapping("/stats")
    public R<Map<String, Integer>> getStats(@RequestParam Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("following", userFollowService.getFollowingCount(userId));
        stats.put("followers", userFollowService.getFollowersCount(userId));
        return R.ok(stats);
    }
}







