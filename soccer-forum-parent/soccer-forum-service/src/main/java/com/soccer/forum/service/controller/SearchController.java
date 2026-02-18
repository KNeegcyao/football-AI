package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.model.dto.PostDetailResp;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.model.dto.SearchResultResp;
import com.soccer.forum.service.model.dto.MatchVO;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.MatchService;
import com.soccer.forum.service.service.NewsService;
import com.soccer.forum.service.service.PlayerService;
import com.soccer.forum.service.service.PostService;
import com.soccer.forum.service.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "搜索管理", description = "全局搜索相关接口")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final PostService postService;
    private final NewsService newsService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;

    public SearchController(PostService postService, NewsService newsService, TeamService teamService, PlayerService playerService, MatchService matchService) {
        this.postService = postService;
        this.newsService = newsService;
        this.teamService = teamService;
        this.playerService = playerService;
        this.matchService = matchService;
    }

    @Operation(summary = "全局搜索", description = "一次性搜索帖子、资讯、球队和球员")
    @GetMapping
    public R<SearchResultResp> search(@Parameter(description = "搜索关键词") @RequestParam String keyword,
                                      @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                      @Parameter(description = "每页大小") @RequestParam(defaultValue = "5") Integer size,
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到全局搜索请求: keyword={}, page={}, size={}", keyword, page, size);
        
        SearchResultResp result = new SearchResultResp();
        
        // 1. 搜索帖子
        PostPageReq postReq = new PostPageReq();
        postReq.setKeyword(keyword);
        postReq.setPage(page);
        postReq.setSize(size);
        Long userId = (loginUser != null && loginUser.getUser() != null) ? loginUser.getUser().getId() : null;
        Page<PostDetailResp> postPage = postService.getPostPage(postReq, userId);
        result.setPosts(postPage);
        
        // 2. 搜索资讯
        Page<News> newsPage = newsService.listNews(page, size, null, null, keyword);
        result.setNews(newsPage);
        
        // 3. 搜索球队
        Page<Team> teamPage = teamService.listTeams(page, size, keyword);
        result.setTeams(teamPage);
        
        // 4. 搜索球员
        Page<Player> playerPage = playerService.listPlayers(page, size, keyword, null);
        result.setPlayers(playerPage);

        // 5. 搜索赛事
        Page<MatchVO> matchPage = matchService.searchMatches(page, size, keyword);
        result.setMatches(matchPage);
        
        log.info("搜索完成: 帖子数={}, 资讯数={}, 球队数={}, 球员数={}, 赛事数={}", 
                postPage.getTotal(), newsPage.getTotal(), teamPage.getTotal(), playerPage.getTotal(), matchPage.getTotal());
        
        return R.ok(result);
    }
}
