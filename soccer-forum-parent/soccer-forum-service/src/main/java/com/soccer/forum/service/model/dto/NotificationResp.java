package com.soccer.forum.service.model.dto;

import com.soccer.forum.domain.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "通知详情响应")
public class NotificationResp {
    @Schema(description = "通知ID")
    private Long id;
    @Schema(description = "类型")
    private Integer type;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "是否已读")
    private Integer isRead;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "触发者信息")
    private UserSimpleResp fromUser;
    
    @Schema(description = "关联帖子ID")
    private Long postId;

    public static NotificationResp from(Notification notification, UserSimpleResp fromUser) {
        NotificationResp resp = new NotificationResp();
        resp.setId(notification.getId());
        resp.setType(notification.getType());
        resp.setTargetId(notification.getTargetId());
        resp.setContent(notification.getContent());
        resp.setIsRead(notification.getIsRead());
        resp.setCreatedAt(notification.getCreatedAt());
        resp.setFromUser(fromUser);
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UserSimpleResp getFromUser() { return fromUser; }
    public void setFromUser(UserSimpleResp fromUser) { this.fromUser = fromUser; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}