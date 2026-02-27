package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "收藏切换响应")
public class FavoriteToggleResp {
    @Schema(description = "是否已收藏")
    private boolean favorited;
    
    @Schema(description = "最新收藏数")
    private Integer latestCount;

    public FavoriteToggleResp() {}

    public FavoriteToggleResp(boolean favorited, Integer latestCount) {
        this.favorited = favorited;
        this.latestCount = latestCount;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public Integer getLatestCount() {
        return latestCount;
    }

    public void setLatestCount(Integer latestCount) {
        this.latestCount = latestCount;
    }
}
