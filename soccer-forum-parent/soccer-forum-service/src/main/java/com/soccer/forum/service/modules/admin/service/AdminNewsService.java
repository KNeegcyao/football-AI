package com.soccer.forum.service.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.NewsCreateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.NewsVO;

public interface AdminNewsService {
    Page<NewsVO> getNewsList(int page, int size, String keyword, String category);
    NewsVO createNews(NewsCreateDTO dto);
    NewsVO updateNews(Long id, NewsUpdateDTO dto);
    void deleteNews(Long id);
}
