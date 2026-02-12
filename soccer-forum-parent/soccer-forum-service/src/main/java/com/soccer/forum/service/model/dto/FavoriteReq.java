package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "收藏请求")
public class FavoriteReq {
    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
