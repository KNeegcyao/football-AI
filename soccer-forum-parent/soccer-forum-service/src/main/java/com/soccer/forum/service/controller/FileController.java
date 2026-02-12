package com.soccer.forum.service.controller;

import com.soccer.forum.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.access-path:http://localhost:8080/uploads/}")
    private String accessPath;

    @Operation(summary = "上传图片", description = "上传单张图片，返回可访问的 URL")
    @PostMapping("/upload")
    public R<String> upload(@Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        try {
            // 确保上传目录存在
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 返回访问 URL
            String fileUrl = accessPath + fileName;
            log.info("文件上传成功: {}", fileUrl);
            return R.ok(fileUrl, "上传成功");

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
