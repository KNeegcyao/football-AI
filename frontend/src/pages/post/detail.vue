<template>
  <view class="container">
    <!-- Status Bar -->
    <view class="status-bar"></view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-state">
      <text style="color: #fff;">Loading...</text>
    </view>

    <template v-else-if="post">
      <!-- Header -->
      <view class="header">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <text class="material-icons" style="font-size: 24px; color: #fff;">arrow_back_ios_new</text>
          </view>
        </view>
        <view class="header-center">
          <view 
            class="author-info" 
            @click.stop="navigateToProfile(post.userId)"
            hover-class="clickable-hover"
            :hover-stay-time="100"
          >
            <view class="avatar-container">
              <image class="author-avatar" :src="post.userAvatar" mode="aspectFill"></image>
              <view class="verified-badge">
                <text class="material-icons" style="font-size: 6px; color: #fff;">bolt</text>
              </view>
            </view>
            <view class="author-details">
              <view class="author-name-row">
                <text class="author-name">{{ post.userName }}</text>
                <text class="material-icons verified-icon">verified</text>
              </view>
              <text class="author-role">社区成员</text>
            </view>
          </view>
        </view>
        <view class="header-right">
          <button 
            class="follow-btn" 
            :class="{ 'following': isFollowing }"
            @click="toggleFollow"
          >
            {{ isFollowing ? '已关注' : '关注' }}
          </button>
        </view>
      </view>

      <scroll-view scroll-y class="content-scroll" :scroll-into-view="scrollTarget" :scroll-with-animation="true">
        <!-- Article Content -->
        <view class="article">
          <view class="article-meta">
            <text class="tag">{{ categoryText }}</text>
            <text class="time">{{ formatTime(post.createdAt) }}</text>
          </view>
          
          <text class="article-title">{{ post.title }}</text>
          
          <view class="article-body">
            <text class="body-text">{{ post.content }}</text>
          </view>

          <!-- Image Grid -->
          <view class="image-grid" v-if="post.images && post.images.length > 0">
            <view class="grid-item" v-for="(img, index) in post.images" :key="index" @click="previewImage(index)">
              <image class="grid-image" :src="img" mode="aspectFill"></image>
            </view>
          </view>

          <!-- Stats -->
          <view class="interaction-stats">
            <view class="avatar-stack" v-if="post.recentLikes && post.recentLikes.length > 0">
              <image 
                v-for="(liker, index) in post.recentLikes" 
                :key="index" 
                class="stack-avatar" 
                :src="liker.avatar" 
                mode="aspectFill"
                @click.stop="navigateToProfile(liker.id)"
                hover-class="clickable-hover"
                :hover-stay-time="100"
              ></image>
              <view class="stack-count" style="background-color: #f2b90d; color: #000;">
                <text class="material-icons" style="font-size: 12px;">thumb_up</text>
              </view>
            </view>
            <view class="view-stats">
              <text class="stat-highlight">{{ post.views || 0 }}</text> 浏览 • <text class="stat-highlight">{{ post.likes || 0 }}</text> 点赞
            </view>
          </view>
        </view>

        <!-- Comments -->
        <view class="comments-section">
          <view class="comments-header">
            <text class="comments-title">评论 ({{ post.commentCount || comments.length }})</text>
            <view class="sort-btn" @click="toggleSort">
              <text>{{ sortType === 'newest' ? '最新' : '最热' }}</text>
              <text class="material-icons" style="font-size: 14px;">expand_more</text>
            </view>
          </view>

          <view class="comments-list">
            <view class="comment-item" v-for="(comment, index) in comments" :key="index" :id="'comment-' + comment.id" :class="{ 'highlight': comment.id == targetId }">
              <image 
                class="comment-avatar" 
                :src="comment.userAvatar" 
                mode="aspectFill" 
                @click.stop="navigateToProfile(comment.userId)"
                hover-class="clickable-hover"
                :hover-stay-time="100"
              ></image>
              <view class="comment-content-wrapper">
                <view class="comment-bubble">
                  <view class="comment-user-row">
                    <text 
                      class="comment-username" 
                      @click.stop="navigateToProfile(comment.userId)"
                      hover-class="text-hover"
                      :hover-stay-time="100"
                    >{{ comment.userName }}</text>
                    <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
                  </view>
                  <text class="comment-text">{{ comment.content }}</text>
                </view>
                <view class="comment-actions">
                  <view class="action-item">
                    <text class="material-icons" style="font-size: 14px;">favorite_border</text>
                    <text class="action-text">{{ comment.likes || 0 }}</text>
                  </view>
                  <text class="reply-btn" @click="handleReply(comment)">回复</text>
                </view>

                <!-- Nested Replies -->
                <view class="nested-replies" v-if="comment.replies && comment.replies.length > 0">
                  <view class="comment-item nested" v-for="(reply, rIndex) in comment.replies" :key="rIndex" :id="'comment-' + reply.id" :class="{ 'highlight': reply.id == targetId }">
                    <image 
                      class="comment-avatar small" 
                      :src="reply.userAvatar" 
                      mode="aspectFill" 
                      @click.stop="navigateToProfile(reply.userId)"
                      hover-class="clickable-hover"
                      :hover-stay-time="100"
                    ></image>
                    <view class="comment-content-wrapper">
                      <view class="comment-bubble semi-transparent">
                        <view class="comment-user-row">
                          <text 
                            class="comment-username" 
                            @click.stop="navigateToProfile(reply.userId)"
                            hover-class="text-hover"
                            :hover-stay-time="100"
                          >{{ reply.userName }}</text>
                          <text class="comment-time">{{ formatTime(reply.createdAt) }}</text>
                        </view>
                        <text class="comment-text">
                          <text class="reply-label" v-if="reply.replyToUserName">回复 @{{ reply.replyToUserName }}: </text>
                          {{ reply.content }}
                        </text>
                      </view>
                      <view class="comment-actions">
                        <view class="action-item">
                          <text class="material-icons" style="font-size: 12px;">favorite_border</text>
                          <text class="action-text">{{ reply.likes || 0 }}</text>
                        </view>
                        <text class="reply-btn" @click="handleReply(reply)">回复</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
            
            <view v-if="comments.length === 0" style="text-align: center; padding: 20px; color: #666;">
              <text>暂无评论。</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </template>
    
    <view v-else class="error-state">
      <text style="color: #fff;">加载帖子失败。</text>
    </view>

    <!-- Unified Bottom Bar -->
    <view class="bottom-bar-wrapper" v-if="post">
      <view class="reply-info" v-if="replyTarget">
        <text class="reply-to">回复 @{{ replyTarget.userName }}</text>
        <text class="material-icons cancel-reply" @click="cancelReply">close</text>
      </view>
      <view class="bottom-bar">
        <input 
          class="comment-input" 
          :placeholder="replyTarget ? '回复评论...' : '说点什么...'" 
          v-model="commentText"
          :disabled="submitting"
          cursor-spacing="20"
          :focus="!!replyTarget"
        />
        
        <button 
          v-if="commentText.trim()"
          class="send-btn" 
          :disabled="submitting"
          @click="submitComment"
        >
          发送
        </button>
        
        <template v-else>
          <view class="action-btn" @click="handleLike">
            <text class="material-icons" :style="{ color: post.isLiked ? '#f2b90d' : '#fff' }">
              {{ post.isLiked ? 'thumb_up' : 'thumb_up_off_alt' }}
            </text>
            <text class="action-text" :style="{ color: post.isLiked ? '#f2b90d' : '#fff' }">
              {{ post.likes || 0 }}
            </text>
          </view>
          <view class="action-btn" @click="handleFavorite">
            <text class="material-icons" :style="{ color: post.isFavorited ? '#f2b90d' : '#fff' }">
              {{ post.isFavorited ? 'star' : 'star_border' }}
            </text>
          </view>
          <view class="action-btn">
            <text class="material-icons">chat_bubble_outline</text>
            <text class="action-text">{{ post.commentCount || comments.length }}</text>
          </view>
          <view class="action-btn">
            <text class="material-icons">share</text>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { postApi, fileApi, favoriteApi } from '@/api';

