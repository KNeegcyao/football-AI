package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@TableName("news")
@Schema(description = "资讯实体")
public class News {
    @TableId(type = IdType.AUTO)
    @Schema(description = "资讯ID")
    private Long id;

    @Schema(description = "标题")
    @NotBlank(message = "资讯标题不能为空")
    @Size(min = 2, max = 200, message = "标题长度必须在2-200个字符之间")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "内容")
    @NotBlank(message = "资讯内容不能为空")
    private String content;
    @Schema(description = "封面图URL")
    private String coverUrl;
    @Schema(description = "作者")
    private String author;
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    @Schema(description = "浏览量")
    private Integer viewCount;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "分类")
    private String category;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "点赞数")
    private Integer likeCount;
    @Schema(description = "评论数")
    private Integer commentCount;

    @TableField(value = "created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
}
