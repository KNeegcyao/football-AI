package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.service.PlayerService;
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

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
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
