package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.service.TeamService;
import com.soccer.forum.service.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public CommunityController(TeamService teamService, TopicService topicService) {
        this.teamService = teamService;
        this.topicService = topicService;
    }

    /**
     * 获取热门圈子
     */
    @Operation(summary = "获取热门圈子", description = "获取社区首页的热门圈子列表")
    @GetMapping("/circles/hot")
    public R<List<Map<String, Object>>> getHotCircles() {
        // 从数据库获取特定球队数据作为圈子
        List<String> hotTeamNames = List.of(
            "皇家马德里", "巴塞罗那", "曼城", "阿森纳", 
            "利物浦", "拜仁慕尼黑", "曼联", "尤文图斯"
        );
        List<Team> teams = teamService.getTeamsByNames(hotTeamNames);
        
        // 使用 Map 去重，Key 为球队名称
        Map<String, Team> teamMap = teams.stream()
            .collect(Collectors.toMap(Team::getName, team -> team, (existing, replacement) -> existing));

        // 按照 hotTeamNames 的顺序构建结果列表，确保顺序和去重
        List<Map<String, Object>> circles = new ArrayList<>();
        for (String name : hotTeamNames) {
            Team team = teamMap.get(name);
            if (team != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", team.getName());
                // 模拟成员数，基于ID生成一个确定的数字，例如 ID*1000 + 50000
                long memberCount = 50000 + (team.getId() * 1234) % 900000;
                String memberStr = String.format("%.1f万", memberCount / 10000.0);
                map.put("members", memberStr);
                map.put("image", team.getLogoUrl());
                circles.add(map);
            }
        }

        return R.ok(circles);
    }

    /**
     * 获取趋势话题
     */
    @Operation(summary = "获取趋势话题", description = "获取社区首页的趋势话题列表")
    @GetMapping("/topics/trending")
    public R<List<Map<String, Object>>> getTrendTopics() {
        List<Topic> hotTopics = topicService.getHotTopics(5);
        List<Map<String, Object>> result = new ArrayList<>();

        // 默认头像列表
        List<String> defaultAvatars = List.of(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuATdmAqFDBHDLFSrvkOdXszRsl-foBm99iFZSMA9k6U27dxMDGrqDj5wePvJLzco3U5kAjEOwI-fQDTlajpB7soBEt7a_4Z6opAeidctON_JZHOJjeh0tsXauMNDRva4qkpcopzq7n21O_VhPzgk9iWKQLpZ85_jn19oxUVXc-sZIv_RLuyqhh96Bu8CJYZMSb-OoIXK2P56I8ezO94Yyp45f9kTA-5CfGIz-_RQ6bHqh9NjO_aRu04vFk8nRdjoaV_oPVhj81ZaQeH",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBOdkyV91FluVNjl50vbosguFH1qM1oPIBS-OMN7hr8Vj8JdAUFctKMtJl_kp8yd107Ryg5-dSmtu6RBCtjuFgLdngUnL2W7TvkrcFEPQn6Tnu8mB6R6cdicAPwUWwzYqRmIwkIDfjZ9X0R2WbVYqoaN7hXRr7h81wUHIj1P75-GtWWQ1EYG4mdU6isbKdWxmGlB8lLpfmFaCX8bKvfYhZgjaDsppxWt8ei5e2H4eYcqGGbjEQIHWItSc3Ic6SIePIalo9FsNsViQ6R",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuB5RVAFby5yx4vJooY_hLKHj39lXJr7XaPyb2gxNTedCulZD3WGJR3Wf4gx3f5bLJadkMUN7djSieqTOC29ZpAAYyJjE7IFHAE_phuCzp52J5O3HRxe0TI4CCiOgT0iR2TDligRPLCJcSLROCyYpPkN2ZPsDk8NBMBp16Oayk1sgpgv4uCG44uy0c0llRWJt_DnK0vxAreEwoaWPwemdUVUnQ0qrNF6wUst9EyaTlrgUc9v41Bl6ZMU3Eh4z3FGTj98TOWBiDD7e7jP"
        );

        for (int i = 0; i < hotTopics.size(); i++) {
            Topic topic = hotTopics.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("title", topic.getTitle());
            
            // 格式化统计数据
            String stats;
            if (topic.getPostCount() >= 10000) {
                stats = String.format("每小时 %.2f万 帖子", topic.getPostCount() / 10000.0);
            } else {
                stats = String.format("每小时 %.1fk 帖子", topic.getPostCount() / 1000.0);
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
