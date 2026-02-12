package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "评论分页查询请求")
public class CommentPageReq {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long postId;

    @Schema(description = "页码", example = "1")
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    
    @Schema(description = "每页数量", example = "10")
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    
    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
