package com.soccer.forum.service.modules.system.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.soccer.forum.service.config.OssConfig;
import com.soccer.forum.service.modules.system.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class OssServiceImpl implements OssService {
    private static final Logger log = LoggerFactory.getLogger(OssServiceImpl.class);

    private final OSS ossClient;
    private final OssConfig ossConfig;

    public OssServiceImpl(OSS ossClient, OssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName) {
        try {
            log.info("开始上传文件到 OSS, objectName: {}", objectName);
            
            ObjectMetadata metadata = new ObjectMetadata();
            // 记录原始 Content-Type
            String contentType = "application/octet-stream";
            
            // 根据后缀设置 Content-Type
            if (objectName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (objectName.endsWith(".png")) {
                contentType = "image/png";
            } else if (objectName.endsWith(".jpg") || objectName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (objectName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (objectName.endsWith(".webp")) {
                contentType = "image/webp";
            }
            metadata.setContentType(contentType);
            
            try {
                // 尝试设置公共读权限
                metadata.setHeader("x-oss-object-acl", CannedAccessControlList.PublicRead.toString());
                PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, inputStream, metadata);
                ossClient.putObject(putObjectRequest);
            } catch (Exception aclEx) {
                log.warn("为对象 {} 设置公共读权限失败，尝试普通上传: {}", objectName, aclEx.getMessage());
                // 重置输入流（如果有必要，但 InputStream 通常不能重置，这里我们可能需要重新获取流，
                // 但在 syncIcons 中我们用的是 resource.getInputStream()，每次调用都是新的流）
                // 这里的异常通常发生在 ossClient.putObject 内部，所以如果失败，我们需要在外层处理重试
                throw aclEx; 
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
}
