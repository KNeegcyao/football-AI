package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    private Long totalTopics;
    private Long todayNewUsers;
    private Long todayNewPosts;
    private Long todayNewComments;
}
