package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "评论创建请求")
public class CommentCreateReq {
    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @Schema(description = "父评论ID (如果是回复评论则必填)")
    private Long parentId; // 可选，回复某条评论

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    @Schema(description = "父评论ID (如果是回复评论则必填)")
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
