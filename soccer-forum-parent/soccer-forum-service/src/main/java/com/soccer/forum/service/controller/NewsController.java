package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
@RequestMapping("/api/v2/news")
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 发布资讯接口
     * <p>
     * 发布新的足球资讯，包括标题、内容、分类等。
     * </p>
     *
     * @param news 资讯实体对象
     * @return 包含新资讯 ID 的响应结果
     */
    @Operation(summary = "发布资讯", description = "发布新的足球资讯")
    @PostMapping
    public Map<String, Object> create(@RequestBody News news) {
        log.info("收到发布资讯请求: 标题={}", news.getTitle());
        try {
            Long id = newsService.createNews(news);
            log.info("资讯发布成功: id={}, 标题={}", id, news.getTitle());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", id);
            return result;
        } catch (Exception e) {
            log.error("资讯发布失败: 标题={}", news.getTitle(), e);
            throw e;
        }
    }

    /**
     * 获取资讯详情接口
     * <p>
     * 根据 ID 获取资讯详情，并同步增加浏览量。
     * </p>
     *
     * @param id 资讯 ID
     * @return 包含资讯详情的响应结果
     */
    @Operation(summary = "获取资讯详情", description = "根据ID获取资讯详情，并增加浏览量")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        log.info("收到获取资讯详情请求: id={}", id);
        News news = newsService.getNewsDetail(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", news);
        return result;
    }

    /**
     * 更新资讯接口
     * <p>
     * 更新已发布的资讯内容。
     * </p>
     *
     * @param id 资讯 ID
     * @param news 更新后的资讯实体对象
     * @return 操作结果
     */
    @Operation(summary = "更新资讯", description = "更新已发布的资讯内容")
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody News news) {
        log.info("收到更新资讯请求: id={}", id);
        try {
            newsService.updateNews(id, news);
            log.info("资讯更新成功: id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            return result;
        } catch (Exception e) {
            log.error("资讯更新失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 删除资讯接口
     * <p>
     * 根据 ID 删除指定的资讯。
     * </p>
     *
     * @param id 资讯 ID
     * @return 操作结果
     */
    @Operation(summary = "删除资讯", description = "删除指定的资讯")
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("收到删除资讯请求: id={}", id);
        try {
            newsService.deleteNews(id);
            log.info("资讯删除成功: id={}", id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "success");
            return result;
        } catch (Exception e) {
            log.error("资讯删除失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 分页查询资讯列表接口
     * <p>
     * 支持按分类、关键词（标题或标签）进行模糊搜索。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param category 资讯分类（可选）
     * @param keyword 搜索关键词（可选）
     * @return 包含资讯分页数据的响应结果
     */
    @Operation(summary = "分页查询资讯", description = "支持按分类、关键词搜索")
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String keyword) {
        log.info("收到分页查询资讯请求: 页码={}, 大小={}, 分类={}, 关键词={}", page, size, category, keyword);
        Page<News> result = newsService.listNews(page, size, category, keyword);
        
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", result);
        return map;
    }
}
