package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.mapper.PlayerMapper;
import com.soccer.forum.service.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerMapper playerMapper;

    public PlayerServiceImpl(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    @Override
    public Long createPlayer(Player player) {
        player.setCreatedAt(LocalDateTime.now());
        player.setUpdatedAt(LocalDateTime.now());
        playerMapper.insert(player);
        return player.getId();
    }

    @Override
    public Player getPlayerDetail(Long id) {
        return playerMapper.selectById(id);
    }

    @Override
    public Page<Player> listPlayers(Integer page, Integer size, String keyword, Long teamId) {
        Page<Player> playerPage = new Page<>(page, size);
        LambdaQueryWrapper<Player> query = new LambdaQueryWrapper<>();
        
        if (teamId != null) {
            query.eq(Player::getCurrentTeamId, teamId);
        }
        
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(Player::getName, keyword)
                          .or()
                          .like(Player::getNationality, keyword)
                          .or()
                          .like(Player::getPosition, keyword));
        }
        return playerMapper.selectPage(playerPage, query);
    }

    @Override
    public List<Player> getPlayersByTeam(Long teamId) {
        return playerMapper.selectList(new LambdaQueryWrapper<Player>()
                .eq(Player::getCurrentTeamId, teamId));
    }

    @Override
    public void updatePlayer(Long id, Player player) {
        player.setId(id);
        player.setUpdatedAt(LocalDateTime.now());
        playerMapper.updateById(player);
    }

    @Override
    public void deletePlayer(Long id) {
        playerMapper.deleteById(id);
    }
}
