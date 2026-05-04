package com.soccer.forum.service.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    private String summary;
    private String content;
    private String coverUrl;
    private String author;

    @Size(max = 200, message = "标签最多200字")
    private String tags;

    private String category;
}
