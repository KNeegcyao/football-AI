package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;

/**
 * 资讯服务接口
 * <p>
 * 定义新闻资讯管理的核心业务逻辑，包括发布、查询、更新和删除。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface NewsService {

    /**
     * 创建资讯
     *
     * @param news 资讯实体对象
     * @return 新创建资讯的 ID
     */
    Long createNews(News news);

    /**
     * 根据 ID 获取资讯详情
     *
     * @param id 资讯 ID
     * @return 资讯实体对象，若不存在返回 null
     */
    News getNewsDetail(Long id);

    /**
     * 分页查询资讯列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param category 资讯分类（可选）
     * @param categoryId 资讯分类ID（可选）
     * @param keyword 搜索关键词（可选）
     * @return 资讯分页对象
     */
    Page<News> listNews(Integer page, Integer size, String category, Integer categoryId, String keyword);

    /**
     * 更新资讯信息
     *
     * @param id 资讯 ID
     * @param news 更新后的资讯实体对象
     */
    void updateNews(Long id, News news);

    /**
     * 删除资讯
     *
     * @param id 资讯 ID
     */
    void deleteNews(Long id);
}
