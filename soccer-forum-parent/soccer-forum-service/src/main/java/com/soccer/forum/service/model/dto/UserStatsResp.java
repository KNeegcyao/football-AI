package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户统计信息响应")
public class UserStatsResp {

    @Schema(description = "发布帖子数")
    private Long postCount;

    @Schema(description = "收获点赞数")
    private Long likeReceivedCount;

    @Schema(description = "收藏帖子数")
    private Long favoriteCount;

    public Long getPostCount() { return postCount; }
    public void setPostCount(Long postCount) { this.postCount = postCount; }

    public Long getLikeReceivedCount() { return likeReceivedCount; }
    public void setLikeReceivedCount(Long likeReceivedCount) { this.likeReceivedCount = likeReceivedCount; }

    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }
}
