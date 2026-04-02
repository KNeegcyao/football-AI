package com.soccer.forum.service.modules.community.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "私信会话响应")
public class ChatSessionResp {
    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "对方用户ID")
    private Long otherUserId;

    @Schema(description = "对方用户昵称")
    private String otherNickname;

    @Schema(description = "对方用户头像")
    private String otherAvatar;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "我的未读数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "是否免打扰")
    private Boolean isMute;

    @Schema(description = "是否被我拉黑")
    private Boolean isBlacklisted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherNickname() { return otherNickname; }
    public void setOtherNickname(String otherNickname) { this.otherNickname = otherNickname; }
    public String getOtherAvatar() { return otherAvatar; }
    public void setOtherAvatar(String otherAvatar) { this.otherAvatar = otherAvatar; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
    public Boolean getIsTop() { return isTop; }
    public void setIsTop(Boolean isTop) { this.isTop = isTop; }
    public Boolean getIsMute() { return isMute; }
    public void setIsMute(Boolean isMute) { this.isMute = isMute; }
    public Boolean getIsBlacklisted() { return isBlacklisted; }
    public void setIsBlacklisted(Boolean isBlacklisted) { this.isBlacklisted = isBlacklisted; }
}
