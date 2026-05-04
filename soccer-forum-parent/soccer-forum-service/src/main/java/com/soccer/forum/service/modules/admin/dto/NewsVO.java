package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private String author;
    private String tags;
    private String category;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
