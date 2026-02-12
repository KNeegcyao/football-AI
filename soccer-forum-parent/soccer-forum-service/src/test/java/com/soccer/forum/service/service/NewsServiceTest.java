package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.exception.ServiceException;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsServiceTest {

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private NewsServiceImpl newsService;

    private News testNews;

    @BeforeEach
    void setUp() {
        testNews = new News();
        testNews.setId(1L);
        testNews.setTitle("Test News");
        testNews.setContent("Test Content");
        testNews.setCategory("Test Category");
    }

    @Test
    void testCreateNews() {
        when(newsMapper.insert(any(News.class))).thenReturn(1);
        
        Long id = newsService.createNews(testNews);
        
        assertNotNull(id);
        verify(newsMapper, times(1)).insert(any(News.class));
    }

    @Test
    void testGetNewsDetail_Success() {
        when(newsMapper.selectById(1L)).thenReturn(testNews);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        News result = newsService.getNewsDetail(1L);

        assertNotNull(result);
        assertEquals("Test News", result.getTitle());
        verify(newsMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetNewsDetail_NotFound() {
        when(newsMapper.selectById(1L)).thenReturn(null);

        News result = newsService.getNewsDetail(1L);

        assertNull(result);
        verify(newsMapper, times(1)).selectById(1L);
    }

    @Test
    void testListNews() {
        Page<News> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(testNews));
        when(newsMapper.selectPage(any(Page.class), any())).thenReturn(page);

        Page<News> result = newsService.listNews(1, 10, "Test Category", "Test");

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(newsMapper, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    void testUpdateNews_Success() {
        when(newsMapper.updateById(any(News.class))).thenReturn(1);

        assertDoesNotThrow(() -> newsService.updateNews(1L, testNews));
        verify(newsMapper, times(1)).updateById(any(News.class));
    }

    @Test
    void testUpdateNews_NotFound() {
        when(newsMapper.updateById(any(News.class))).thenReturn(0);

        assertThrows(ServiceException.class, () -> newsService.updateNews(1L, testNews));
    }

    @Test
    void testDeleteNews_Success() {
        when(newsMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> newsService.deleteNews(1L));
        verify(newsMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNews_NotFound() {
        when(newsMapper.deleteById(1L)).thenReturn(0);

        assertThrows(ServiceException.class, () -> newsService.deleteNews(1L));
    }
}
