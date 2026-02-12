package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 赛事管理控制器
 * <p>
 * 提供赛事日程的创建、查询、更新及删除功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "赛事管理", description = "赛事日程管理接口")
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * 创建赛事接口
     */
    @Operation(summary = "创建赛事", description = "录入新的比赛日程")
    @PostMapping
    public R<Long> create(@Parameter(description = "赛事信息对象") @RequestBody Match match) {
        log.info("收到创建赛事请求: 主队ID={} vs 客队ID={}", match.getHomeTeamId(), match.getAwayTeamId());
        Long id = matchService.createMatch(match);
        log.info("赛事创建成功: id={}", id);
        return R.ok(id, "赛事创建成功");
    }

    /**
     * 获取赛事详情接口
     */
    @Operation(summary = "获取赛事详情", description = "根据ID获取比赛详细信息")
    @GetMapping("/{id}")
    public R<Match> get(@Parameter(description = "赛事ID") @PathVariable Long id) {
        log.info("收到获取赛事详情请求: id={}", id);
        Match match = matchService.getMatchDetail(id);
        return R.ok(match);
    }

    /**
     * 查询某日赛事列表接口
     */
    @Operation(summary = "查询每日赛事", description = "获取指定日期的所有比赛列表")
    @GetMapping("/date")
    public R<List<Match>> listByDate(@Parameter(description = "日期 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("收到查询日期赛事请求: date={}", date);
        List<Match> matches = matchService.getMatchesByDate(date);
        return R.ok(matches);
    }

    /**
     * 分页查询赛事列表接口
     */
    @Operation(summary = "查询赛事列表", description = "分页获取赛事日程，支持按状态筛选")
    @GetMapping("/list")
    public R<Page<Match>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                               @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                               @Parameter(description = "赛事名称 (如：英超, 西甲)") @RequestParam(required = false) String competition,
                               @Parameter(description = "比赛状态 (0:未开赛, 1:进行中, 2:已结束)") @RequestParam(required = false) Integer status) {
        log.info("收到查询赛事列表请求: page={}, size={}, competition={}, status={}", page, size, competition, status);
        Page<Match> matchPage = matchService.listMatches(page, size, competition, status);
        return R.ok(matchPage);
    }

    /**
     * 更新赛事接口
     */
    @Operation(summary = "更新赛事", description = "更新比赛信息（如比分、状态）")
    @PutMapping("/{id}")
    public R<Void> update(@Parameter(description = "赛事ID") @PathVariable Long id, 
                          @Parameter(description = "赛事更新信息对象") @RequestBody Match match) {
        log.info("收到更新赛事请求: id={}", id);
        matchService.updateMatch(id, match);
        return R.ok(null, "赛事更新成功");
    }

    /**
     * 删除赛事接口
     */
    @Operation(summary = "删除赛事", description = "根据ID删除赛事")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "赛事ID") @PathVariable Long id) {
        log.info("收到删除赛事请求: id={}", id);
        matchService.deleteMatch(id);
        return R.ok(null, "赛事删除成功");
    }
}
