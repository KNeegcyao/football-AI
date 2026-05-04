package com.soccer.forum.service.modules.admin.service.impl;

import com.soccer.forum.service.modules.admin.dto.DashboardStatsVO;
import com.soccer.forum.service.modules.admin.dto.DailyTrendVO;
import com.soccer.forum.service.modules.admin.mapper.AdminDashboardMapper;
import com.soccer.forum.service.modules.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setTotalUsers(adminDashboardMapper.countActiveUsers());
        stats.setTotalPosts(adminDashboardMapper.countActivePosts());
        stats.setTotalComments(adminDashboardMapper.countComments());
        stats.setTotalTopics(adminDashboardMapper.countTopics());
        stats.setTodayNewUsers(adminDashboardMapper.countTodayNewUsers());
        stats.setTodayNewPosts(adminDashboardMapper.countTodayNewPosts());
        stats.setTodayNewComments(adminDashboardMapper.countTodayNewComments());
        return stats;
    }

    @Override
    public List<DailyTrendVO> getTrend(int days) {
        return adminDashboardMapper.getDailyTrend(days);
    }
}
