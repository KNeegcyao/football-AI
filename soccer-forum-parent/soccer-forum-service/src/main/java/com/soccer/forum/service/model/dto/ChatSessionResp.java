package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}
