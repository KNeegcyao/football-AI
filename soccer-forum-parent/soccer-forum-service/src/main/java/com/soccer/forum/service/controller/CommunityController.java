package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.model.dto.PostDetailResp;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.PostService;
import com.soccer.forum.service.service.TeamService;
import com.soccer.forum.service.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区相关接口控制器
 * <p>
 * 提供社区热门圈子、趋势话题等数据。
 * </p>
 */
@Tag(name = "社区管理", description = "社区发现页接口")
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final TeamService teamService;
    private final TopicService topicService;
    private final PostService postService;

    public CommunityController(TeamService teamService, TopicService topicService, PostService postService) {
        this.teamService = teamService;
        this.topicService = topicService;
        this.postService = postService;
    }

    /**
     * 获取圈子帖子列表
     */
    @Operation(summary = "获取圈子帖子列表", description = "获取指定圈子（球队）相关的帖子列表")
    @GetMapping("/circles/{name}/posts")
    public R<Page<PostDetailResp>> getCirclePosts(@Parameter(description = "圈子名称") @PathVariable String name, 
                                       @Validated PostPageReq req,
                                       @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        // 尝试根据名称查找圈子ID，优先使用精确的 circleId 查询
        List<Team> teams = teamService.getTeamsByNames(List.of(name));
        
        // 如果未找到且名称以"圈"结尾，尝试去除后缀再次查找
        if (teams.isEmpty() && name.endsWith("圈")) {
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
    @Operation(summary = "获取话题详情", description = "获取话题的详细信息")
    @GetMapping("/topics/{id}")
    public R<Topic> getTopicDetail(@Parameter(description = "话题ID") @PathVariable Long id) {
        Topic topic = topicService.getById(id);
        if (topic != null) {
            if (topic.getDescription() == null || topic.getDescription().trim().isEmpty()) {
                String title = topic.getTitle().replace("#", "");
                topic.setDescription("关于“" + title + "”的最新讨论，点击参与互动！");
            }
        }
        return R.ok(topic);
    }

    /**
     * 获取话题帖子列表
     */
    @Operation(summary = "获取话题帖子列表", description = "获取指定话题相关的帖子列表")
    @GetMapping("/topics/posts")
    public R<Page<PostDetailResp>> getTopicPosts(@Parameter(description = "话题标题") @RequestParam String title, 
                                      @Validated PostPageReq req,
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        // 尝试根据标题查找话题ID，优先使用精确的 topicId 查询
        Topic topic = topicService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                .eq(Topic::getTitle, title));
        
        // 如果没找到，尝试加上 # 前缀查找
        if (topic == null && !title.startsWith("#")) {
            topic = topicService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                    .eq(Topic::getTitle, "#" + title));
        }
        
        // 如果还没找到，尝试加上 # 前缀和后缀查找
        if (topic == null && !title.startsWith("#")) {
            topic = topicService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                    .eq(Topic::getTitle, "#" + title + "#"));
        }
        
        if (topic != null) {
            req.setTopicId(topic.getId());
        } else {
            req.setKeyword(title);
        }
        Long userId = (loginUser != null && loginUser.getUser() != null) ? loginUser.getUser().getId() : null;
        return R.ok(postService.getPostPage(req, userId));
    }

    /**
     * 获取热门圈子
     */
    @Operation(summary = "获取热门圈子", description = "获取社区首页的热门圈子列表")
    @GetMapping("/circles/hot")
    public R<List<Map<String, Object>>> getHotCircles() {
        // 获取所有标记为推荐的球队（Top 5）
        List<Team> teams = teamService.listRecommendTeams();
        
        List<Map<String, Object>> circles = teams.stream().map(team -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            // 模拟成员数，基于ID生成一个确定的数字，例如 ID*1000 + 50000
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
    public R<List<Map<String, Object>>> getTrendTopics() {
        // 获取前10个热门话题
        List<Topic> hotTopics = topicService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                .orderByDesc(Topic::getViewCount)
                .last("LIMIT 10")
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        // 默认头像列表
        List<String> defaultAvatars = List.of(
            "/static/soccer-logo.png",
            "/static/default-team.png",
            "/static/teams/generic_stadium.jpg"
        );

        for (int i = 0; i < hotTopics.size(); i++) {
            Topic topic = hotTopics.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("id", topic.getId());
            map.put("title", topic.getTitle());
            
            // 格式化统计数据
            String stats;
            if (topic.getPostCount() >= 10000) {
                stats = String.format("每小时 %.2f万 帖子", topic.getPostCount() / 10000.0);
            } else {
                stats = String.format("每小时 %d 帖子", topic.getPostCount());
            }
            map.put("stats", stats);
            
            // 模拟不同的展示样式
            if (i % 2 == 0) {
                map.put("action", "加入");
                map.put("avatars", defaultAvatars);
                map.put("extraCount", 50 + (topic.getViewCount() % 100));
            } else {
                map.put("action", "探索");
                map.put("tags", List.of("热门"));
                map.put("time", "刚刚活跃");
            }
            result.add(map);
        }
        
        return R.ok(result);
    }
}
