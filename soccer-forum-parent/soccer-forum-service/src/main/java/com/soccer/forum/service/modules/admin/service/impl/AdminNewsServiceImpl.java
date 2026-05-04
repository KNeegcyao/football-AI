package com.soccer.forum.service.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.NewsCreateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsVO;
import com.soccer.forum.service.modules.admin.service.AdminNewsService;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.modules.match.mapper.NewsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNewsServiceImpl implements AdminNewsService {

    private final NewsMapper newsMapper;

    @Override
    public Page<NewsVO> getNewsList(int page, int size, String keyword, String category) {
        Page<News> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(News::getTitle, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(News::getCategory, category);
        }
        wrapper.orderByDesc(News::getCreatedAt);
        Page<News> newsPage = newsMapper.selectPage(pageParam, wrapper);

        List<NewsVO> records = newsPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        Page<NewsVO> result = new Page<>(newsPage.getCurrent(), newsPage.getSize(), newsPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public NewsVO createNews(NewsCreateDTO dto) {
        News news = new News();
        BeanUtils.copyProperties(dto, news);
        news.setLikeCount(0);
        news.setCommentCount(0);
        news.setCollectCount(0);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.insert(news);
        return convertToVO(news);
    }

    @Override
    public NewsVO updateNews(Long id, NewsUpdateDTO dto) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("资讯不存在");
        }
        BeanUtils.copyProperties(dto, news);
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
        return convertToVO(news);
    }

    @Override
    public void deleteNews(Long id) {
        newsMapper.deleteById(id);
    }

    private NewsVO convertToVO(News news) {
        NewsVO vo = new NewsVO();
        BeanUtils.copyProperties(news, vo);
        vo.setCreatedAt(news.getCreatedAt());
        vo.setUpdatedAt(news.getUpdatedAt());
        return vo;
    }
}
