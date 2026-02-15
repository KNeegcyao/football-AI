package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 资讯管理控制器
 * <p>
 * 提供新闻资讯的发布、查询、详情获取及删除功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "资讯管理", description = "新闻资讯管理接口")
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 发布资讯接口
     */
    @Operation(summary = "发布资讯", description = "发布新的足球资讯")
    @PostMapping
    public R<Long> create(@Parameter(description = "资讯信息对象") @Validated @RequestBody News news) {
        log.info("收到发布资讯请求: 标题={}", news.getTitle());
        Long id = newsService.createNews(news);
        log.info("资讯发布成功: id={}, 标题={}", id, news.getTitle());
        return R.ok(id, "资讯发布成功");
    }

    /**
     * 获取资讯详情接口
     */
    @Operation(summary = "获取资讯详情", description = "根据ID获取资讯详情，并增加浏览量")
    @GetMapping("/{id}")
    public R<News> get(@Parameter(description = "资讯ID") @PathVariable Long id) {
        log.info("收到获取资讯详情请求: id={}", id);
        News news = newsService.getNewsDetail(id);
        return R.ok(news);
    }

    /**
     * 分页查询资讯列表接口
     */
    @Operation(summary = "查询资讯列表", description = "分页查询资讯列表，支持分类和关键词搜索")
    @GetMapping("/list")
    public R<Page<News>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                              @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                              @Parameter(description = "分类 (如：转会, 战报, 花边)") @RequestParam(required = false) String category,
                              @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
                              @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("收到查询资讯列表请求: page={}, size={}, category={}, categoryId={}, keyword={}", page, size, category, categoryId, keyword);
        Page<News> newsPage = newsService.listNews(page, size, category, categoryId, keyword);
        return R.ok(newsPage);
    }

    /**
     * 更新资讯接口
     */
    @Operation(summary = "更新资讯", description = "更新已发布的资讯内容")
    @PutMapping("/{id}")
    public R<Void> update(@Parameter(description = "资讯ID") @PathVariable Long id, 
                          @Parameter(description = "资讯更新信息对象") @RequestBody News news) {
        log.info("收到更新资讯请求: id={}", id);
        newsService.updateNews(id, news);
        return R.ok(null, "资讯更新成功");
    }

    /**
     * 删除资讯接口
     */
    @Operation(summary = "删除资讯", description = "根据ID删除资讯")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "资讯ID") @PathVariable Long id) {
        log.info("收到删除资讯请求: id={}", id);
        newsService.deleteNews(id);
        return R.ok(null, "资讯删除成功");
    }
}
