package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;

import com.soccer.forum.service.model.dto.PostDetailResp;

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
     * 获取帖子详情
     * @param id 帖子ID
     * @param userId 当前用户ID (可选)
     * @return 帖子详情
     */
    PostDetailResp getPostDetail(Long id, Long userId);

    /**
     * 分页查询帖子列表
     *
     * @param req 分页及查询参数
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 帖子分页列表（包含用户信息和点赞状态）
     */
    Page<PostDetailResp> getPostPage(PostPageReq req, Long currentUserId);

    /**
     * 更新帖子
     *
     * @param id 帖子 ID
     * @param req 更新请求参数
     * @param userId 操作用户 ID
     */
    void updatePost(Long id, PostCreateReq req, Long userId);

    /**
     * 删除帖子
     *
     * @param id 帖子 ID
     * @param userId 操作用户的 ID（用于权限校验）
     */
    void deletePost(Long id, Long userId);
}
