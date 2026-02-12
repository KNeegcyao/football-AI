package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "评论信息响应")
public class CommentResp {
    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "帖子ID")
    private Long postId;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "父评论ID")
    private Long parentId;
    @Schema(description = "回复给哪个用户ID")
    private Long replyToUserId;
    @Schema(description = "回复给哪个用户的昵称")
    private String replyToNickname;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "点赞数")
    private Integer likes;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "子评论列表")
    private List<CommentResp> replies; // 子评论

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }
    public String getReplyToNickname() { return replyToNickname; }
    public void setReplyToNickname(String replyToNickname) { this.replyToNickname = replyToNickname; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<CommentResp> getReplies() { return replies; }
    public void setReplies(List<CommentResp> replies) { this.replies = replies; }
}
