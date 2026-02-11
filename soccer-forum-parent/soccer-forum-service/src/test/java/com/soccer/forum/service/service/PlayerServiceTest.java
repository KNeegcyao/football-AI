package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.mapper.PlayerMapper;
import com.soccer.forum.service.service.impl.PlayerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private Player player;
    private Long playerId = 1L;
    private Long teamId = 1L;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(playerId);
        player.setName("Test Player");
        player.setCurrentTeamId(teamId);
    }

    @Test
    void createPlayer_Success() {
        when(playerMapper.insert(any(Player.class))).thenReturn(1);

        Long resultId = playerService.createPlayer(player);

        assertEquals(playerId, resultId);
        verify(playerMapper).insert(any(Player.class));
    }

    @Test
    void getPlayerDetail_Success() {
        when(playerMapper.selectById(playerId)).thenReturn(player);

        Player result = playerService.getPlayerDetail(playerId);

        assertNotNull(result);
        assertEquals("Test Player", result.getName());
    }

    @Test
    void listPlayers_Success() {
        when(playerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Player>());

        Page<Player> result = playerService.listPlayers(1, 10, "Test", teamId);

        assertNotNull(result);
        verify(playerMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getPlayersByTeam_Success() {
        when(playerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(player));

        List<Player> result = playerService.getPlayersByTeam(teamId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(playerMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void updatePlayer_Success() {
        when(playerMapper.updateById(any(Player.class))).thenReturn(1);

        assertDoesNotThrow(() -> playerService.updatePlayer(playerId, player));
        
        verify(playerMapper).updateById(any(Player.class));
    }

    @Test
    void updatePlayer_NotFound_ThrowsException() {
        when(playerMapper.updateById(any(Player.class))).thenReturn(0);

        assertThrows(RuntimeException.class, () -> playerService.updatePlayer(playerId, player));
    }

    @Test
    void deletePlayer_Success() {
        when(playerMapper.deleteById(playerId)).thenReturn(1);

        assertDoesNotThrow(() -> playerService.deletePlayer(playerId));
        
        verify(playerMapper).deleteById(playerId);
    }

    @Test
    void deletePlayer_NotFound_ThrowsException() {
        when(playerMapper.deleteById(playerId)).thenReturn(0);

        assertThrows(RuntimeException.class, () -> playerService.deletePlayer(playerId));
    }
}
