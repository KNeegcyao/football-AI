package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_session")
@Schema(description = "私信会话实体")
public class ChatSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "参与者1 (ID较小者)")
    private Long userOneId;

    @Schema(description = "参与者2 (ID较大者)")
    private Long userTwoId;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "参与者1的未读数")
    private Integer unreadCountOne;

    @Schema(description = "参与者2的未读数")
    private Integer unreadCountTwo;

    @Schema(description = "参与者1是否置顶")
    private Integer isTopOne;

    @Schema(description = "参与者2是否置顶")
    private Integer isTopTwo;

    @Schema(description = "参与者1是否免打扰")
    private Integer isMuteOne;

    @Schema(description = "参与者2是否免打扰")
    private Integer isMuteTwo;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
