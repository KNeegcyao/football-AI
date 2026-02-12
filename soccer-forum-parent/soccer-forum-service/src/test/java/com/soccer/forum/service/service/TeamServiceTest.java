package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team testTeam;

    @BeforeEach
    void setUp() {
        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("皇家马德里");
        testTeam.setLogoUrl("logo.png");
        testTeam.setFoundedYear(1902);
        testTeam.setCreatedAt(LocalDateTime.now());
        testTeam.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateTeam() {
        when(teamMapper.insert(any(Team.class))).thenReturn(1);

        teamService.createTeam(testTeam);

        verify(teamMapper, times(1)).insert(any(Team.class));
        assertNotNull(testTeam.getCreatedAt());
        assertNotNull(testTeam.getUpdatedAt());
    }

    @Test
    void testGetTeamDetail_Success() {
        when(teamMapper.selectById(1L)).thenReturn(testTeam);

        Team result = teamService.getTeamDetail(1L);

        assertNotNull(result);
        assertEquals("皇家马德里", result.getName());
    }

    @Test
    void testGetTeamDetail_NotFound() {
        when(teamMapper.selectById(1L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> teamService.getTeamDetail(1L));
    }

    @Test
    void testListTeams() {
        Page<Team> teamPage = new Page<>(1, 10);
        List<Team> list = new ArrayList<>();
        list.add(testTeam);
        teamPage.setRecords(list);
        
        when(teamMapper.selectPage(any(Page.class), any())).thenReturn(teamPage);

        Page<Team> result = teamService.listTeams(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testUpdateTeam_Success() {
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);

        teamService.updateTeam(1L, testTeam);

        verify(teamMapper, times(1)).updateById(any(Team.class));
        assertEquals(1L, testTeam.getId());
    }

    @Test
    void testUpdateTeam_NotFound() {
        when(teamMapper.updateById(any(Team.class))).thenReturn(0);

        assertThrows(ServiceException.class, () -> teamService.updateTeam(1L, testTeam));
    }

    @Test
    void testDeleteTeam_Success() {
        when(teamMapper.deleteById(1L)).thenReturn(1);

        teamService.deleteTeam(1L);

        verify(teamMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTeam_NotFound() {
        when(teamMapper.deleteById(1L)).thenReturn(0);

        assertThrows(ServiceException.class, () -> teamService.deleteTeam(1L));
    }
}
