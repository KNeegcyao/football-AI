package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "用户信息更新请求")
public class UserUpdateReq {

    @Schema(description = "昵称")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "电子邮箱")
    @Email(message = "电子邮箱格式不正确")
    private String email;

    @Schema(description = "手机号码")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    @Schema(description = "昵称")
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    @Schema(description = "头像URL")
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    @Schema(description = "电子邮箱")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Schema(description = "手机号码")
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
