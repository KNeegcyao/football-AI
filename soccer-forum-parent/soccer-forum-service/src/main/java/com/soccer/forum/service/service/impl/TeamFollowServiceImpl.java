package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.TeamFollow;
import com.soccer.forum.service.mapper.TeamFollowMapper;
import com.soccer.forum.service.service.TeamFollowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TeamFollowServiceImpl extends ServiceImpl<TeamFollowMapper, TeamFollow> implements TeamFollowService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long userId, Long teamId) {
        if (isFollowing(userId, teamId)) {
            return false;
        }
        
        TeamFollow follow = new TeamFollow();
        follow.setUserId(userId);
        follow.setTeamId(teamId);
        follow.setCreatedAt(LocalDateTime.now());
        
        return this.save(follow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long teamId) {
        LambdaQueryWrapper<TeamFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFollow::getUserId, userId)
               .eq(TeamFollow::getTeamId, teamId);
        this.remove(wrapper);
    }

    @Override
    public boolean isFollowing(Long userId, Long teamId) {
        LambdaQueryWrapper<TeamFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFollow::getUserId, userId)
               .eq(TeamFollow::getTeamId, teamId);
        return this.count(wrapper) > 0;
    }

    @Override
    public java.util.List<Long> getFollowedTeamIds(Long userId) {
        LambdaQueryWrapper<TeamFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFollow::getUserId, userId);
        return this.list(wrapper).stream().map(TeamFollow::getTeamId).collect(java.util.stream.Collectors.toList());
    }
}
