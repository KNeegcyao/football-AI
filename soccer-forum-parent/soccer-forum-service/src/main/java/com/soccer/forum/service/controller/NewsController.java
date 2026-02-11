package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.service.NewsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody News news) {
        Long id = newsService.createNews(news);
        return Map.of("code", 200, "msg", "success", "data", id);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        News news = newsService.getNewsDetail(id);
        return Map.of("code", 200, "msg", "success", "data", news);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody News news) {
        newsService.updateNews(id, news);
        return Map.of("code", 200, "msg", "success");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        newsService.deleteNews(id);
        return Map.of("code", 200, "msg", "success");
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String keyword) {
        Page<News> result = newsService.listNews(page, size, category, keyword);
        return Map.of("code", 200, "msg", "success", "data", result);
    }
}
