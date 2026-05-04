package com.soccer.forum.service.modules.admin.service;

import com.soccer.forum.service.modules.admin.dto.DashboardStatsVO;
import com.soccer.forum.service.modules.admin.dto.DailyTrendVO;

import java.util.List;

public interface AdminDashboardService {
    DashboardStatsVO getStats();
    List<DailyTrendVO> getTrend(int days);
}
