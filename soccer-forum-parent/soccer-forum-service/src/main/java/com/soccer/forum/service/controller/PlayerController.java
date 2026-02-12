package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v2")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * 创建球员接口
     * <p>
     * 录入新的球员信息。
     * </p>
     *
     * @param player 球员实体对象
     * @return 包含新球员 ID 的响应结果
     */
    @Operation(summary = "创建球员", description = "录入新球员信息")
    @PostMapping("/players")
    public R<Long> create(@RequestBody Player player) {
        log.info("收到创建球员请求: 姓名={}", player.getName());
        try {
            Long id = playerService.createPlayer(player);
            log.info("球员创建成功: id={}, 姓名={}", id, player.getName());
            return R.ok(id);
        } catch (Exception e) {
            log.error("球员创建失败: 姓名={}", player.getName(), e);
            return R.fail("球员创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取球员详情接口
     * <p>
     * 根据 ID 查询球员详细信息。
     * </p>
     *
     * @param id 球员 ID
     * @return 包含球员详情的响应结果
     */
    @Operation(summary = "获取球员详情", description = "根据ID查询球员详细信息")
    @GetMapping("/players/{id}")
    public R<Player> get(@PathVariable Long id) {
        log.info("收到获取球员详情请求: id={}", id);
        Player player = playerService.getPlayerDetail(id);
        return R.ok(player);
    }

    /**
     * 更新球员信息接口
     * <p>
     * 更新已存在的球员信息。
     * </p>
     *
     * @param id 球员 ID
     * @param player 更新后的球员实体对象
     * @return 操作结果
     */
    @Operation(summary = "更新球员信息", description = "更新已存在的球员信息")
    @PutMapping("/players/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Player player) {
        log.info("收到更新球员请求: id={}", id);
        try {
            playerService.updatePlayer(id, player);
            log.info("球员更新成功: id={}", id);
            return R.ok();
        } catch (Exception e) {
            log.error("球员更新失败: id={}", id, e);
            return R.fail("球员更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除球员接口
     * <p>
     * 根据 ID 删除球员数据。
     * </p>
     *
     * @param id 球员 ID
     * @return 操作结果
     */
    @Operation(summary = "删除球员", description = "删除球员数据")
    @DeleteMapping("/players/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("收到删除球员请求: id={}", id);
        try {
            playerService.deletePlayer(id);
            log.info("球员删除成功: id={}", id);
            return R.ok();
        } catch (Exception e) {
            log.error("球员删除失败: id={}", id, e);
            return R.fail("球员删除失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询球员列表接口
     * <p>
     * 支持按名称、国籍、位置等关键词搜索，或按球队筛选。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（名称/国籍/位置）
     * @param teamId 球队 ID（可选）
     * @return 包含球员分页数据的响应结果
     */
    @Operation(summary = "分页查询球员", description = "支持按名称、国籍、位置等关键词搜索，或按球队筛选")
    @GetMapping("/players")
    public R<Page<Player>> list(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Long teamId) {
        log.info("收到分页查询球员请求: 页码={}, 大小={}, 关键词={}, 球队ID={}", page, size, keyword, teamId);
        Page<Player> result = playerService.listPlayers(page, size, keyword, teamId);
        return R.ok(result);
    }

    /**
     * 查询球队所有球员接口
     * <p>
     * 获取指定球队的所有球员列表（不分页）。
     * </p>
     *
     * @param teamId 球队 ID
     * @return 包含球员列表的响应结果
     */
    @Operation(summary = "查询球队所有球员", description = "获取指定球队的球员列表")
    @GetMapping("/teams/{teamId}/players")
    public R<List<Player>> listByTeam(@PathVariable Long teamId) {
        log.info("收到查询球队球员请求: 球队ID={}", teamId);
        List<Player> players = playerService.getPlayersByTeam(teamId);
        return R.ok(players);
    }
}
