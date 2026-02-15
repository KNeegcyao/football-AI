package com.soccer.forum.service.model.dto;

import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.domain.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "赛事详情响应对象")
public class MatchVO extends Match implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主队详情")
    private Team homeTeam;

    @Schema(description = "客队详情")
    private Team awayTeam;

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
}
