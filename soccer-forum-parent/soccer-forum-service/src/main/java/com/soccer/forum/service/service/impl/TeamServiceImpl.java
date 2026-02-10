package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamMapper teamMapper;

    public TeamServiceImpl(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @Override
    public Long createTeam(Team team) {
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        teamMapper.insert(team);
        return team.getId();
    }

    @Override
    public Team getTeamDetail(Long id) {
        return teamMapper.selectById(id);
    }

    @Override
    public Page<Team> listTeams(Integer page, Integer size, String keyword) {
        Page<Team> teamPage = new Page<>(page, size);
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            query.like(Team::getName, keyword)
                 .or()
                 .like(Team::getLeague, keyword);
        }
        return teamMapper.selectPage(teamPage, query);
    }

    @Override
    public void updateTeam(Long id, Team team) {
        team.setId(id);
        team.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(team);
    }

    @Override
    public void deleteTeam(Long id) {
        teamMapper.deleteById(id);
    }
}
