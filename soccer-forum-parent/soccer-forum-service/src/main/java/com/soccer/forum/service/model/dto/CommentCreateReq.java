package com.soccer.forum.service.model.dto;

public class CommentCreateReq {
    private Long postId;
    private Long parentId; // 可选，回复某条评论
    private String content;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
