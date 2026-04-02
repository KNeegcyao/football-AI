package com.soccer.forum.service.modules.system.controller;

import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.system.service.OssService;
import com.soccer.forum.service.modules.user.model.LoginUser;
import com.soccer.forum.service.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制。
 */
@Tag(name = "文件管理", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final OssService ossService;
    private final UserService userService;

    public FileController(OssService ossService, UserService userService) {
        this.ossService = ossService;
        this.userService = userService;
    }

    @Operation(summary = "上传图片", description = "上传单张图片到阿里云 OSS，返回可访问 URL")
    @PostMapping(value = "/upload", produces = "application/json;charset=UTF-8")
    public R<String> upload(
            @Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型: avatar, posts, etc") @RequestParam(value = "type", defaultValue = "general") String type,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") LoginUser loginUser) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 根据业务类型确定 OSS 路径
            String objectName;
            if ("avatar".equalsIgnoreCase(type)) {
                objectName = "avatar/" + fileName;
            } else if ("posts".equalsIgnoreCase(type)) {
                String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/"));
                objectName = "posts/" + datePath + fileName;
            } else {
                objectName = "uploads/" + type + "/" + fileName;
            }

            // 上传到 OSS
            String url = ossService.uploadFile(file.getInputStream(), objectName);
            
            log.info("文件上传 OSS 成功: {}, 业务类型: {}", url, type);

            // 如果是头像上传，且用户已登录，则直接更新数据库中的头像 URL
            if ("avatar".equalsIgnoreCase(type) && loginUser != null && loginUser.getUser() != null) {
                Long userId = loginUser.getUser().getId();
                log.info("同步更新用户头像: userId={}, avatarUrl={}", userId, url);
                com.soccer.forum.domain.entity.User user = new com.soccer.forum.domain.entity.User();
                user.setId(userId);
                user.setAvatar(url);
                userService.updateById(user);
            }
            
            return R.ok(url, "上传成功");

        } catch (IOException e) {
            log.error("文件上传 OSS 失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
