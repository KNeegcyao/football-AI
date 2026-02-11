package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;

import java.time.LocalDate;
import java.util.List;

public interface MatchService {
    Long createMatch(Match match);
    Match getMatchDetail(Long id);
    Page<Match> listMatches(Integer page, Integer size, String competition, Integer status);
    List<Match> getMatchesByDate(LocalDate date);
    void updateMatch(Long id, Match match);
    void deleteMatch(Long id);
}
