package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.mapper.NewsMapper;
import com.soccer.forum.service.service.impl.NewsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private NewsServiceImpl newsService;

    private News news;
    private Long newsId = 1L;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setId(newsId);
        news.setTitle("Breaking News");
        news.setViewCount(100);
    }

    @Test
    void createNews_Success() {
        when(newsMapper.insert(any(News.class))).thenReturn(1);

        Long resultId = newsService.createNews(news);

        assertEquals(newsId, resultId);
        verify(newsMapper).insert(any(News.class));
    }

    @Test
    void getNewsDetail_Success_IncrementsViews_Redis() {
        when(newsMapper.selectById(newsId)).thenReturn(news);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        News result = newsService.getNewsDetail(newsId);

        assertNotNull(result);
        assertEquals(101, result.getViewCount()); // 100 + 1 (from memory update)
        // Redis count is 1, so no DB update
        verify(newsMapper, never()).updateById(any(News.class));
    }

    @Test
    void getNewsDetail_Success_SyncsToDB_Every10thView() {
        when(newsMapper.selectById(newsId)).thenReturn(news);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(10L);

        News result = newsService.getNewsDetail(newsId);

        assertNotNull(result);
        assertEquals(110, result.getViewCount()); // 100 + 10 (synced to DB)
        verify(newsMapper).updateById(any(News.class));
    }

    @Test
    void listNews_Success() {
        when(newsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<News>());

        Page<News> result = newsService.listNews(1, 10, "Transfer", "Messi");

        assertNotNull(result);
        verify(newsMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void updateNews_Success() {
        when(newsMapper.updateById(any(News.class))).thenReturn(1);

        assertDoesNotThrow(() -> newsService.updateNews(newsId, news));
        
        verify(newsMapper).updateById(any(News.class));
    }

    @Test
    void updateNews_NotFound_ThrowsException() {
        when(newsMapper.updateById(any(News.class))).thenReturn(0);

        assertThrows(RuntimeException.class, () -> newsService.updateNews(newsId, news));
    }

    @Test
    void deleteNews_Success() {
        when(newsMapper.deleteById(newsId)).thenReturn(1);

        assertDoesNotThrow(() -> newsService.deleteNews(newsId));
        
        verify(newsMapper).deleteById(newsId);
    }

    @Test
    void deleteNews_NotFound_ThrowsException() {
        when(newsMapper.deleteById(newsId)).thenReturn(0);

        assertThrows(RuntimeException.class, () -> newsService.deleteNews(newsId));
    }
}
