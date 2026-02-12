package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("notifications")
@Schema(description = "通知实体")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long id;
    @Schema(description = "接收通知的用户ID")
    private Long userId;
    @Schema(description = "触发通知的用户ID")
    private Long fromUserId;
    @Schema(description = "类型 (1:点赞帖子, 2:点赞评论, 3:评论帖子, 4:回复评论, 5:系统通知)")
    private Integer type;
    @Schema(description = "目标ID (帖子ID或评论ID)")
    private Long targetId;
    @Schema(description = "通知简述或系统消息内容")
    private String content;
    @Schema(description = "是否已读 (0:未读, 1:已读)")
    private Integer isRead;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    // Getters and Setters
    @Schema(description = "通知ID")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Schema(description = "接收通知的用户ID")
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    @Schema(description = "触发通知的用户ID")
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    @Schema(description = "类型 (1:点赞帖子, 2:点赞评论, 3:评论帖子, 4:回复评论, 5:系统通知)")
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    @Schema(description = "目标ID (帖子ID或评论ID)")
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    @Schema(description = "通知简述或系统消息内容")
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    @Schema(description = "是否已读 (0:未读, 1:已读)")
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    @Schema(description = "创建时间")
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
