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
@TableName("chat_message")
@Schema(description = "私信消息实体")
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "所属会话ID")
    private Long sessionId;

    @Schema(description = "发送者用户ID")
    private Long senderId;

    @Schema(description = "接收者用户ID")
    private Long receiverId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型 (0:文字, 1:图片, 2:战术板分享卡片)")
    private Integer type;

    @Schema(description = "状态 (0:未读, 1:已读, 2:已撤回)")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime createdAt;
}
