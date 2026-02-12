package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@TableName("teams")
@Schema(description = "球队实体")
public class Team {
    @TableId(type = IdType.AUTO)
    @Schema(description = "球队ID")
    private Long id;

    @Schema(description = "名称")
    private String name;
    @Schema(description = "Logo URL")
    private String logoUrl;
    @Schema(description = "成立年份")
    private Integer foundedYear;
    @Schema(description = "主场馆")
    private String homeStadium;
    @Schema(description = "联赛")
    private String league;
    @Schema(description = "教练姓名")
    private String coachName;
    @Schema(description = "阵型")
    private String formation;
    @Schema(description = "总场次")
    private Integer totalMatches;
    @Schema(description = "胜场")
    private Integer wins;
    @Schema(description = "平场")
    private Integer draws;
    @Schema(description = "负场")
    private Integer losses;
    @Schema(description = "进球数")
    private Integer goalsFor;
    @Schema(description = "失球数")
    private Integer goalsAgainst;

    @TableField(value = "created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
    public String getHomeStadium() { return homeStadium; }
    public void setHomeStadium(String homeStadium) { this.homeStadium = homeStadium; }
    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }
    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }
    public Integer getTotalMatches() { return totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }
    public Integer getWins() { return wins; }
    public void setWins(Integer wins) { this.wins = wins; }
    public Integer getDraws() { return draws; }
    public void setDraws(Integer draws) { this.draws = draws; }
    public Integer getLosses() { return losses; }
    public void setLosses(Integer losses) { this.losses = losses; }
    public Integer getGoalsFor() { return goalsFor; }
    public void setGoalsFor(Integer goalsFor) { this.goalsFor = goalsFor; }
    public Integer getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(Integer goalsAgainst) { this.goalsAgainst = goalsAgainst; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
