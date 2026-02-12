package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.service.mapper.NewsMapper;
import com.soccer.forum.service.service.NewsService;
import com.soccer.forum.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 资讯服务实现类
 * <p>
 * 实现新闻资讯管理的具体业务逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class NewsServiceImpl implements NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

    private final NewsMapper newsMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public NewsServiceImpl(NewsMapper newsMapper, org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.newsMapper = newsMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建资讯实现
     * <p>
     * 初始化资讯创建时间、更新时间和发布时间（如果未指定），持久化到数据库。
     * </p>
     *
     * @param news 资讯实体对象
     * @return 新创建资讯的 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNews(News news) {
        log.debug("创建资讯: 标题={}", news.getTitle());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        newsMapper.insert(news);
        log.info("资讯创建成功: id={}", news.getId());
        return news.getId();
    }

    /**
     * 获取资讯详情实现
     * <p>
     * 查询资讯信息，若存在则同步增加浏览量。
     * 使用 Redis 计数，每 10 次访问同步一次数据库。
     * </p>
     *
     * @param id 资讯 ID
     * @return 资讯实体对象
     */
    @Override
    public News getNewsDetail(Long id) {
        log.debug("获取资讯详情: id={}", id);
        News news = newsMapper.selectById(id);
        if (news != null) {
            try {
                // 使用 Redis 增加浏览量
                String key = "news:views:" + id;
                Long views = redisTemplate.opsForValue().increment(key);
                
                // 每 10 次访问同步一次数据库，减轻数据库压力
                if (views != null && views % 10 == 0) {
                    news.setViewCount(news.getViewCount() == null ? 10 : news.getViewCount() + 10);
                    newsMapper.updateById(news);
                    log.debug("同步资讯浏览量到数据库: id={}, views={}", id, news.getViewCount());
                } else {
                    // 仅在内存对象中增加，用于返回给前端展示实时数据
                    news.setViewCount((news.getViewCount() == null ? 0 : news.getViewCount()) + (views == null ? 0 : views.intValue() % 10));
                }
            } catch (Exception e) {
                log.warn("Redis 操作失败, 仅使用数据库数据: {}", e.getMessage());
                // Redis 失败时，返回数据库中的数据，并记录日志
            }
        }
        return news;
    }

    /**
     * 分页查询资讯列表实现
     * <p>
     * 支持按分类、关键词（标题或标签）进行模糊搜索，按发布时间倒序排列。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param category 资讯分类
     * @param keyword 搜索关键词
     * @return 资讯分页对象
     */
    @Override
    public Page<News> listNews(Integer page, Integer size, String category, String keyword) {
        log.debug("分页查询资讯: 页码={}, 大小={}, 分类={}, 关键词={}", page, size, category, keyword);
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

    /**
     * 更新资讯信息实现
     * <p>
     * 更新资讯的基本信息和更新时间。
     * </p>
     *
     * @param id 资讯 ID
     * @param news 更新后的资讯实体对象
     * @throws RuntimeException 当资讯不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNews(Long id, News news) {
        log.debug("更新资讯: id={}", id);
        news.setId(id);
        news.setUpdatedAt(LocalDateTime.now());
        int rows = newsMapper.updateById(news);
        if (rows == 0) {
            log.warn("资讯更新失败, 未找到资讯: id={}", id);
            throw new ServiceException("未找到资讯");
        }
        log.info("资讯更新成功: id={}", id);
    }

    /**
     * 删除资讯实现
     * <p>
     * 根据 ID 删除资讯数据。
     * </p>
     *
     * @param id 资讯 ID
     * @throws ServiceException 当资讯不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNews(Long id) {
        log.debug("删除资讯: id={}", id);
        int rows = newsMapper.deleteById(id);
        if (rows == 0) {
            log.warn("资讯删除失败, 未找到资讯: id={}", id);
            throw new ServiceException("未找到资讯");
        }
        log.info("资讯删除成功: id={}", id);
    }
}