const post = ref(null);
const comments = ref([]);
const postId = ref(null);
const loading = ref(true);
const sortType = ref('newest'); // newest, hottest
const isFollowing = ref(false);
const commentText = ref('');
const submitting = ref(false);
const scrollTarget = ref('');
const targetId = ref(null);
const replyTarget = ref(null); // { id, userName }

const categoryText = computed(() => {
  if (!post.value) return '# 足球';
  if (post.value.topicName) return `# ${post.value.topicName}`;
  if (post.value.circleName) return `# ${post.value.circleName}`;
  return post.value.category || '# 足球';
});

const goBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    // 如果没有上一页（如分享卡片进入或发布跳转导致栈替换），回到社区首页
    uni.switchTab({
      url: '/pages/community/community'
    });
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + 'm ago';
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + 'h ago';
  } else {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
};

const loadPostDetail = async (id) => {
  try {
    loading.value = true;
    const data = await postApi.getDetail(id);
    if (data) {
      // Parse images if needed
      let images = [];
      if (data.images) {
        try {
            images = typeof data.images === 'string' ? JSON.parse(data.images) : data.images;
        } catch (e) {
            console.error('Failed to parse images JSON:', e);
            images = [];
        }
      }
      
      // Process image URLs
      const processedImages = images.map(img => fileApi.getFileUrl(img));
      
      // Process avatar URL
      const avatarUrl = data.userAvatar ? fileApi.getFileUrl(data.userAvatar) : '/static/soccer-logo.png';

      // Process recent likes avatars
      const processedRecentLikes = (data.recentLikes || []).map(liker => ({
        ...liker,
        avatar: liker.avatar ? fileApi.getFileUrl(liker.avatar) : '/static/soccer-logo.png'
      }));

      post.value = {
        ...data,
        userName: data.userName || 'Unknown User',
        userAvatar: avatarUrl,
        images: processedImages,
        recentLikes: processedRecentLikes,
        views: data.views || 0,
        likes: data.likes || 0,
        isLiked: data.isLiked || false,
        isFavorited: data.isFavorited || false
      };
      
      // Check follow status
      if (post.value.userId) {
        try {
          const res = await postApi.checkFollowStatus(post.value.userId);
          isFollowing.value = res;
        } catch (e) {
          console.error('Failed to check follow status:', e);
        }
      }
    }
  } catch (error) {
    console.error('Failed to load post detail:', error);
  } finally {
    loading.value = false;
  }
};

