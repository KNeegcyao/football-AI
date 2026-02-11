package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.mapper.NewsMapper;
import com.soccer.forum.service.service.NewsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;

    public NewsServiceImpl(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    @Override
    public Long createNews(News news) {
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        newsMapper.insert(news);
        return news.getId();
    }

    @Override
    public News getNewsDetail(Long id) {
        News news = newsMapper.selectById(id);
        if (news != null) {
            // 简单增加浏览量，实际项目中可以用Redis优化
            news.setViewCount(news.getViewCount() == null ? 1 : news.getViewCount() + 1);
            newsMapper.updateById(news);
        }
        return news;
    }

    @Override
    public Page<News> listNews(Integer page, Integer size, String category, String keyword) {
        Page<News> newsPage = new Page<>(page, size);
        LambdaQueryWrapper<News> query = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            query.eq(News::getCategory, category);
        }
        
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(News::getTitle, keyword)
                          .or()
                          .like(News::getTags, keyword));
        }
        
        query.orderByDesc(News::getPublishTime);
        return newsMapper.selectPage(newsPage, query);
    }

    @Override
    public void updateNews(Long id, News news) {
        news.setId(id);
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
    }

    @Override
    public void deleteNews(Long id) {
        newsMapper.deleteById(id);
    }
}
