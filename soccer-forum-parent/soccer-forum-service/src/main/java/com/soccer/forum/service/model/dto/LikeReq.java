package com.soccer.forum.service.model.dto;

public class LikeReq {
    private Long targetId;
    private Integer targetType; // 1:Post, 2:Comment

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
}
