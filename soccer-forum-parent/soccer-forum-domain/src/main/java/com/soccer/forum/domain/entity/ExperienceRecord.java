package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("experience_record")
@Schema(description = "经验流水实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "变动金额")
    private Integer amount;

    @Schema(description = "变动原因")
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
