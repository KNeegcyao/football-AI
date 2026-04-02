package com.soccer.forum.service.modules.system.service;

import java.io.InputStream;

public interface OssService {
    /**
     * 上传文件到 OSS
     * @param inputStream 文件流
     * @param objectName OSS 对象名称 (包括路径)
     * @return 文件的访问 URL
     */
    String uploadFile(InputStream inputStream, String objectName);

    /**
     * 根据对象名称获取文件的访问 URL
     * @param objectName OSS 对象名称
     * @return 文件的访问 URL
     */
    String getFileUrl(String objectName);
}
