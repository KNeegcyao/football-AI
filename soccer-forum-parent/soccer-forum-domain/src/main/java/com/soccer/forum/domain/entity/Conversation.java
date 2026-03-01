package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("conversations")
@Schema(description = "私信会话实体")
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long id;
    @Schema(description = "用户A (通常是小ID)")
    private Long userA;
    @Schema(description = "用户B (通常是大ID)")
    private Long userB;
    @Schema(description = "最后一条消息内容")
    private String lastMessage;
    @Schema(description = "最后发送者ID")
    private Long lastSenderId;
    @Schema(description = "用户A的未读数")
    private Integer unreadCountA;
    @Schema(description = "用户B的未读数")
    private Integer unreadCountB;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserA() { return userA; }
    public void setUserA(Long userA) { this.userA = userA; }
    public Long getUserB() { return userB; }
    public void setUserB(Long userB) { this.userB = userB; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Long getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(Long lastSenderId) { this.lastSenderId = lastSenderId; }
    public Integer getUnreadCountA() { return unreadCountA; }
    public void setUnreadCountA(Integer unreadCountA) { this.unreadCountA = unreadCountA; }
    public Integer getUnreadCountB() { return unreadCountB; }
    public void setUnreadCountB(Integer unreadCountB) { this.unreadCountB = unreadCountB; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