const loadComments = async (id) => {
  try {
    const data = await postApi.getComments(id, { 
      page: 1, 
      size: 20,
      sort: sortType.value 
    });
    if (data) {
      const records = data.records || [];
      comments.value = records.map(c => ({
        ...c,
        userName: c.nickname || 'Unknown User',
        userAvatar: c.avatar ? fileApi.getFileUrl(c.avatar) : '/static/soccer-logo.png',
        likes: c.likes || 0,
        // Map replies recursively if needed, but for now flat list or simple structure
        replies: (c.replies || []).map(r => ({
            ...r,
            userName: r.nickname || 'Unknown User',
            userAvatar: r.avatar ? fileApi.getFileUrl(r.avatar) : '/static/soccer-logo.png',
            likes: r.likes || 0,
            replyToUserName: r.replyToNickname // 映射后端字段
        }))
      }));

      // 如果有目标评论ID，尝试滚动到该位置
      if (targetId.value) {
        nextTick(() => {
          setTimeout(() => {
            scrollTarget.value = 'comment-' + targetId.value;
            console.log('Scrolling to target:', scrollTarget.value);
            
            // 3秒后移除高亮
            setTimeout(() => {
              targetId.value = null;
            }, 3000);
          }, 500);
        });
      }
    }
  } catch (error) {
    console.error('Failed to load comments:', error);
  }
};

