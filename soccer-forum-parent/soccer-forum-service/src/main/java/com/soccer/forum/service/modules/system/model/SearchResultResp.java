package com.soccer.forum.service.modules.system.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.News;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.modules.match.model.MatchVO;
import com.soccer.forum.service.modules.community.model.PostDetailResp;

import java.io.Serializable;

/**
 * 全局搜索结果
 */
@Schema(description = "全局搜索结果响应")
public class SearchResultResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "帖子列表")
    private Page<PostDetailResp> posts;
    @Schema(description = "新闻列表")
    private Page<News> news;
    @Schema(description = "球队列表")
    private Page<Team> teams;
    @Schema(description = "球员列表")
    private Page<Player> players;
    @Schema(description = "赛事列表")
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
