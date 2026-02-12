package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "帖子创建请求")
public class PostCreateReq {
    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度必须在2-100个字符之间")
    private String title;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "内容不能为空")
    @Size(min = 5, message = "内容不能少于5个字符")
    private String content;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
