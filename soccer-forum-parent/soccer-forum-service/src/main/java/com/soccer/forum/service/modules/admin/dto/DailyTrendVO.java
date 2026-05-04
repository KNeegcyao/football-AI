package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;

@Data
public class DailyTrendVO {
    private String date;
    private Long newUsers;
    private Long newPosts;
    private Long newComments;
}
