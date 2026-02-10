package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "players", autoResultMap = true)
public class Player {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String photoUrl;
    private LocalDate birthDate;
    private String nationality;
    private String position;
    private Integer height;
    private Integer weight;
    private String preferredFoot;
    private Long currentTeamId;
    private Integer jerseyNumber;
    private BigDecimal marketValue;
    private LocalDate contractUntil;
    private Integer appearances;
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> careerHistory;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getPreferredFoot() { return preferredFoot; }
    public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }
    public Long getCurrentTeamId() { return currentTeamId; }
    public void setCurrentTeamId(Long currentTeamId) { this.currentTeamId = currentTeamId; }
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    public LocalDate getContractUntil() { return contractUntil; }
    public void setContractUntil(LocalDate contractUntil) { this.contractUntil = contractUntil; }
    public Integer getAppearances() { return appearances; }
    public void setAppearances(Integer appearances) { this.appearances = appearances; }
    public Integer getGoals() { return goals; }
    public void setGoals(Integer goals) { this.goals = goals; }
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }
    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }
    public Map<String, Object> getCareerHistory() { return careerHistory; }
    public void setCareerHistory(Map<String, Object> careerHistory) { this.careerHistory = careerHistory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
