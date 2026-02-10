package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;

import java.util.List;

public interface PlayerService {
    Long createPlayer(Player player);
    Player getPlayerDetail(Long id);
    Page<Player> listPlayers(Integer page, Integer size, String keyword, Long teamId);
    List<Player> getPlayersByTeam(Long teamId);
    void updatePlayer(Long id, Player player);
    void deletePlayer(Long id);
}
