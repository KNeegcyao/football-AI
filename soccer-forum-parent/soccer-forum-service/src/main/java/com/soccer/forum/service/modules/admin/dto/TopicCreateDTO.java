package com.soccer.forum.service.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicCreateDTO {
    @NotBlank(message = "话题标题不能为空")
    @Size(max = 50, message = "话题标题最多50字")
    private String title;

    @Size(max = 200, message = "话题描述最多200字")
    private String description;
}
