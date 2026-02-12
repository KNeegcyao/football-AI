package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "收藏请求")
public class FavoriteReq {
    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
