package com.soccer.forum.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.model.dto.MatchVO;
import com.soccer.forum.service.model.dto.PostDetailResp;

import java.io.Serializable;

/**
 * 全局搜索结果
 */
@Schema(description = "全局搜索结果响应")
public class SearchResultResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "帖子搜索结果")
    private Page<PostDetailResp> posts;
    @Schema(description = "资讯搜索结果")
    private Page<News> news;
    @Schema(description = "球队搜索结果")
    private Page<Team> teams;
    @Schema(description = "球员搜索结果")
    private Page<Player> players;
    @Schema(description = "赛事搜索结果")
    private Page<MatchVO> matches;

    public Page<PostDetailResp> getPosts() { return posts; }
    public void setPosts(Page<PostDetailResp> posts) { this.posts = posts; }

    public Page<News> getNews() { return news; }
    public void setNews(Page<News> news) { this.news = news; }

    public Page<Team> getTeams() { return teams; }
    public void setTeams(Page<Team> teams) { this.teams = teams; }

    public Page<Player> getPlayers() { return players; }
    public void setPlayers(Page<Player> players) { this.players = players; }

    public Page<MatchVO> getMatches() { return matches; }
    public void setMatches(Page<MatchVO> matches) { this.matches = matches; }
}
