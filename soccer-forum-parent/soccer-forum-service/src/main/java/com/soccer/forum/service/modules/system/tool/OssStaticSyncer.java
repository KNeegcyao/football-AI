package com.soccer.forum.service.modules.system.tool;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.soccer.forum.service.config.OssConfig;
import com.soccer.forum.service.modules.system.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.aliyun.oss.model.SetBucketCORSRequest;
import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;
import java.util.Collections;

@Component
public class OssStaticSyncer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(OssStaticSyncer.class);

    private final OssService ossService;
    private final OSS ossClient;
    private final OssConfig ossConfig;
    
    // 缓存文件名到 URL 的映射
    private static final Map<String, String> iconUrlCache = new HashMap<>();

    public OssStaticSyncer(OssService ossService, OSS ossClient, OssConfig ossConfig) {
        this.ossService = ossService;
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!ossConfig.isSyncOnStartup()) {
            log.info("OSS Static Syncer 已禁用 (sync-on-startup=false)，跳过资源同步。");
            return;
        }
        log.info("====================================================");
        log.info("OSS Static Syncer 启动中...");
        log.info("Bucket Name: {}", ossConfig.getBucketName());
        log.info("Endpoint: {}", ossConfig.getEndpoint());
        log.info("====================================================");
        try {
            log.info("尝试设置 OSS Bucket 为公共读权限及配置 CORS...");
            ossClient.setBucketAcl(ossConfig.getBucketName(), CannedAccessControlList.PublicRead);
            log.info("OSS Bucket 权限设置成功: PublicRead");

            // 配置 CORS，允许前端访问
            SetBucketCORSRequest corsRequest = new SetBucketCORSRequest(ossConfig.getBucketName());
            List<SetBucketCORSRequest.CORSRule> rules = new ArrayList<>();
            SetBucketCORSRequest.CORSRule rule = new SetBucketCORSRequest.CORSRule();
            rule.setAllowedOrigins(Collections.singletonList("*"));
            rule.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD"));
            rule.setAllowedHeaders(Collections.singletonList("*"));
            rule.setExposeHeaders(Collections.singletonList("ETag"));
            rule.setMaxAgeSeconds(3600);
            rules.add(rule);
            corsRequest.setCorsRules(rules);
            ossClient.setBucketCORS(corsRequest);
            log.info("OSS Bucket CORS 配置成功");
            
        } catch (Exception e) {
            log.warn("无法设置 Bucket 权限或 CORS，请手动在阿里云控制台配置: {}", e.getMessage());
        }
        syncIcons();
        syncUploads();
    }

    public void syncUploads() {
        try {
            log.info("开始扫描本地 uploads 目录并同步到 OSS...");
            String uploadPath = "D:/project/football/soccer-forum-parent/soccer-forum-service/uploads";
            java.io.File uploadDir = new java.io.File(uploadPath);
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                return;
            }

            // 递归扫描文件
            scanAndSync(uploadDir, uploadDir.getAbsolutePath());
            log.info("本地 uploads 目录同步完成");
        } catch (Exception e) {
            log.error("同步 uploads 到 OSS 失败", e);
        }
    }

    private void scanAndSync(java.io.File file, String baseAbsPath) {
        if (file.isDirectory()) {
            java.io.File[] files = file.listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    scanAndSync(f, baseAbsPath);
                }
            }
        } else {
            // 计算 objectName (相对于 baseAbsPath 的路径)
            String absPath = file.getAbsolutePath();
            String relativePath = absPath.substring(baseAbsPath.length());
            // 统一使用正斜杠，且去掉开头的斜杠
            String objectName = relativePath.replace("\\", "/");
            if (objectName.startsWith("/")) {
                objectName = objectName.substring(1);
            }

            // 检查 OSS 是否已存在 (如果是 SVG 且配置为强制刷新，才重传)
            try {
                boolean exists = ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
                if (!exists) {
                    log.info("同步旧文件到 OSS (新增): {}", objectName);
                    try (java.io.InputStream is = new java.io.FileInputStream(file)) {
                        ossService.uploadFile(is, objectName);
                    }
                } else {
                    log.debug("文件已存在，跳过同步: {}", objectName);
                }
            } catch (Exception e) {
                log.error("上传文件 {} 失败", objectName, e);
            }
        }
    }

    public void syncIcons() {
        try {
            log.info("开始扫描本地图标并同步到 OSS...");
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // 1. 扫描后端 static 下的所有文件 (包括图标、上传的封面、新闻图片等)
            Resource[] backendResources = resolver.getResources("classpath:static/**/*.*");
            syncResources(backendResources, "classpath:static/");

            // 2. 扫描前端 src/static 下的文件
            try {
                // 注意：在 IDE 环境中可能需要使用 file: 协议
                String frontendStaticPath = "file:D:/project/football/frontend/src/static/**/*.*";
                Resource[] frontendResources = resolver.getResources(frontendStaticPath);
                syncResources(frontendResources, "file:D:/project/football/frontend/src/static/");
            } catch (Exception e) {
                log.warn("扫描前端静态资源失败，请检查路径是否正确: {}", e.getMessage());
            }

            log.info("本地图标扫描完成，缓存了 {} 个文件的 URL", iconUrlCache.size());
        } catch (Exception e) {
            log.error("同步图标到 OSS 失败", e);
        }
    }

    private void syncResources(Resource[] resources, String rootPath) throws Exception {
        for (Resource resource : resources) {
            String fullPath = resource.getURL().getPath();
            String objectName;
            
            if (rootPath.startsWith("classpath:")) {
                int index = fullPath.indexOf("static/");
                if (index == -1) continue;
                objectName = fullPath.substring(index);
            } else {
                // 处理前端路径，保持 static/ 结构
                String marker = "src/static/";
                int index = fullPath.indexOf(marker);
                if (index == -1) continue;
                objectName = fullPath.substring(index + "src/".length());
            }

            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            
            // 检查 OSS 是否已存在
            boolean exists = ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
            String url;
            if (!exists) {
                log.info("同步文件到 OSS: {}", objectName);
                try (InputStream is = resource.getInputStream()) {
                    url = ossService.uploadFile(is, objectName);
                }
            } else {
                log.debug("文件已在 OSS 存在: {}", objectName);
                url = ossService.getFileUrl(objectName);
            }
            
            // 缓存
            iconUrlCache.put(objectName, url);
            iconUrlCache.put(fileName, url);
        }
    }

    /**
     * 根据文件名获取 OSS URL
     * @param fileName 文件名，如 "logo.png"
     * @return 完整的 OSS URL
     */
    public String getIconUrl(String fileName) {
        // 先从缓存拿
        if (iconUrlCache.containsKey(fileName)) {
            return iconUrlCache.get(fileName);
        }
        // 缓存没中，尝试生成（假设已上传）
        return ossService.getFileUrl("static/icons/" + fileName);
    }
}
