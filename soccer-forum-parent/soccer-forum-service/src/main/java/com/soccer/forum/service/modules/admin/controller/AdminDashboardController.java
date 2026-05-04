package com.soccer.forum.service.modules.admin.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.DailyTrendVO;
import com.soccer.forum.service.modules.admin.dto.DashboardStatsVO;
import com.soccer.forum.service.modules.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public R<DashboardStatsVO> getStats() {
        return R.ok(adminDashboardService.getStats());
    }

    @GetMapping("/trend")
    public R<List<DailyTrendVO>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return R.ok(adminDashboardService.getTrend(days));
    }
}
