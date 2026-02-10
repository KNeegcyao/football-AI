package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Team team) {
        Long id = teamService.createTeam(team);
        return Map.of("code", 200, "msg", "success", "data", id);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Team team = teamService.getTeamDetail(id);
        return Map.of("code", 200, "msg", "success", "data", team);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Team team) {
        teamService.updateTeam(id, team);
        return Map.of("code", 200, "msg", "success");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return Map.of("code", 200, "msg", "success");
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String keyword) {
        Page<Team> result = teamService.listTeams(page, size, keyword);
        return Map.of("code", 200, "msg", "success", "data", result);
    }
}
