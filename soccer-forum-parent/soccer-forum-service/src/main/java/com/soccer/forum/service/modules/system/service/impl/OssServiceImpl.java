package com.soccer.forum.service.modules.system.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.soccer.forum.service.config.OssConfig;
import com.soccer.forum.service.modules.system.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class OssServiceImpl implements OssService {
    private static final Logger log = LoggerFactory.getLogger(OssServiceImpl.class);

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    public OssServiceImpl(OSS ossClient, OssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName) {
        // 如果没有配置有效的 AK，或者配置的是 demo，则回退到本地存储
        if (ossConfig.getAccessKeyId() == null || 
            ossConfig.getAccessKeyId().isEmpty() || 
            "demo".equals(ossConfig.getAccessKeyId())) {
            
            return saveToLocal(inputStream, objectName);
        }

        try {
            log.info("开始上传文件到 OSS, objectName: {}", objectName);
            
            ObjectMetadata metadata = new ObjectMetadata();
            // 记录原始 Content-Type
            String contentType = "application/octet-stream";
            
            // 根据后缀设置 Content-Type
            String lowerName = objectName.toLowerCase();
            if (lowerName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (lowerName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerName.endsWith(".webp")) {
                contentType = "image/webp";
            }
            metadata.setContentType(contentType);
            
            // 先执行普通上传
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, inputStream, metadata);
            ossClient.putObject(putObjectRequest);
            
            // 上传成功后，尝试异步设置公共读权限 (如果设置失败，不影响上传结果)
            try {
                ossClient.setObjectAcl(ossConfig.getBucketName(), objectName, CannedAccessControlList.PublicRead);
                log.info("成功为对象 {} 设置公共读权限", objectName);
            } catch (Exception aclEx) {
                log.warn("为对象 {} 设置公共读权限失败 (可能子账号权限不足，请确保 Bucket 开启了公共读): {}", objectName, aclEx.getMessage());
            }
            
            log.info("上传文件到 OSS 成功, objectName: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件到 OSS 彻底失败, objectName: {}, 错误类型: {}, 错误信息: {}", 
                objectName, e.getClass().getName(), e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        // 如果是本地上传，直接返回以 /uploads/ 开头的路径
        if (ossConfig.getAccessKeyId() == null || 
            ossConfig.getAccessKeyId().isEmpty() || 
            "demo".equals(ossConfig.getAccessKeyId())) {
            
            // 前端已经配置了 baseUrl 拼接，所以只返回 /uploads/...
            if (!objectName.startsWith("/")) {
                objectName = "/" + objectName;
            }
            return "/uploads" + objectName;
        }

        if (objectName == null || objectName.isEmpty()) {
            return "";
        }
        
        // 如果配置了 CDN 域名，使用 CDN 域名
        String domain = ossConfig.getCdnDomain();
        if (domain != null && !domain.trim().isEmpty()) {
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain + objectName;
        }

        // 默认使用 OSS 访问地址: https://{bucket}.{endpoint}/{objectName}
        String endpoint = ossConfig.getEndpoint();
        // 如果 endpoint 是带 http/https 的，先处理掉
        if (endpoint.startsWith("http://")) {
            endpoint = endpoint.substring(7);
        } else if (endpoint.startsWith("https://")) {
            endpoint = endpoint.substring(8);
        }
        
        return String.format("https://%s.%s/%s", ossConfig.getBucketName(), endpoint, objectName);
    }

    private String saveToLocal(InputStream inputStream, String objectName) {
        try {
            log.info("回退为本地上传，保存到本地目录: {}", objectName);
            
            // 构建本地存储路径
            File destFile = new File(uploadPath, objectName);
            
            // 确保父目录存在
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new RuntimeException("无法创建本地上传目录: " + parentDir.getAbsolutePath());
            }
            
            // 将输入流写入本地文件
            Files.copy(inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            log.info("本地上传成功: {}", destFile.getAbsolutePath());
            
            // 返回访问 URL（假设 WebConfig 将 /uploads/** 映射到了 uploadPath）
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("本地文件上传失败, objectName: {}, 错误: {}", objectName, e.getMessage());
            throw new RuntimeException("本地文件上传失败: " + e.getMessage(), e);
        }
    }
}
