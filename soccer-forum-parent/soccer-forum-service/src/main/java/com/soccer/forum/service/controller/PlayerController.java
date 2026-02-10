package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players")
    public Map<String, Object> create(@RequestBody Player player) {
        Long id = playerService.createPlayer(player);
        return Map.of("code", 200, "msg", "success", "data", id);
    }

    @GetMapping("/players/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Player player = playerService.getPlayerDetail(id);
        return Map.of("code", 200, "msg", "success", "data", player);
    }

    @PutMapping("/players/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Player player) {
        playerService.updatePlayer(id, player);
        return Map.of("code", 200, "msg", "success");
    }

    @DeleteMapping("/players/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return Map.of("code", 200, "msg", "success");
    }

    @GetMapping("/players")
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Long teamId) {
        Page<Player> result = playerService.listPlayers(page, size, keyword, teamId);
        return Map.of("code", 200, "msg", "success", "data", result);
    }

    @GetMapping("/teams/{teamId}/players")
    public Map<String, Object> listByTeam(@PathVariable Long teamId) {
        List<Player> players = playerService.getPlayersByTeam(teamId);
        return Map.of("code", 200, "msg", "success", "data", players);
    }
}
