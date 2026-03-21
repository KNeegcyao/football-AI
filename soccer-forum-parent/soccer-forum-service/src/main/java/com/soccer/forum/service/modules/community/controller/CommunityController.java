package com.soccer.forum.service.modules.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.modules.community.model.PostDetailResp;
import com.soccer.forum.service.modules.community.model.PostPageReq;
import com.soccer.forum.service.modules.user.model.LoginUser;
import com.soccer.forum.service.modules.community.service.PostService;
import com.soccer.forum.service.modules.match.service.TeamService;
import com.soccer.forum.service.modules.community.service.TopicService;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.modules.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 社区相关接口控制器
 * <p>
 * 提供社区热门圈子、趋势话题等数据
 * </p>
 */
@Tag(name = "社区管理", description = "社区发现页接口")
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

    @Value("${file.access-path:http://localhost:8080/uploads/}")
    private String accessPath;

    private final TeamService teamService;
    private final TopicService topicService;
    private final PostService postService;
    private final UserService userService;

    public CommunityController(TeamService teamService, TopicService topicService, PostService postService, UserService userService) {
        this.teamService = teamService;
        this.topicService = topicService;
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * 获取圈子帖子列表
     */
    @Operation(summary = "获取圈子帖子列表", description = "获取指定圈子（球队）相关的帖子列表")
    @GetMapping("/circles/{name}/posts")
    public R<Page<PostDetailResp>> getCirclePosts(@Parameter(description = "圈子名称") @PathVariable String name, 
                                       @Validated PostPageReq req,
                                       @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.debug("获取圈子帖子列表: name={}, req={}", name, req);
        // 尝试根据名称查找圈子ID，优先使用精确的 circleId 查询
        List<Team> teams = teamService.getTeamsByNames(List.of(name));
        
        // 如果未找到且名称以“队”结尾，尝试去除后缀再次查找
        if (teams.isEmpty() && name.endsWith("队")) {
            String cleanName = name.substring(0, name.length() - 1);
            teams = teamService.getTeamsByNames(List.of(cleanName));
        }
        
        if (!teams.isEmpty()) {
            req.setCircleId(teams.get(0).getId());
        } else {
            // 找不到圈子时，回退到按关键词搜索
            req.setKeyword(name);
        }
        Long userId = (loginUser != null && loginUser.getUser() != null) ? loginUser.getUser().getId() : null;
        return R.ok(postService.getPostPage(req, userId));
    }

    /**
     * 获取话题详情
     */
    @Operation(summary = "获取话题详情", description = "根据ID获取话题详情，并增加阅读量")
    @GetMapping("/topics/{id}")
    public R<Topic> getTopicDetail(@Parameter(description = "话题ID") @PathVariable Long id) {
        log.debug("获取话题详情: id={}", id);
        Topic topic = topicService.incrementViewCountAndSyncPostCount(id);
        if (topic != null) {
            if (topic.getDescription() == null || topic.getDescription().trim().isEmpty()) {
                String title = topic.getTitle().replace("#", "");
                topic.setDescription("关于“" + title + "”的最新讨论，点击参与互动");
            }
        }
        return R.ok(topic);
    }

    /**
     * 获取话题帖子列表
     */
    @Operation(summary = "获取话题帖子列表", description = "获取指定话题相关的帖子列表")
    @GetMapping("/topics/posts")
    public R<Map<String, Object>> getTopicPosts(@Parameter(description = "话题标题") @RequestParam String title, 
                                      @Validated PostPageReq req,
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.debug("获取话题帖子列表: title={}, req={}", title, req);
        // 尝试根据标题查找话题ID，优先使用精确的 topicId 查询
        Topic topic = topicService.getOne(new LambdaQueryWrapper<Topic>()
                .eq(Topic::getTitle, title), false);
        
        // 如果没找到，尝试加上 # 前缀查找
        if (topic == null && !title.startsWith("#")) {
            topic = topicService.getOne(new LambdaQueryWrapper<Topic>()
                    .eq(Topic::getTitle, "#" + title), false);
        }
        
        // 如果还没找到，尝试加# 前缀和后缀查找
        if (topic == null && !title.startsWith("#")) {
            topic = topicService.getOne(new LambdaQueryWrapper<Topic>()
                    .eq(Topic::getTitle, "#" + title + "#"), false);
        }
        
        if (topic != null) {
            req.setTopicId(topic.getId());
        } else {
            req.setKeyword(title);
        }
        Long userId = (loginUser != null && loginUser.getUser() != null) ? loginUser.getUser().getId() : null;
        Page<PostDetailResp> postPage = postService.getPostPage(req, userId);
        
        // 兜底策略：如果通过标题没找到话题，但通过关键字搜到了帖子，从帖子中提取topicId
        if (topic == null && postPage.getTotal() > 0) {
            // 找到第一个有 topicId 的帖子
            for (PostDetailResp p : postPage.getRecords()) {
                if (p.getTopicId() != null) {
                    topic = topicService.getById(p.getTopicId());
                    if (topic != null) {
                        log.debug("通过帖子找到话题 ID={}, 标题={}", topic.getId(), topic.getTitle());
                        break;
                    }
                }
            }
        }

        // 如果最终找到了话题，进行统计数据校准
        if (topic != null) {
            topic = topicService.incrementViewCountAndSyncPostCount(topic.getId());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("posts", postPage);
        if (topic != null) {
            result.put("topic", topic);
        }
        
        return R.ok(result);
    }

    /**
     * 获取热门圈子
     */
    @Operation(summary = "获取热门圈子", description = "获取社区首页的热门圈子列表")
    @GetMapping("/circles/hot")
    public R<List<Map<String, Object>>> getHotCircles() {
        log.debug("获取热门圈子");
        // 获取所有标记为推荐的球队（Top 5）
        List<Team> teams = teamService.listRecommendTeams();
        
        List<Map<String, Object>> circles = teams.stream().map(team -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            // 模拟成员数，基于ID生成一个确定的数字，例如ID*1000 + 50000
            long memberCount = 50000 + (team.getId() * 1234) % 900000;
            String memberStr = String.format("%.1f万", memberCount / 10000.0);
            map.put("members", memberStr);
            map.put("image", team.getLogoUrl());
            return map;
        }).collect(Collectors.toList());

        return R.ok(circles);
    }

    /**
     * 分页获取所有圈子
     */
    @Operation(summary = "获取所有圈子", description = "分页获取所有圈子列表，支持热门筛选")
    @GetMapping("/circles")
    public R<Map<String, Object>> getCircles(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer size,
                                           @RequestParam(required = false) Boolean isHot) {
        log.debug("获取所有圈子: page={}, size={}, isHot={}", page, size, isHot);
        Page<Team> teamPage = teamService.listTeams(page, size, null, isHot);
        List<Team> teams = teamPage.getRecords();
        
        List<Map<String, Object>> circles = teams.stream().map(team -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            long memberCount = 50000 + (team.getId() * 1234) % 900000;
            String memberStr = String.format("%.1f万", memberCount / 10000.0);
            map.put("members", memberStr);
            map.put("image", team.getLogoUrl());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", circles);
        result.put("total", teamPage.getTotal());
        result.put("current", teamPage.getCurrent());
        result.put("pages", teamPage.getPages());
        
        return R.ok(result);
    }

    /**
     * 获取趋势话题
     */
    @Operation(summary = "获取趋势话题", description = "获取社区首页的趋势话题列表")
    @GetMapping("/topics/trending")
    public R<Map<String, Object>> getTrendTopics(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "4") Integer size) {
        log.debug("获取趋势话题: page={}, size={}", page, size);
        // 分页获取热门话题
        Page<Topic> topicPage = topicService.page(
            new Page<>(page, size),
            new LambdaQueryWrapper<Topic>()
                .orderByDesc(Topic::getViewCount)
        );
        
        List<Topic> hotTopics = topicPage.getRecords();
        if (hotTopics.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", Collections.emptyList());
            result.put("total", 0);
            return R.ok(result);
        }

        // 批量获取相关帖子以避免 N+1
        List<Long> topicIds = hotTopics.stream().map(Topic::getId).collect(Collectors.toList());
        
        // 每个话题取最近10个帖子
        // 由于 MyBatis Plus LambdaQueryWrapper 不直接支持 group by limit，这里先取所有话题的最近帖子
        // 或者简单起见，如果话题不多，可以分两次批量查询
        
        // 1. 获取所有话题的最近参与者ID
        List<Post> allRecentPosts = postService.list(
            new LambdaQueryWrapper<Post>()
                .in(Post::getTopicId, topicIds)
                .orderByDesc(Post::getCreatedAt)
        );
        
        Map<Long, List<Long>> topicToUserIds = allRecentPosts.stream()
            .collect(Collectors.groupingBy(
                Post::getTopicId,
                Collectors.mapping(Post::getUserId, Collectors.toList())
            ));

        // 2. 收集所有不重复的用户ID并批量查询用户信息
        Set<Long> allUserIds = allRecentPosts.stream()
            .map(Post::getUserId)
            .collect(Collectors.toSet());
            
        Map<Long, User> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userService.listByIds(new ArrayList<>(allUserIds));
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        List<Map<String, Object>> topicList = new ArrayList<>();
        for (Topic topic : hotTopics) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", topic.getId());
            map.put("title", topic.getTitle());
            
            // 格式化统计数
            String stats;
            int postCount = topic.getPostCount() != null ? topic.getPostCount() : 0;
            if (postCount >= 10000) {
                stats = String.format("每小时%.2f万帖子", postCount / 10000.0);
            } else {
                stats = String.format("每小时%d 帖子", postCount);
            }
            map.put("stats", stats);
            
            // 获取头像
            List<Long> uids = topicToUserIds.getOrDefault(topic.getId(), Collections.emptyList())
                .stream().distinct().limit(5).collect(Collectors.toList());
                
            List<String> realAvatars = new ArrayList<>();
            for (Long uid : uids) {
                User u = userMap.get(uid);
                if (u != null) {
                    String av = u.getAvatar();
                    if (av != null && !av.isEmpty()) {
                        realAvatars.add(av);
                    } else {
                        realAvatars.add("/static/default-team.png");
                    }
                }
            }

            if (!realAvatars.isEmpty()) {
                map.put("action", "加入");
                map.put("avatars", realAvatars);
                long extra = Math.max(0, postCount - realAvatars.size());
                map.put("extraCount", extra > 0 ? extra : null);
            } else {
                map.put("action", "探索");
                map.put("tags", List.of("热门"));
                map.put("time", "刚刚活跃");
            }
            
            topicList.add(map);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", topicList);
        result.put("total", topicPage.getTotal());
        result.put("current", topicPage.getCurrent());
        result.put("pages", topicPage.getPages());
        result.put("size", topicPage.getSize());
        
        return R.ok(result);
    }
}

