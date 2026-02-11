package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;

/**
 * 帖子服务接口
 * <p>
 * 定义帖子管理的核心业务逻辑，包括发布、查询和删除。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface PostService {

    /**
     * 创建帖子
     *
     * @param req 帖子创建请求参数
     * @param userId 发布用户的 ID
     * @return 新创建帖子的 ID
     */
    Long createPost(PostCreateReq req, Long userId);

    /**
     * 根据 ID 获取帖子详情
     *
     * @param id 帖子 ID
     * @return 帖子实体对象，若不存在返回 null
     */
    Post getPostById(Long id);

    /**
     * 分页查询帖子列表
     *
     * @param req 分页查询参数（页码、大小、关键词）
     * @return 帖子分页对象
     */
    Page<Post> getPostPage(PostPageReq req);

    /**
     * 删除帖子
     *
     * @param id 帖子 ID
     * @param userId 操作用户的 ID（用于权限校验）
     */
    void deletePost(Long id, Long userId);
}
