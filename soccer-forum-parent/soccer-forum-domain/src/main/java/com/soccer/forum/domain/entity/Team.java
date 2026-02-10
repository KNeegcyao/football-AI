package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("teams")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String logoUrl;
    private Integer foundedYear;
    private String homeStadium;
    private String league;
    private String coachName;
    private String formation;
    private Integer totalMatches;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
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
