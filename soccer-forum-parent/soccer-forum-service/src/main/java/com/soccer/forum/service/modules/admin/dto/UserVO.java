package com.soccer.forum.service.modules.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private Integer level;
    private Integer experience;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