const toggleFollow = async () => {
  if (!post.value || !post.value.userId) return;
  
  try {
    if (isFollowing.value) {
      await postApi.unfollowUser(post.value.userId);
      isFollowing.value = false;
      uni.showToast({ title: '已取消关注', icon: 'none' });
    } else {
      await postApi.followUser(post.value.userId);
      isFollowing.value = true;
      uni.showToast({ title: '已关注', icon: 'success' });
    }
  } catch (error) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const toggleSort = () => {
  sortType.value = sortType.value === 'newest' ? 'hottest' : 'newest';
  loadComments(postId.value);
};

const handleReply = (comment) => {
  replyTarget.value = {
    id: comment.id,
    userName: comment.userName
  };
  // 滚动到底部或聚焦输入框
  nextTick(() => {
    // 聚焦逻辑通常在 uniapp 中通过 focus 属性实现
  });
};

const cancelReply = () => {
  replyTarget.value = null;
};

const submitComment = async () => {
  if (!commentText.value.trim()) {
    return;
  }
  
  submitting.value = true;
  try {
    const params = {
      postId: postId.value,
      content: commentText.value
    };
    
    // 如果是回复某条评论
    if (replyTarget.value) {
      params.parentId = replyTarget.value.id;
    }
    
    await postApi.createComment(params);
    
    const isReply = !!replyTarget.value;
    commentText.value = '';
    replyTarget.value = null; // 清除回复目标
    uni.showToast({ title: isReply ? '回复成功' : '评论成功', icon: 'success' });
    
    // Reload comments and update count
    await loadComments(postId.value);
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1;
    }
  } catch (error) {
    uni.showToast({ title: '评论失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

const handleLike = async () => {
  if (!post.value) return;
  try {
    const res = await postApi.like({
      targetId: post.value.id,
      targetType: 1 // 1 for post
    });
    
    if (res && res.liked !== undefined) {
      post.value.isLiked = res.liked;
      if (res.liked) {
        post.value.likes++;
        uni.showToast({ title: '已点赞', icon: 'none' });
      } else {
        post.value.likes--;
        uni.showToast({ title: '已取消', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Like failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const handleFavorite = async () => {
  if (!post.value) return;
  try {
    const res = await favoriteApi.toggle({
      postId: post.value.id
    });
    
    if (res && res.favorited !== undefined) {
      post.value.isFavorited = res.favorited;
      if (res.favorited) {
        uni.showToast({ title: '已收藏', icon: 'success' });
      } else {
        uni.showToast({ title: '已取消收藏', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Favorite failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const previewImage = (index) => {
  if (post.value && post.value.images) {
    uni.previewImage({
      urls: post.value.images,
      current: index
    });
  }
};

const navigateToProfile = (userId) => {
  console.log('Attempting to navigate to profile for userId:', userId);
  if (!userId) {
    console.warn('Cannot navigate: userId is missing');
    return;
  }
  
  const userInfo = uni.getStorageSync('userInfo');
  const currentUserId = userInfo?.id;
  console.log('Current logged-in userId:', currentUserId);
  
  // 如果是当前用户，使用 switchTab 跳转到 TabBar 的“我的”页面
  if (userId == currentUserId) {
    console.log('Navigating to own profile (TabBar)');
    uni.switchTab({
      url: '/pages/my/my',
      success: () => console.log('Successfully switched to own profile tab'),
      fail: (err) => console.error('Failed to switch to own profile tab:', err)
    });
  } else {
    // 如果是他人，使用 navigateTo 跳转到专门的他人主页
    const url = `/pages/my/user-profile?userId=${userId}`;
    console.log('Navigating to others profile:', url);
    uni.navigateTo({
      url: url,
      success: () => console.log('Successfully navigated to user-profile'),
      fail: (err) => {
        console.error('Failed to navigate to user-profile:', err);
        uni.showToast({ title: '跳转失败', icon: 'none' });
      }
    });
  }
};

onLoad((options) => {
  if (options.id) {
    postId.value = options.id;
    if (options.targetId) {
        targetId.value = options.targetId;
    }
    loadPostDetail(options.id);
    loadComments(options.id);
  }
});
</script>

<style scoped>
/* Colors */
.container {
  background-color: #0A0A0A;
  min-height: 100vh;
  color: #fff;
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
  background-color: rgba(248, 245, 246, 0.8);
}

.header {
  position: sticky;
  top: 0;
  z-index: 50;
  background-color: rgba(10, 10, 10, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(242, 185, 13, 0.1);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  width: 40px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  overflow: hidden;
  padding: 4px 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.back-btn {
  padding: 4px;
  margin-left: -4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.avatar-container {
  position: relative;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(242, 185, 13, 0.2);
}

.verified-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background-color: #f2b90d;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #0A0A0A;
  display: flex;
  align-items: center;
  justify-content: center;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
}

.verified-icon {
  color: #f2b90d;
  font-size: 14px;
}

.author-role {
  font-size: 10px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.follow-btn {
  background-color: #f2b90d;
  color: #000;
  font-size: 12px;
  font-weight: bold;
  padding: 6px 16px;
  border-radius: 9999px;
  margin: 0;
  line-height: 1.5;
}

.follow-btn.following {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* Article */
.article {
  padding: 16px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  color: #f2b90d;
  font-size: 12px;
  font-weight: 600;
  background-color: rgba(242, 185, 13, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.time {
  font-size: 12px;
  color: #64748b;
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  line-height: 1.25;
  margin-bottom: 12px;
  color: #fff;
  display: block;
}

.article-body {
  margin-bottom: 16px;
}

.body-text {
  font-size: 14px;
  color: #cbd5e1;
  line-height: 1.625;
}

.read-more {
  color: #f2b90d;
  font-weight: 500;
  font-size: 14px;
  margin-left: 4px;
}

/* Image Grid */
.image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.grid-item {
  aspect-ratio: 1;
  background-color: #1A1A1A;
  overflow: hidden;
  position: relative;
}

.grid-image {
  width: 100%;
  height: 100%;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-text {
  color: #fff;
  font-weight: bold;
  font-size: 18px;
}

/* Stats */
.interaction-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid rgba(30, 41, 59, 0.5);
  border-bottom: 1px solid rgba(30, 41, 59, 0.5);
}

.avatar-stack {
  display: flex;
  margin-left: 8px;
}

.stack-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #0A0A0A;
  margin-left: -8px;
}

.stack-count {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #1e293b;
  border: 2px solid #0A0A0A;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
  color: #94a3b8;
  margin-left: -8px;
}

.view-stats {
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: -0.05em;
}

.stat-highlight {
  color: #cbd5e1;
  font-weight: bold;
}

/* Comments */
.comments-section {
  background-color: #0A0A0A;
  margin-top: 8px;
}

.comments-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comments-title {
  font-weight: bold;
  font-size: 14px;
  color: #fff;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.comments-list {
  padding: 0 16px 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.5s ease;
}

.comment-item.highlight {
  background-color: rgba(242, 185, 13, 0.15);
  transform: scale(1.02);
}

.comment-item.nested {
  margin-top: 16px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.comment-avatar.small {
  width: 28px;
  height: 28px;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-bubble {
  background-color: #1A1A1A;
  padding: 12px;
  border-radius: 12px;
  border-top-left-radius: 0;
}

.comment-bubble.semi-transparent {
  background-color: rgba(26, 26, 26, 0.5);
  padding: 10px;
}

.comment-user-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.comment-username {
  font-size: 12px;
  font-weight: bold;
  color: #e2e8f0;
}

.comment-time {
  font-size: 10px;
  color: #64748b;
}

.comment-text {
  font-size: 14px;
  color: #cbd5e1;
}

.highlight-text {
  color: #f2b90d;
}

.reply-label {
  color: #f2b90d;
  font-size: 13px;
  margin-right: 4px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
  margin-left: 4px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.action-item.active {
  color: #f2b90d;
}

.action-text {
  font-size: 10px;
  font-weight: bold;
}

.reply-btn {
  font-size: 10px;
  font-weight: bold;
  color: #64748b;
}

.nested-replies {
  margin-top: 16px;
  border-left: 2px solid #1e293b;
  padding-left: 16px;
}

.bottom-bar-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  /* #ifdef H5 */
  max-width: 500px;
  left: 50% !important;
  transform: translateX(-50%);
  width: 100%;
  /* #endif */
}

.reply-info {
  background-color: #1A1A1A;
  padding: 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.reply-to {
  font-size: 12px;
  color: #f2b90d;
  font-weight: 500;
}

.cancel-reply {
  font-size: 16px;
  color: #64748b;
  padding: 4px;
}

.bottom-bar {
  height: 60px;
  background-color: rgba(10, 10, 10, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  padding-bottom: env(safe-area-inset-bottom);
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-text {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}

.comment-input {
  flex: 1;
  background-color: #2C2C2C;
  border-radius: 20px;
  padding: 8px 16px;
  color: #fff;
  font-size: 14px;
  height: 36px;
}

.send-btn {
  background-color: #f2b90d;
  color: #000;
  font-size: 14px;
  font-weight: bold;
  padding: 0 16px;
  height: 36px;
  line-height: 36px;
  border-radius: 18px;
  margin: 0;
  min-width: 60px;
  text-align: center;
}

.send-btn[disabled] {
  opacity: 0.5;
  background-color: #666;
}

/* Add padding to scroll-view to avoid overlap with bottom bar */
.content-scroll {
  height: calc(100vh - 80px - 60px); /* Adjust based on header and bottom bar */
}

.highlight {
  background-color: rgba(242, 185, 13, 0.15);
  border: 1px solid rgba(242, 185, 13, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.clickable-hover {
  opacity: 0.7;
  transform: scale(0.98);
  transition: all 0.1s;
}

.text-hover {
  opacity: 0.6;
  text-decoration: underline;
}
</style>
