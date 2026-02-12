package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "点赞请求")
public class LikeReq {
    @Schema(description = "目标ID (帖子ID或评论ID)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;
    @Schema(description = "目标类型 (1:帖子, 2:评论)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer targetType; // 1:Post, 2:Comment

    @Schema(description = "目标ID (帖子ID或评论ID)", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    @Schema(description = "目标类型 (1:帖子, 2:评论)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
}
