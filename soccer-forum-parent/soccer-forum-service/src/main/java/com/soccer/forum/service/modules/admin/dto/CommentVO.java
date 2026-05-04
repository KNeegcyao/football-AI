package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String authorName;
    private Long authorId;
    private Long parentId;
    private String content;
    private Integer likes;
    private Integer status;
    private LocalDateTime createdAt;
}
