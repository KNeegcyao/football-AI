<template>
  <view class="container">
    <!-- Header Image & Overlay -->
    <view class="header-image-container">
      <image 
        class="header-image" 
        src="https://lh3.googleusercontent.com/aida-public/AB6AXuAwsjDBXPF_ZBUL_MNJrjMqbuEppi7q-YRE5SIrTWkDszyCYpHhZnTo4BIH-Rm0bNhS8QEf4Tj52HstK0eLc6Pew4yjFKFmrCf275HGSCQTWOnLQ1ovhjAr1cusTYseYC3GUoQFm857MTCN0YbH6NGP4KMvWwjKCAV4rTuvE7rznuVX5iiQMrYTXWpVN4IHLgloD594kIBxJRZ5euXJWnOKdCWbxsnL2XWmFLUK_YRJHO4VqwOfsZ2rXV0HQie-d_FKkjbuodaH-zWe" 
        mode="aspectFill"
      ></image>
      <view class="header-overlay"></view>
      
      <!-- Top Navigation Bar -->
      <view class="nav-bar">
        <view class="icon-btn" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="24"></u-icon>
        </view>
        <view class="nav-actions">
          <view class="icon-btn">
            <u-icon name="search" color="#fff" size="24"></u-icon>
          </view>
          <view class="icon-btn">
            <u-icon name="share" color="#fff" size="24"></u-icon>
          </view>
        </view>
      </view>

      <!-- Topic Title -->
      <view class="topic-info">
        <view class="topic-badge-row">
          <view class="badge">Trending</view>
          <view class="ai-label">
            <u-icon name="star-fill" color="#fff" size="14"></u-icon>
            <text class="ai-text">PitchPulse AI 热门话题</text>
          </view>
        </view>
        <text class="topic-title">#{{ topicTitle }}#</text>
      </view>
    </view>

    <!-- Stats & Description -->
    <view class="content-body">
      <view class="stats-row">
        <view class="stat-card">
          <text class="stat-label">讨论</text>
          <text class="stat-value">1.2w</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">阅读</text>
          <text class="stat-value">450w</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">预测</text>
          <text class="stat-value highlight">89%</text>
        </view>
      </view>

      <view class="description-box">
        <text class="description-text">
          梅西率领阿根廷国家队开启2024中国行，在北京与摩洛哥队进行友谊赛。这是球王职业生涯第八次来到中国，全城球迷共迎盛事。
        </text>
        <view class="expand-btn">
          <text>查看完整背景</text>
          <u-icon name="arrow-down" color="#f20d33" size="12"></u-icon>
        </view>
      </view>
    </view>

    <!-- Feed Tabs -->
    <view class="tabs-header sticky-header">
      <view class="tab-item active">最热</view>
      <view class="tab-item">最新</view>
      <view class="tab-item">媒体</view>
    </view>

    <!-- Discussion Feed -->
    <view class="feed-list">
      <!-- Post Card 1 (Static Featured) -->
      <view class="post-card">
        <view class="post-header">
          <view class="user-info">
            <view class="avatar-wrapper">
              <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAz587xyU9hdDY5pWPzvhmpbGa8y3P16Qjy-98NjD1s3dwVMidPBhA4O7vGD1z7cvy0V2InSSjLs1ej2k9hjIdC7Ead4njJCVCAqzH0sqDBt9Z9p52AN6THPv46e86A9NwDGMXpBnQ5G5ZxUwABbItjr0v7Lfh7X02oU0Y4ZYShG2GX57DuGIuPTnh9mAq4C4tfmnUKD0qa8dGnJpgq52cxfrZoRLSEIX0gyZGUH6OGvXbIUR68pyf7LOJPepWObEFlylehjyj3NK06" mode="aspectFill"></image>
            </view>
            <view class="user-meta">
              <view class="name-row">
                <text class="username">阿根廷死忠张伟</text>
                <u-icon name="checkmark-circle-fill" color="#f20d33" size="14"></u-icon>
              </view>
              <text class="time">2小时前 · 北京</text>
            </view>
          </view>
          <u-icon name="more-dot-fill" color="rgba(255,255,255,0.4)" size="20"></u-icon>
        </view>
        <text class="post-content">
          梅西的首秀太震撼了！全场齐声高呼名号的那一刻，我甚至觉得这就是工体的巅峰时刻。阿根廷的中场控制力依然是顶级。 <text class="hashtag">#梅西中国行#</text>
        </text>
        <view class="media-container">
          <image class="post-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCPbX5Nw38MvcbPLK4SryvZ9GOrEGCfo4CaLg9j-oiJiBN3gIhp6kTbitTcuxB1_mk-E5GiYD799QTLvzHlQVd30AmhRJApes4YrDv1x6nl8Bp-7bMw7NA88Kma8Wk-nNNpXi9G7Ci7PPtGd1AzhJcjgKbs-HPmC4dc4bBBW32jUakQ0SY-iLwZ5gChwY5ZappipQJ0ovRQG8mNvetrUoXbqPowrpUXs444lNACOYNTHIkcAmuqhvOl1VJ4chMVmC4HUsavdjx8iX7T" mode="aspectFill"></image>
        </view>
        <view class="post-actions">
          <view class="action-group">
            <view class="action-btn">
              <u-icon name="heart" color="rgba(255,255,255,0.6)" size="20"></u-icon>
              <text class="count">1,245</text>
            </view>
            <view class="action-btn">
              <u-icon name="chat" color="rgba(255,255,255,0.6)" size="20"></u-icon>
              <text class="count">89</text>
            </view>
          </view>
          <view class="action-btn">
            <u-icon name="hourglass" color="rgba(255,255,255,0.6)" size="20"></u-icon>
            <text class="count">AI 分析</text>
          </view>
        </view>
      </view>

      <!-- Post Card 2 (AI Analysis - Static) -->
      <view class="post-card ai-card">
        <view class="ai-bg-icon">
          <u-icon name="info-circle" color="rgba(255,255,255,0.1)" size="60"></u-icon>
        </view>
        <view class="post-header">
          <view class="user-info">
            <view class="avatar-wrapper ai-avatar">
              <u-icon name="star-fill" color="#fff" size="20"></u-icon>
            </view>
            <view class="user-meta">
              <view class="name-row">
                <text class="username">PitchPulse AI 分析师</text>
                <view class="pro-badge">PRO</view>
              </view>
              <text class="time">战术实时同步</text>
            </view>
          </view>
        </view>
        <view class="ai-content-box">
          <text class="ai-title">实时热点摘要</text>
          <text class="ai-text">大部分用户正在讨论上半场第32分钟梅西的过人动作。该区域社交讨论热度上升 400%。</text>
        </view>
        <view class="ai-footer">
          <text class="ai-footer-text">AI Insights</text>
          <view class="subscribe-btn">订阅分析报</view>
        </view>
      </view>

      <!-- Real User Post Cards -->
      <view class="post-card" v-for="(post, index) in postList" :key="post.id || index">
        <view class="post-header">
          <view class="user-info">
            <view class="avatar-wrapper">
              <image class="avatar" :src="post.userAvatar || 'https://lh3.googleusercontent.com/aida-public/AB6AXuAW-tRnOKF1UnPpHhxy2flLtbiO8R0HTwS4FeLc56nZBxXLJNcsc-Bo2LkZVB_ap4G03ZX_jHWsmLN9BDVzvD6I2xxi8-_89Aefzqj3EMb6PWRvo-ysi4ExowK2wTcCG-yqpJbTMq8USzEu0eUos6rPE6UGMrR75P1oyqtX6hu6YbCafpnwyZCaXMH-d2hIxz29kk9n1KYH6glR56zVQ3EvZoFaoDtwsKzMcIQzwWPpWUXDlAepvgvskvu4DnCQ-v-1rzD0_2h6BGEr'" mode="aspectFill"></image>
            </view>
            <view class="user-meta">
              <view class="name-row">
                <text class="username">{{ post.userName || '球迷用户' }}</text>
              </view>
              <text class="time">{{ formatTime(post.createTime) }}</text>
            </view>
          </view>
          <u-icon name="more-dot-fill" color="rgba(255,255,255,0.4)" size="20"></u-icon>
        </view>
        <text class="post-content">{{ post.content }}</text>
        
        <!-- Post Images -->
        <view class="media-container" v-if="post.images && post.images.length > 0">
          <image 
            class="post-image" 
            v-for="(img, i) in post.images" 
            :key="i" 
            :src="img" 
            mode="aspectFill"
            @click.stop="previewImage(post.images, i)"
          ></image>
        </view>
        
        <view class="post-actions">
          <view class="action-group">
            <view class="action-btn">
              <u-icon name="heart" color="rgba(255,255,255,0.6)" size="20"></u-icon>
              <text class="count">{{ post.likeCount || 0 }}</text>
            </view>
            <view class="action-btn">
              <u-icon name="chat" color="rgba(255,255,255,0.6)" size="20"></u-icon>
              <text class="count">{{ post.commentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-if="postList.length === 0" class="empty-state">
        <text>暂无更多话题讨论</text>
      </view>
    </view>

    <!-- Floating Action Button -->
    <view class="fab-container">
      <button class="fab-btn">
        <u-icon name="plus" color="#fff" size="20"></u-icon>
        <text class="fab-text">参与讨论</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { communityApi, fileApi } from '@/api';

const topicTitle = ref('梅西中国行');
const topicInfo = ref({});
const loading = ref(false);
const postList = ref([]);

const goBack = () => {
  uni.navigateBack();
};

onLoad((options) => {
  if (options.title) {
    topicTitle.value = decodeURIComponent(options.title).replace(/^#|#$/g, '');
    loadPosts(topicTitle.value);
  }
});

const loadTopicDetail = async (id) => {
  try {
    const data = await communityApi.getTopicDetail(id)
    if (data) {
      topicInfo.value = {
        ...data,
        description: data.description || '这里是话题导语，引导大家参与讨论...',
        viewCount: data.viewCount || Math.floor(Math.random() * 10000),
        discussCount: data.discussCount || Math.floor(Math.random() * 1000)
      }
    }
  } catch (error) {
    console.error('获取话题详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const loadPosts = async (id) => {
  try {
    loading.value = true
    const data = await communityApi.getTopicPosts(id, { page: 1, size: 10 })
    if (data) {
      const records = data.records || []
      postList.value = records.map(post => {
        let postImages = [];
        try {
          if (post.images) {
            if (Array.isArray(post.images)) {
                postImages = post.images;
            } else if (typeof post.images === 'string') {
                postImages = JSON.parse(post.images);
            }
          }
        } catch (e) {
          console.error('解析图片失败:', e);
        }

        return {
          ...post,
          userName: post.userName || '未知用户',
          userAvatar: post.userAvatar ? fileApi.getFileUrl(post.userAvatar) : '/static/default-avatar.png',
          images: postImages.map(img => fileApi.getFileUrl(img)),
          likeCount: post.likeCount || 0,
          commentCount: post.commentCount || 0,
          createTime: post.createdAt || post.createTime
        }
      })
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

const getRandomAvatar = () => {
  const avatars = [
    'https://lh3.googleusercontent.com/aida-public/AB6AXuBrRbCMQ9jttw9cIS5KvBEH7v21rrGegUNbE_x3qMLLvCDd206ZrgoV8t35q1ze3OwpjV-cPZXyCIm1TqRCFeUOqs4mCXUC8F-oTUefRjWEwQdBJh86UE363VP2GDt0LgnG9bc3NJ1lyO-GADQ-onflDElLt02WLLahKrxTJxmrFSuuOf6LG7R6XHaLY60uxTjZFYPkVB-E_FheufCuakN_sq3efmkaIRAfs5J0NBc-sUlCQbnc9UeZwKWUiPMKa0rhKyOrgOk6HnXc',
    'https://lh3.googleusercontent.com/aida-public/AB6AXuAz587xyU9hdDY5pWPzvhmpbGa8y3P16Qjy-98NjD1s3dwVMidPBhA4O7vGD1z7cvy0V2InSSjLs1ej2k9hjIdC7Ead4njJCVCAqzH0sqDBt9Z9p52AN6THPv46e86A9NwDGMXpBnQ5G5ZxUwABbItjr0v7Lfh7X02oU0Y4ZYShG2GX57DuGIuPTnh9mAq4C4tfmnUKD0qa8dGnJpgq52cxfrZoRLSEIX0gyZGUH6OGvXbIUR68pyf7LOJPepWObEFlylehjyj3NK06',
    'https://lh3.googleusercontent.com/aida-public/AB6AXuAW-tRnOKF1UnPpHhxy2flLtbiO8R0HTwS4FeLc56nZBxXLJNcsc-Bo2LkZVB_ap4G03ZX_jHWsmLN9BDVzvD6I2xxi8-_89Aefzqj3EMb6PWRvo-ysi4ExowK2wTcCG-yqpJbTMq8USzEu0eUos6rPE6UGMrR75P1oyqtX6hu6YbCafpnwyZCaXMH-d2hIxz29kk9n1KYH6glR56zVQ3EvZoFaoDtwsKzMcIQzwWPpWUXDlAepvgvskvu4DnCQ-v-1rzD0_2h6BGEr'
  ];
  return avatars[Math.floor(Math.random() * avatars.length)];
};

const getRandomName = () => {
  const names = ['伯纳乌之王', '梅西死忠', 'C罗粉丝', '曼联红魔', '阿森纳枪手', '利物浦KOP', '拜仁慕尼黑', '尤文蒂尼'];
  return names[Math.floor(Math.random() * names.length)] + Math.floor(Math.random() * 1000);
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
};

const previewImage = (images, current) => {
  uni.previewImage({
    urls: images,
    current: current
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #0a0a0a;
  color: #fff;
  padding-bottom: 100rpx;
}

.header-image-container {
  position: relative;
  height: 500rpx;
  width: 100%;
}

.header-image {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.header-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(10,10,10,0.4), rgba(10,10,10,1));
}

.nav-bar {
  position: absolute;
  top: 0; /* Will be adjusted by safe-area-inset-top in logic if needed, but fixed here */
  padding-top: var(--status-bar-height); /* Safe area padding */
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 32rpx;
  padding-right: 32rpx;
  height: 88rpx; /* Standard nav height */
  z-index: 20;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-actions {
  display: flex;
  gap: 16rpx;
}

.topic-info {
  position: absolute;
  bottom: 32rpx;
  left: 32rpx;
  right: 32rpx;
}

.topic-badge-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.badge {
  background-color: #f20d33;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: bold;
  text-transform: uppercase;
  color: #fff;
}

.ai-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.ai-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

.topic-title {
  font-size: 48rpx;
  font-weight: 900;
  font-style: italic;
  letter-spacing: -1px;
  color: #fff;
}

.content-body {
  padding: 32rpx;
}

.stats-row {
  display: flex;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.stat-card {
  flex: 1;
  background-color: #1a1a1a;
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.stat-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.5);
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 900;
  color: #fff;
}

.stat-value.highlight {
  color: #f20d33;
}

.description-box {
  background-color: rgba(242, 13, 51, 0.1);
  border-left: 8rpx solid #f20d33;
  padding: 24rpx;
  border-top-right-radius: 16rpx;
  border-bottom-right-radius: 16rpx;
  margin-bottom: 32rpx;
}

.description-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.expand-btn {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #f20d33;
  font-weight: bold;
}

.tabs-header {
  position: sticky;
  top: 0; /* Needs adjustment if header is fixed, but here it flows */
  z-index: 10;
  background-color: rgba(10, 10, 10, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
  display: flex;
  padding: 0 32rpx;
}

.tab-item {
  padding: 32rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.4);
  position: relative;
}

.tab-item.active {
  color: #fff;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 32rpx;
  right: 32rpx;
  height: 4rpx;
  background-color: #f20d33;
}

.feed-list {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.post-card {
  background-color: #1a1a1a;
  border-radius: 24rpx;
  padding: 32rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.3);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid rgba(242, 13, 51, 0.3);
  background-color: #333;
}

.avatar {
  width: 100%;
  height: 100%;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.username {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

.time {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
}

.post-content {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  margin-bottom: 24rpx;
  display: block;
}

.hashtag {
  color: #f20d33;
  font-weight: 500;
  margin-left: 8rpx;
}

.media-container {
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 32rpx;
  height: 400rpx;
  background-color: rgba(0, 0, 0, 0.4);
}

.post-image {
  width: 100%;
  height: 100%;
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.05);
}

.action-group {
  display: flex;
  gap: 32rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.count {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

/* AI Card Styles */
.ai-card {
  position: relative;
  overflow: hidden;
}

.ai-bg-icon {
  position: absolute;
  top: 0;
  right: 0;
  padding: 16rpx;
  opacity: 0.1;
  pointer-events: none;
}

.ai-avatar {
  background-color: #f20d33;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.pro-badge {
  background-color: rgba(242, 13, 51, 0.2);
  color: #f20d33;
  font-size: 16rpx;
  font-weight: 900;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.ai-content-box {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 16rpx;
  padding: 24rpx;
  border: 1rpx solid rgba(242, 13, 51, 0.2);
  margin-bottom: 24rpx;
}

.ai-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #f20d33;
  margin-bottom: 8rpx;
  text-transform: uppercase;
  letter-spacing: 2px;
  display: block;
}

.ai-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.ai-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-footer-text {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.3);
  text-transform: uppercase;
  font-weight: 900;
  font-style: italic;
}

.subscribe-btn {
  font-size: 20rpx;
  background-color: rgba(255, 255, 255, 0.05);
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  color: rgba(255, 255, 255, 0.6);
}

/* FAB */
.fab-container {
  position: fixed;
  bottom: 48rpx;
  left: 0;
  right: 0;
  /* #ifdef H5 */
  max-width: 500px;
  margin: 0 auto;
  /* #endif */
  display: flex;
  justify-content: center;
  z-index: 50;
  pointer-events: none; /* Container passes clicks */
}

.fab-btn {
  pointer-events: auto;
  background-color: #f20d33;
  color: #fff;
  padding: 0 48rpx;
  height: 96rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 16rpx 60rpx rgba(242, 13, 51, 0.4);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  /* Reset button default styles */
  margin: 0;
  line-height: 1;
}

.fab-btn:active {
  transform: scale(0.95);
}

.fab-text {
  font-size: 28rpx;
  font-weight: 900;
  letter-spacing: 2px;
}
</style>