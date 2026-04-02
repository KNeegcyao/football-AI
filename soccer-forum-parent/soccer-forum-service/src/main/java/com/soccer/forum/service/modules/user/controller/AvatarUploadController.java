package com.soccer.forum.service.modules.user.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.modules.system.service.OssService;
import com.soccer.forum.service.modules.user.model.LoginUser;
import com.soccer.forum.service.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 用户头像上传控制器
 */
@Tag(name = "用户头像", description = "专门处理用户头像上传与更新的接口")
@RestController
@RequestMapping("/api/user/avatar")
public class AvatarUploadController {

    private static final Logger log = LoggerFactory.getLogger(AvatarUploadController.class);

    private final OssService ossService;
    private final UserService userService;

    public AvatarUploadController(OssService ossService, UserService userService) {
        this.ossService = ossService;
        this.userService = userService;
    }

    @Operation(summary = "上传头像", description = "上传头像到 OSS 并直接更新当前用户的头像 URL")
    @PostMapping("/upload")
    public R<String> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }

        if (loginUser == null || loginUser.getUser() == null) {
            return R.fail("未获取到登录用户信息");
        }

        Long userId = loginUser.getUser().getId();
        log.info("用户 {} 开始上传头像", userId);

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = "avatar/" + UUID.randomUUID().toString() + extension;

            // 1. 上传到 OSS
            String ossUrl = ossService.uploadFile(file.getInputStream(), objectName);
            log.info("头像上传 OSS 成功: {}", ossUrl);

            // 2. 更新数据库
            User user = new User();
            user.setId(userId);
            user.setAvatar(ossUrl);
            userService.updateById(user);
            log.info("用户 {} 头像数据库更新成功", userId);

            return R.ok(ossUrl, "头像上传并更新成功");
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return R.fail("头像上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("更新用户头像失败", e);
            return R.fail("更新头像失败: " + e.getMessage());
        }
    }
}
