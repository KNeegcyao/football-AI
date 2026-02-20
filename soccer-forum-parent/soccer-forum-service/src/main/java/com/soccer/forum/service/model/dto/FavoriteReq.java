package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "收藏请求")
public class FavoriteReq {
    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "新闻ID")
    private Long newsId;

    @Schema(description = "帖子ID")
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    @Schema(description = "新闻ID")
    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
}
