package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/api/v2/matches")
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * 创建赛事接口
     * <p>
     * 录入新的比赛日程信息。
     * </p>
     *
     * @param match 赛事实体对象
     * @return 包含新赛事 ID 的响应结果
     */
    @Operation(summary = "创建赛事", description = "录入新的比赛日程")
    @PostMapping
    public Map<String, Object> create(@RequestBody Match match) {
        log.info("收到创建赛事请求: 主队ID={} vs 客队ID={}", match.getHomeTeamId(), match.getAwayTeamId());
        try {
            Long id = matchService.createMatch(match);
            log.info("赛事创建成功: id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", id);
            return result;
        } catch (Exception e) {
            log.error("赛事创建失败", e);
            throw e;
        }
    }

    /**
     * 获取赛事详情接口
     * <p>
     * 根据 ID 获取比赛详细信息。
     * </p>
     *
     * @param id 赛事 ID
     * @return 包含赛事详情的响应结果
     */
    @Operation(summary = "获取赛事详情", description = "根据ID获取比赛详细信息")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        log.info("收到获取赛事详情请求: id={}", id);
        Match match = matchService.getMatchDetail(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", match);
        return result;
    }

    /**
     * 更新赛事接口
     * <p>
     * 更新比赛信息（如比分、状态、延期等）。
     * </p>
     *
     * @param id 赛事 ID
     * @param match 更新后的赛事实体对象
     * @return 操作结果
     */
    @Operation(summary = "更新赛事", description = "更新比赛信息（如比分、状态）")
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Match match) {
        log.info("收到更新赛事请求: id={}", id);
        try {
            matchService.updateMatch(id, match);
            log.info("赛事更新成功: id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            return result;
        } catch (Exception e) {
            log.error("赛事更新失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 删除赛事接口
     * <p>
     * 根据 ID 删除指定的比赛日程。
     * </p>
     *
     * @param id 赛事 ID
     * @return 操作结果
     */
    @Operation(summary = "删除赛事", description = "删除指定的比赛日程")
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("收到删除赛事请求: id={}", id);
        try {
            matchService.deleteMatch(id);
            log.info("赛事删除成功: id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            return result;
        } catch (Exception e) {
            log.error("赛事删除失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 分页查询赛事列表接口
     * <p>
     * 支持按赛事名称、比赛状态进行筛选。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param competition 赛事名称（可选）
     * @param status 比赛状态（可选）
     * @return 包含赛事分页数据的响应结果
     */
    @Operation(summary = "分页查询赛事", description = "支持按赛事名称、状态筛选")
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String competition,
                                    @RequestParam(required = false) Integer status) {
        log.info("收到分页查询赛事请求: 页码={}, 大小={}, 赛事={}, 状态={}", page, size, competition, status);
        Page<Match> result = matchService.listMatches(page, size, competition, status);
        
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", result);
        return map;
    }

    /**
     * 按日期查询赛事接口
     * <p>
     * 获取指定日期的所有比赛列表。
     * </p>
     *
     * @param date 查询日期（yyyy-MM-dd）
     * @return 包含当日赛事列表的响应结果
     */
    @Operation(summary = "按日期查询赛事", description = "获取指定日期的所有比赛")
    @GetMapping("/by-date")
    public Map<String, Object> listByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("收到按日期查询赛事请求: 日期={}", date);
        List<Match> matches = matchService.getMatchesByDate(date);
        
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", matches);
        return map;
    }
}
