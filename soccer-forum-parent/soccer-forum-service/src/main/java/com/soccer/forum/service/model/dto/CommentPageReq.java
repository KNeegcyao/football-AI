package com.soccer.forum.service.model.dto;

public class CommentPageReq {
    private Integer page = 1;
    private Integer size = 10;
    private Long postId;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
