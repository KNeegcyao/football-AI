package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 球队管理控制器
 * <p>
 * 提供球队信息的增删改查接口。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "球队管理", description = "球队信息管理接口")
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private static final Logger log = LoggerFactory.getLogger(TeamController.class);

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 创建球队接口
     */
    @Operation(summary = "创建球队", description = "录入新球队信息")
    @PostMapping
    public R<Long> create(@Parameter(description = "球队信息对象") @RequestBody Team team) {
        log.info("收到创建球队请求: 名称={}", team.getName());
        Long id = teamService.createTeam(team);
        log.info("球队创建成功: id={}, 名称={}", id, team.getName());
        return R.ok(id, "球队创建成功");
    }

    /**
     * 获取球队详情接口
     */
    @Operation(summary = "获取球队详情", description = "根据ID查询球队详细信息")
    @GetMapping("/{id}")
    public R<Team> get(@Parameter(description = "球队ID") @PathVariable Long id) {
        log.info("收到获取球队详情请求: id={}", id);
        Team team = teamService.getTeamDetail(id);
        return R.ok(team);
    }

    /**
     * 分页查询球队列表接口
     */
    @Operation(summary = "查询球队列表", description = "分页查询球队信息，支持按联赛筛选")
    @GetMapping("/list")
    public R<Page<Team>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                              @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                              @Parameter(description = "联赛名称 (如：英超, 西甲, 中超)") @RequestParam(required = false) String league) {
        log.info("收到查询球队列表请求: page={}, size={}, league={}", page, size, league);
        Page<Team> teamPage = teamService.listTeams(page, size, league);
        return R.ok(teamPage);
    }

    /**
     * 更新球队信息接口
     */
    @Operation(summary = "更新球队信息", description = "更新已存在的球队信息")
    @PutMapping("/{id}")
    public R<Void> update(@Parameter(description = "球队ID") @PathVariable Long id, 
                          @Parameter(description = "球队更新信息对象") @RequestBody Team team) {
        log.info("收到更新球队请求: id={}", id);
        teamService.updateTeam(id, team);
        return R.ok(null, "球队信息更新成功");
    }

    /**
     * 删除球队接口
     */
    @Operation(summary = "删除球队", description = "根据ID删除球队")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "球队ID") @PathVariable Long id) {
        log.info("收到删除球队请求: id={}", id);
        teamService.deleteTeam(id);
        return R.ok(null, "球队删除成功");
    }
}
