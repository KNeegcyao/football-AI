package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "评论分页查询请求")
public class CommentPageReq {
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帖子ID不能为空")
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
