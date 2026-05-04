package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TopicVO {
    private Long id;
    private String title;
    private String description;
    private Integer viewCount;
    private Integer postCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
