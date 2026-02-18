package com.soccer.forum.service.model.dto;

import com.soccer.forum.domain.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.BeanUtils;

/**
 * 帖子详情响应对象
 */
@Schema(description = "帖子详情响应")
public class PostDetailResp extends Post {

    @Schema(description = "发帖人昵称")
    private String userName;

    @Schema(description = "发帖人头像URL")
    private String userAvatar;

    @Schema(description = "关联话题名称")
    private String topicName;

    @Schema(description = "关联圈子名称")
    private String circleName;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "最近点赞用户")
    private java.util.List<UserSimpleResp> recentLikes;

    public static PostDetailResp fromPost(Post post, String nickname, String avatar) {
        PostDetailResp resp = new PostDetailResp();
        BeanUtils.copyProperties(post, resp);
        resp.setUserName(nickname);
        resp.setUserAvatar(avatar);
        return resp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    public java.util.List<UserSimpleResp> getRecentLikes() {
        return recentLikes;
    }

    public void setRecentLikes(java.util.List<UserSimpleResp> recentLikes) {
        this.recentLikes = recentLikes;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }
}
