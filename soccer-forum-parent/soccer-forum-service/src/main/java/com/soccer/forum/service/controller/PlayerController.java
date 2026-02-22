package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.service.PlayerService;
import com.soccer.forum.service.service.PlayerSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 球员管理控制器
 * <p>
 * 提供球员信息的增删改查及按球队查询的接口。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "球员管理", description = "球员信息管理接口")
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;
    private final PlayerSyncService playerSyncService;

    public PlayerController(PlayerService playerService, PlayerSyncService playerSyncService) {
        this.playerService = playerService;
        this.playerSyncService = playerSyncService;
    }

    /**
     * 同步球队球员数据接口
     */
    @Operation(summary = "同步所有豪门球队 (Football-Data.org)", description = "从 Football-Data.org 同步 15 支豪门球队的数据")
    @PostMapping("/sync/football-data")
    public R<String> syncAllFootballDataTeams() {
        log.info("收到 Football-Data.org 全量同步请求");
        try {
            String result = playerSyncService.syncAllTeamsFromFootballData();
            return R.ok(result);
        } catch (Exception e) {
            log.error("同步请求处理失败", e);
            return R.fail("同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步联赛射手榜数据
     */
    @Operation(summary = "同步联赛射手榜", description = "从 Football-Data.org 同步主要联赛的射手榜数据")
    @PostMapping("/sync-scorers")
    public R<String> syncLeagueScorers() {
        log.info("收到射手榜同步请求");
        String result = playerSyncService.syncLeagueScorers();
        return R.ok(result);
    }

    /**
     * 同步球队球员数据接口
     */
    @Operation(summary = "同步球队球员", description = "从第三方API同步球队的球员数据")
    @PostMapping("/sync/{teamId}")
    public R<String> syncTeamPlayers(@Parameter(description = "本地球队ID") @PathVariable Long teamId,
                                   @Parameter(description = "API球队ID") @RequestParam Integer apiTeamId,
                                   @Parameter(description = "赛季年份") @RequestParam(defaultValue = "2025") Integer season) {
        log.info("收到同步球队球员请求: teamId={}, apiTeamId={}, season={}", teamId, apiTeamId, season);
        String result = playerSyncService.syncTeamPlayers(teamId, apiTeamId, season);
        return R.ok(result);
    }

    /**
     * 测试 SportAPI 接口 (获取原始JSON)
     */
    @Operation(summary = "测试 SportAPI (JSON)", description = "获取 SportAPI 的原始球员数据")
    @GetMapping("/sync-sportapi/{playerId}")
    public R<com.fasterxml.jackson.databind.JsonNode> testSportApi(@Parameter(description = "API球员ID") @PathVariable Integer playerId) {
        log.info("收到 SportAPI 测试请求: playerId={}", playerId);
        return R.ok(playerSyncService.getPlayerFromSportApi(playerId));
    }

    /**
     * 执行 SportAPI 球员同步
     */
    @Operation(summary = "同步 SportAPI 球员", description = "从 SportAPI 同步单个球员数据到数据库")
    @PostMapping("/sync-sportapi/{playerId}")
    public R<String> syncSportApiPlayer(@Parameter(description = "API球员ID") @PathVariable Integer playerId) {
        log.info("收到 SportAPI 同步请求: playerId={}", playerId);
        return R.ok(playerSyncService.syncPlayerFromSportApi(playerId));
    }

    /**
     * 获取 SportAPI 球队球员列表 (实时)
     */
    @Operation(summary = "获取 SportAPI 球队球员", description = "从 SportAPI 获取指定球队的球员列表")
    @GetMapping("/team/{teamId}/sportapi")
    public R<com.fasterxml.jackson.databind.JsonNode> getTeamPlayersFromSportApi(@Parameter(description = "本地球队ID") @PathVariable Long teamId) {
        log.info("收到 SportAPI 球队球员请求: teamId={}", teamId);
        return R.ok(playerSyncService.getTeamPlayersFromSportApi(teamId));
    }

    /**
     * 获取 SportAPI 球员详情 (实时)
     */
    @Operation(summary = "获取 SportAPI 球员详情", description = "从 SportAPI 获取指定球员的详细信息")
    @GetMapping("/sportapi/{id}")
    public R<com.fasterxml.jackson.databind.JsonNode> getSportApiPlayerDetail(@Parameter(description = "SportAPI球员ID") @PathVariable Long id) {
        log.info("收到 SportAPI 球员详情请求: id={}", id);
        // 优先从本地查询
        com.fasterxml.jackson.databind.JsonNode localData = playerSyncService.getPlayerDetailJson(id);
        if (localData != null) {
            return R.ok(localData);
        }
        return R.ok(playerSyncService.getPlayerFromSportApi(id.intValue()));
    }

    /**
     * 创建球员接口
     */
    @Operation(summary = "创建球员", description = "录入新球员信息")
    @PostMapping
    public R<Long> create(@Parameter(description = "球员信息对象") @RequestBody Player player) {
        log.info("收到创建球员请求: 姓名={}", player.getName());
        Long id = playerService.createPlayer(player);
        log.info("球员创建成功: id={}, 姓名={}", id, player.getName());
        return R.ok(id, "球员创建成功");
    }

    /**
     * 获取球员详情接口
     */
    @Operation(summary = "获取球员详情", description = "根据ID查询球员详细信息")
    @GetMapping("/{id}")
    public R<Player> get(@Parameter(description = "球员ID") @PathVariable Long id) {
        log.info("收到获取球员详情请求: id={}", id);
        Player player = playerService.getPlayerDetail(id);
        return R.ok(player);
    }

    /**
     * 查询某球队球员列表接口
     */
    @Operation(summary = "查询球队球员", description = "获取指定球队的所有球员列表")
    @GetMapping("/team/{teamId}")
    public R<List<Player>> listByTeam(@Parameter(description = "球队ID") @PathVariable Long teamId) {
        log.info("收到查询球队球员请求: teamId={}", teamId);
        List<Player> players = playerService.getPlayersByTeam(teamId);
        return R.ok(players);
    }

    /**
     * 分页查询球员列表接口
     */
    @Operation(summary = "查询球员列表", description = "分页查询球员信息，支持姓名搜索")
    @GetMapping("/list")
    public R<Page<Player>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                                @Parameter(description = "搜索关键词 (球员姓名)") @RequestParam(required = false) String keyword,
                                @Parameter(description = "球队ID (可选)") @RequestParam(required = false) Long teamId) {
        log.info("收到查询球员列表请求: page={}, size={}, keyword={}, teamId={}", page, size, keyword, teamId);
        Page<Player> playerPage = playerService.listPlayers(page, size, keyword, teamId);
        return R.ok(playerPage);
    }

    /**
     * 更新球员信息接口
     */
    @Operation(summary = "更新球员信息", description = "更新已存在的球员信息")
    @PutMapping("/{id}")
    public R<Void> update(@Parameter(description = "球员ID") @PathVariable Long id, 
                          @Parameter(description = "球员更新信息对象") @RequestBody Player player) {
        log.info("收到更新球员请求: id={}", id);
        playerService.updatePlayer(id, player);
        return R.ok(null, "球员信息更新成功");
    }

    /**
     * 删除球员接口
     */
    @Operation(summary = "删除球员", description = "根据ID删除球员")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "球员ID") @PathVariable Long id) {
        log.info("收到删除球员请求: id={}", id);
        playerService.deletePlayer(id);
        return R.ok(null, "球员删除成功");
    }
}
