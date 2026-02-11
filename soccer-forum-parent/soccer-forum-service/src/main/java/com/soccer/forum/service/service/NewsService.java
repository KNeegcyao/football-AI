package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;

public interface NewsService {
    Long createNews(News news);
    News getNewsDetail(Long id);
    Page<News> listNews(Integer page, Integer size, String category, String keyword);
    void updateNews(Long id, News news);
    void deleteNews(Long id);
}
