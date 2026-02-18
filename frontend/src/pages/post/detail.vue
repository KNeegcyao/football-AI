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
          <view class="author-info">
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
              <text class="author-role">Community Member</text>
            </view>
          </view>
        </view>
        <button class="follow-btn">Follow</button>
      </view>

      <scroll-view scroll-y class="content-scroll">
        <!-- Article Content -->
        <view class="article">
          <view class="article-meta">
            <text class="tag"># Football</text>
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
            <view class="avatar-stack">
              <image class="stack-avatar" :src="post.userAvatar"></image>
              <view class="stack-count" style="background-color: #f2b90d; color: #000;">
                <text class="material-icons" style="font-size: 12px;">thumb_up</text>
              </view>
            </view>
            <view class="view-stats">
              <text class="stat-highlight">{{ post.views || 0 }}</text> Views • <text class="stat-highlight">{{ post.likes || 0 }}</text> Likes
            </view>
          </view>
        </view>

        <!-- Comments -->
        <view class="comments-section">
          <view class="comments-header">
            <text class="comments-title">Comments ({{ post.commentCount || comments.length }})</text>
            <view class="sort-btn">
              <text>Newest</text>
              <text class="material-icons" style="font-size: 14px;">expand_more</text>
            </view>
          </view>

          <view class="comments-list">
            <view class="comment-item" v-for="(comment, index) in comments" :key="index">
              <image class="comment-avatar" :src="comment.userAvatar" mode="aspectFill"></image>
              <view class="comment-content-wrapper">
                <view class="comment-bubble">
                  <view class="comment-user-row">
                    <text class="comment-username">{{ comment.userName }}</text>
                    <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
                  </view>
                  <text class="comment-text">{{ comment.content }}</text>
                </view>
                <view class="comment-actions">
                  <view class="action-item">
                    <text class="material-icons" style="font-size: 14px;">favorite_border</text>
                    <text class="action-text">{{ comment.likes || 0 }}</text>
                  </view>
                  <text class="reply-btn">Reply</text>
                </view>
              </view>
            </view>
            
            <view v-if="comments.length === 0" style="text-align: center; padding: 20px; color: #666;">
              <text>No comments yet.</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </template>
    
    <view v-else class="error-state">
      <text style="color: #fff;">Failed to load post.</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { postApi } from '@/api';

const post = ref(null);
const comments = ref([]);
const postId = ref(null);
const loading = ref(true);

const goBack = () => {
  uni.navigateBack();
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

const getRandomAvatar = () => {
  const avatars = [
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Zack',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Bella',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo'
  ];
  return avatars[Math.floor(Math.random() * avatars.length)];
};

const getRandomName = () => {
  const names = ['Fan_007', 'SoccerLover', 'GoalMachine', 'MidfieldMaestro', 'Keeper1'];
  return names[Math.floor(Math.random() * names.length)];
};

const loadPostDetail = async (id) => {
  try {
    loading.value = true;
    const data = await postApi.getDetail(id);
    if (data) {
      // Parse images if needed
      let images = [];
      if (data.images) {
        images = typeof data.images === 'string' ? JSON.parse(data.images) : data.images;
      } else {
        // Mock images if none provided for better UI experience
        // images = [
        //   'https://lh3.googleusercontent.com/aida-public/AB6AXuBU26CqlmforlJF_IccguOr0tQXy2qXyVmdIap6Ehv3s6rOzM4dHZSGoLbe0mmGuNNdCRxDVbxlFoUIEN9HlXBhLoc6Mo4bqmJmksZ6OzW6IujCTR1VsSlM0WK6ULUP38Vina2mJ2O9iX8qVmRUS29GBge_eEYo9EkAebRy2OAmkOIjk7sCxuW623dXScmOyl0DAvF-6Zww-6I7NBjM646ekQMDBGI2Ps0u2Ph9ARHXuV9WAxHif7lwGjrZhhOiUJM6PMbWIQiPQVs5',
        //   'https://lh3.googleusercontent.com/aida-public/AB6AXuAfVhxd9UX1WqDsvs1HwgDbCGu5M41hjWe7UHY8sbkULMVCMop-K-w-4Zc2huAXOGR6xX6X_sqpiDFNUtXOeyJV2LouCPdS9yIN0nca_quoKCKb32nx6VC__ZbhOL0qaMhysMl0CcHPUbjDuRUnBDA-ogoiKu_hXdCe5FIHiTTp1vN5DFuNeetIEVSYCNGddknExGB7EesozGS7JZ46Bu9o39GgzKIYOdOZ4LrW7dXwlaYscBWDxCHXR7SczLUt0e9auhGBgKLnHjrW'
        // ];
      }

      post.value = {
        ...data,
        userName: data.userName || getRandomName(),
        userAvatar: data.userAvatar || getRandomAvatar(),
        images: images,
        views: data.views || Math.floor(Math.random() * 10000) + 1000,
        likes: data.likes || Math.floor(Math.random() * 500)
      };
    }
  } catch (error) {
    console.error('Failed to load post detail:', error);
  } finally {
    loading.value = false;
  }
};

const loadComments = async (id) => {
  try {
    const data = await postApi.getComments(id, { page: 1, size: 20 });
    if (data) {
      const records = data.records || [];
      comments.value = records.map(c => ({
        ...c,
        userName: c.userName || getRandomName(),
        userAvatar: c.userAvatar || getRandomAvatar(),
        likes: Math.floor(Math.random() * 50)
      }));
    }
  } catch (error) {
    console.error('Failed to load comments:', error);
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

onLoad((options) => {
  if (options.id) {
    postId.value = options.id;
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
  gap: 12px;
}

.back-btn {
  padding: 4px;
  margin-left: -4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
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
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  padding: 6px 16px;
  border-radius: 9999px;
  margin: 0;
  line-height: 1.5;
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
</style>