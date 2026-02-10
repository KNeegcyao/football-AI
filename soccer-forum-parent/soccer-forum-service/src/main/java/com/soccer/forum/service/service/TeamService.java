package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;

public interface TeamService {
    Long createTeam(Team team);
    Team getTeamDetail(Long id);
    Page<Team> listTeams(Integer page, Integer size, String keyword);
    void updateTeam(Long id, Team team);
    void deleteTeam(Long id);
}
