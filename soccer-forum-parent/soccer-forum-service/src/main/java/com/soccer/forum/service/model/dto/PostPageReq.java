package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "帖子分页查询请求")
public class PostPageReq {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
    @Schema(description = "搜索关键词")
    private String keyword;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
