package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team team;
    private Long teamId = 1L;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(teamId);
        team.setName("Test FC");
        team.setLeague("Premier League");
    }

    @Test
    void createTeam_Success() {
        when(teamMapper.insert(any(Team.class))).thenReturn(1);

        Long resultId = teamService.createTeam(team);

        assertEquals(teamId, resultId);
        verify(teamMapper).insert(any(Team.class));
    }

    @Test
    void getTeamDetail_Success() {
        when(teamMapper.selectById(teamId)).thenReturn(team);

        Team result = teamService.getTeamDetail(teamId);

        assertNotNull(result);
        assertEquals("Test FC", result.getName());
    }

    @Test
    void listTeams_Success() {
        when(teamMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Team>());

        Page<Team> result = teamService.listTeams(1, 10, "Test");

        assertNotNull(result);
        verify(teamMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void updateTeam_Success() {
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);

        assertDoesNotThrow(() -> teamService.updateTeam(teamId, team));
        
        verify(teamMapper).updateById(any(Team.class));
    }

    @Test
    void updateTeam_NotFound_ThrowsException() {
        when(teamMapper.updateById(any(Team.class))).thenReturn(0);

        assertThrows(RuntimeException.class, () -> teamService.updateTeam(teamId, team));
    }

    @Test
    void deleteTeam_Success() {
        when(teamMapper.deleteById(teamId)).thenReturn(1);

        assertDoesNotThrow(() -> teamService.deleteTeam(teamId));
        
        verify(teamMapper).deleteById(teamId);
    }

    @Test
    void deleteTeam_NotFound_ThrowsException() {
        when(teamMapper.deleteById(teamId)).thenReturn(0);

        assertThrows(RuntimeException.class, () -> teamService.deleteTeam(teamId));
    }
}
