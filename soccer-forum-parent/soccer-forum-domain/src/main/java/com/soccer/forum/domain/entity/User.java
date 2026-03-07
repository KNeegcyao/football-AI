package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.soccer.forum.domain.enums.UserRole;
import com.soccer.forum.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("users")
@Schema(description = "用户实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "个人签名")
    private String bio;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "背景图URL")
    private String cover;

    @Schema(description = "微信OpenID")
    private String openid;

    @Schema(description = "角色")
    private UserRole role;

    @Schema(description = "状态")
    private UserStatus status;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "经验值")
    private Integer experience;

    @Schema(description = "接收谁的回复提醒: all-所有人, following-关注的人, none-关闭")
    private String replyNotificationType;

    @Schema(description = "是否接收粉丝提醒: receive-接收, never-永不")
    private String fanNotificationType;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
