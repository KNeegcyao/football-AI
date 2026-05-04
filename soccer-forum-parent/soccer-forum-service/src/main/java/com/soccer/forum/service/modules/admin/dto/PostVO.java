package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long authorId;
    private Integer views;
    private Integer likes;
    private Integer commentCount;
    private Integer status;
    private Long topicId;
    private String topicName;
    private List<String> images;
    private LocalDateTime createdAt;
}
