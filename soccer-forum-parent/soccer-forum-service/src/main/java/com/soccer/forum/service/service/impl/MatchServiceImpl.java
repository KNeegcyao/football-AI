package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.mapper.MatchMapper;
import com.soccer.forum.service.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchMapper matchMapper;

    public MatchServiceImpl(MatchMapper matchMapper) {
        this.matchMapper = matchMapper;
    }

    @Override
    public Long createMatch(Match match) {
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        matchMapper.insert(match);
        return match.getId();
    }

    @Override
    public Match getMatchDetail(Long id) {
        return matchMapper.selectById(id);
    }

    @Override
    public Page<Match> listMatches(Integer page, Integer size, String competition, Integer status) {
        Page<Match> matchPage = new Page<>(page, size);
        LambdaQueryWrapper<Match> query = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(competition)) {
            query.eq(Match::getCompetitionName, competition);
        }
        
        if (status != null) {
            query.eq(Match::getStatus, status);
        }
        
        query.orderByAsc(Match::getMatchTime);
        return matchMapper.selectPage(matchPage, query);
    }

    @Override
    public List<Match> getMatchesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        return matchMapper.selectList(new LambdaQueryWrapper<Match>()
                .between(Match::getMatchTime, startOfDay, endOfDay)
                .orderByAsc(Match::getMatchTime));
    }

    @Override
    public void updateMatch(Long id, Match match) {
        match.setId(id);
        match.setUpdatedAt(LocalDateTime.now());
        matchMapper.updateById(match);
    }

    @Override
    public void deleteMatch(Long id) {
        matchMapper.deleteById(id);
    }
}
