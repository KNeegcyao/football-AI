package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v2/teams")
public class TeamController {

    private static final Logger log = LoggerFactory.getLogger(TeamController.class);

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 创建球队接口
     * <p>
     * 录入新的球队信息。
     * </p>
     *
     * @param team 球队实体对象
     * @return 包含新球队 ID 的响应结果
     */
    @Operation(summary = "创建球队", description = "录入新球队信息")
    @PostMapping
    public R<Long> create(@RequestBody Team team) {
        log.info("收到创建球队请求: 名称={}", team.getName());
        try {
            Long id = teamService.createTeam(team);
            log.info("球队创建成功: id={}, 名称={}", id, team.getName());
            return R.ok(id);
        } catch (Exception e) {
            log.error("球队创建失败: 名称={}", team.getName(), e);
            return R.fail("球队创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取球队详情接口
     * <p>
     * 根据 ID 查询球队详细信息。
     * </p>
     *
     * @param id 球队 ID
     * @return 包含球队详情的响应结果
     */
    @Operation(summary = "获取球队详情", description = "根据ID查询球队详细信息")
    @GetMapping("/{id}")
    public R<Team> get(@PathVariable Long id) {
        log.info("收到获取球队详情请求: id={}", id);
        Team team = teamService.getTeamDetail(id);
        return R.ok(team);
    }

    /**
     * 更新球队信息接口
     * <p>
     * 更新已存在的球队信息。
     * </p>
     *
     * @param id 球队 ID
     * @param team 更新后的球队实体对象
     * @return 操作结果
     */
    @Operation(summary = "更新球队信息", description = "更新已存在的球队信息")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Team team) {
        log.info("收到更新球队请求: id={}", id);
        try {
            teamService.updateTeam(id, team);
            log.info("球队更新成功: id={}", id);
            return R.ok();
        } catch (Exception e) {
            log.error("球队更新失败: id={}", id, e);
            return R.fail("球队更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除球队接口
     * <p>
     * 根据 ID 删除球队数据。
     * </p>
     *
     * @param id 球队 ID
     * @return 操作结果
     */
    @Operation(summary = "删除球队", description = "删除球队数据")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("收到删除球队请求: id={}", id);
        try {
            teamService.deleteTeam(id);
            log.info("球队删除成功: id={}", id);
            return R.ok();
        } catch (Exception e) {
            log.error("球队删除失败: id={}", id, e);
            return R.fail("球队删除失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询球队列表接口
     * <p>
     * 支持按球队名称或所属联赛关键词模糊搜索。
     * </p>
     *
     * @param page 页码，默认为 1
     * @param size 每页大小，默认为 10
     * @param keyword 搜索关键词（可选）
     * @return 包含球队分页数据的响应结果
     */
    @Operation(summary = "分页查询球队", description = "支持按名称或联赛关键词搜索")
    @GetMapping
    public R<Page<Team>> list(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String keyword) {
        log.info("收到分页查询球队请求: 页码={}, 大小={}, 关键词={}", page, size, keyword);
        Page<Team> result = teamService.listTeams(page, size, keyword);
        return R.ok(result);
    }
}
