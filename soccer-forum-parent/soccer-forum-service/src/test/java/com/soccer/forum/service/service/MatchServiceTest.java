package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.mapper.MatchMapper;
import com.soccer.forum.service.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Match match;
    private Long matchId = 1L;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setId(matchId);
        match.setCompetitionName("Premier League");
    }

    @Test
    void createMatch_Success() {
        when(matchMapper.insert(any(Match.class))).thenReturn(1);

        Long resultId = matchService.createMatch(match);

        assertEquals(matchId, resultId);
        verify(matchMapper).insert(any(Match.class));
    }

    @Test
    void getMatchDetail_Success() {
        when(matchMapper.selectById(matchId)).thenReturn(match);

        Match result = matchService.getMatchDetail(matchId);

        assertNotNull(result);
        assertEquals("Premier League", result.getCompetitionName());
    }

    @Test
    void listMatches_Success() {
        when(matchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Match>());

        Page<Match> result = matchService.listMatches(1, 10, "Premier League", 1);

        assertNotNull(result);
        verify(matchMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getMatchesByDate_Success() {
        when(matchMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(match));

        List<Match> result = matchService.getMatchesByDate(LocalDate.now());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(matchMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void updateMatch_Success() {
        when(matchMapper.updateById(any(Match.class))).thenReturn(1);

        assertDoesNotThrow(() -> matchService.updateMatch(matchId, match));
        
        verify(matchMapper).updateById(any(Match.class));
    }

    @Test
    void updateMatch_NotFound_ThrowsException() {
        when(matchMapper.updateById(any(Match.class))).thenReturn(0);

        assertThrows(RuntimeException.class, () -> matchService.updateMatch(matchId, match));
    }

    @Test
    void deleteMatch_Success() {
        when(matchMapper.deleteById(matchId)).thenReturn(1);

        assertDoesNotThrow(() -> matchService.deleteMatch(matchId));
        
        verify(matchMapper).deleteById(matchId);
    }

    @Test
    void deleteMatch_NotFound_ThrowsException() {
        when(matchMapper.deleteById(matchId)).thenReturn(0);

        assertThrows(RuntimeException.class, () -> matchService.deleteMatch(matchId));
    }
}
