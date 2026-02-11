package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.service.MatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Match match) {
        Long id = matchService.createMatch(match);
        return Map.of("code", 200, "msg", "success", "data", id);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Match match = matchService.getMatchDetail(id);
        return Map.of("code", 200, "msg", "success", "data", match);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Match match) {
        matchService.updateMatch(id, match);
        return Map.of("code", 200, "msg", "success");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return Map.of("code", 200, "msg", "success");
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String competition,
                                    @RequestParam(required = false) Integer status) {
        Page<Match> result = matchService.listMatches(page, size, competition, status);
        return Map.of("code", 200, "msg", "success", "data", result);
    }

    @GetMapping("/by-date")
    public Map<String, Object> listByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Match> matches = matchService.getMatchesByDate(date);
        return Map.of("code", 200, "msg", "success", "data", matches);
    }
}
