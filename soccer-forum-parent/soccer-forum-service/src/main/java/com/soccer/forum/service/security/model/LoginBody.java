package com.soccer.forum.service.security.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录/注册请求体")
public class LoginBody {
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    @Schema(description = "昵称 (注册时使用)")
    private String nickname;

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    @Schema(description = "昵称 (注册时使用)")
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
