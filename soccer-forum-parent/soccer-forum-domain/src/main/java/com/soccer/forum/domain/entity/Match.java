package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@TableName("matches")
@Schema(description = "赛事实体")
public class Match {
    @TableId(type = IdType.AUTO)
    @Schema(description = "赛事ID")
    private Long id;

    @Schema(description = "主队ID")
    private Long homeTeamId;
    @Schema(description = "客队ID")
    private Long awayTeamId;
    @Schema(description = "赛事名称")
    private String competitionName;
    @Schema(description = "比赛时间")
    private LocalDateTime matchTime;
    @Schema(description = "场馆")
    private String venue;
    @Schema(description = "状态 (0:未开始, 1:进行中, 2:已结束, 3:延期)")
    private Integer status;
    @Schema(description = "主队得分")
    private Integer homeScore;
    @Schema(description = "客队得分")
    private Integer awayScore;
    @Schema(description = "轮次")
    private String round;
    @Schema(description = "转播地址")
    private String broadcastUrl;
    @Schema(description = "图文直播ID")
    private Long liveTextId;
    @Schema(description = "实时比赛时间")
    private String liveTime;

    @TableField(value = "created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(Long homeTeamId) { this.homeTeamId = homeTeamId; }
    public Long getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(Long awayTeamId) { this.awayTeamId = awayTeamId; }
    public String getCompetitionName() { return competitionName; }
    public void setCompetitionName(String competitionName) { this.competitionName = competitionName; }
    public LocalDateTime getMatchTime() { return matchTime; }
    public void setMatchTime(LocalDateTime matchTime) { this.matchTime = matchTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }
    public String getBroadcastUrl() { return broadcastUrl; }
    public void setBroadcastUrl(String broadcastUrl) { this.broadcastUrl = broadcastUrl; }
    public Long getLiveTextId() { return liveTextId; }
    public void setLiveTextId(Long liveTextId) { this.liveTextId = liveTextId; }
    public String getLiveTime() { return liveTime; }
    public void setLiveTime(String liveTime) { this.liveTime = liveTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
