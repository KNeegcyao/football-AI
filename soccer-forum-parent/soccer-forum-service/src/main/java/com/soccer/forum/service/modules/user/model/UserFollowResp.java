package com.soccer.forum.service.modules.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "关注/粉丝用户响应")
public class UserFollowResp {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "是否已认证")
    private Boolean isVerified;

    @Schema(description = "是否当前用户已关注该用户")
    private Boolean isFollowing;

    @Schema(description = "是否该用户已关注当前用户")
    private Boolean isFollower;

    public UserFollowResp() {}

    public UserFollowResp(Long id, String username, String nickname, String avatar, String bio, Boolean isVerified) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.bio = bio;
        this.isVerified = isVerified;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public Boolean getIsFollowing() { return isFollowing; }
    public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }
    public Boolean getIsFollower() { return isFollower; }
    public void setIsFollower(Boolean isFollower) { this.isFollower = isFollower; }
}
