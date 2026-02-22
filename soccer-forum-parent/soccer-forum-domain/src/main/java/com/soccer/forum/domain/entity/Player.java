package com.soccer.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "players", autoResultMap = true)
@Schema(description = "球员实体")
public class Player {
    @TableId(type = IdType.AUTO)
    @Schema(description = "球员ID")
    private Long id;

    @Schema(description = "第三方API的唯一ID")
    private Integer apiId;

    @Schema(description = "数据来源标识")
    private String sourceFrom;

    @Schema(description = "英文原名")
    private String name;

    @Schema(description = "中文显示名称")
    private String displayName;

    @Schema(description = "名字")
    private String firstname;

    @Schema(description = "姓氏")
    private String lastname;

    @Schema(description = "URL别名")
    private String slug;

    @Schema(description = "照片URL")
    private String photoUrl;
    @Schema(description = "出生日期")
    private LocalDate birthDate;
    @Schema(description = "出生地")
    private String birthPlace;
    @Schema(description = "国籍")
    private String nationality;
    @Schema(description = "场上位置")
    private String position;

    @Schema(description = "详细位置")
    private String detailedPos;

    @Schema(description = "身高 (cm)")
    private Integer height;
    @Schema(description = "体重 (kg)")
    private Integer weight;
    @Schema(description = "惯用脚")
    private String preferredFoot;
    @Schema(description = "当前球队ID")
    private Long currentTeamId;
    @Schema(description = "球衣号码")
    private Integer jerseyNumber;
    @Schema(description = "市场价值")
    private BigDecimal marketValue;

    @Schema(description = "状态 (active, injured, suspended)")
    private String status;

    @Schema(description = "合同到期日")
    private LocalDate contractUntil;
    @Schema(description = "出场次数")
    private Integer appearances;
    @Schema(description = "进球数")
    private Integer goals;
    @Schema(description = "助攻数")
    private Integer assists;
    @Schema(description = "黄牌数")
    private Integer yellowCards;
    @Schema(description = "红牌数")
    private Integer redCards;

    @Schema(description = "赛季平均评分")
    private BigDecimal rating;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "职业生涯历史 (JSON)")
    private Map<String, Object> careerHistory;

    @TableField(value = "created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "球员ID")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    @Schema(description = "第三方API的唯一ID")
    public Integer getApiId() { return apiId; }
    public void setApiId(Integer apiId) { this.apiId = apiId; }

    @Schema(description = "数据来源标识")
    public String getSourceFrom() { return sourceFrom; }
    public void setSourceFrom(String sourceFrom) { this.sourceFrom = sourceFrom; }

    @Schema(description = "英文原名")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Schema(description = "中文显示名称")
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    @Schema(description = "照片URL")
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    @Schema(description = "出生日期")
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    @Schema(description = "出生地")
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    @Schema(description = "国籍")
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    @Schema(description = "场上位置")
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    @Schema(description = "详细位置")
    public String getDetailedPos() { return detailedPos; }
    public void setDetailedPos(String detailedPos) { this.detailedPos = detailedPos; }

    @Schema(description = "身高 (cm)")
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    @Schema(description = "体重 (kg)")
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    @Schema(description = "惯用脚")
    public String getPreferredFoot() { return preferredFoot; }
    public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }
    @Schema(description = "当前球队ID")
    public Long getCurrentTeamId() { return currentTeamId; }
    public void setCurrentTeamId(Long currentTeamId) { this.currentTeamId = currentTeamId; }
    @Schema(description = "球衣号码")
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    @Schema(description = "市场价值")
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    @Schema(description = "状态 (active, injured, suspended)")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Schema(description = "合同到期日")
    public LocalDate getContractUntil() { return contractUntil; }
    public void setContractUntil(LocalDate contractUntil) { this.contractUntil = contractUntil; }
    @Schema(description = "出场次数")
    public Integer getAppearances() { return appearances; }
    public void setAppearances(Integer appearances) { this.appearances = appearances; }
    @Schema(description = "进球数")
    public Integer getGoals() { return goals; }
    public void setGoals(Integer goals) { this.goals = goals; }
    @Schema(description = "助攻数")
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    @Schema(description = "黄牌数")
    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }
    @Schema(description = "红牌数")
    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }

    @Schema(description = "赛季平均评分")
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    @Schema(description = "职业生涯历史 (JSON)")
    public Map<String, Object> getCareerHistory() { return careerHistory; }
    public void setCareerHistory(Map<String, Object> careerHistory) { this.careerHistory = careerHistory; }
    @Schema(description = "创建时间")
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @Schema(description = "更新时间")
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
