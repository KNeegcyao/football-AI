<template>
  <view class="container">
    <!-- Navigation Bar -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="24"></u-icon>
      </view>
      <text class="nav-title">我的收藏</text>
      <view class="nav-right"></view>
    </view>

    <!-- Content Area -->
    <scroll-view scroll-y class="content-scroll" @scrolltolower="loadMore">
      <view class="post-list" v-if="posts.length > 0">
        <view v-for="(post, index) in posts" :key="index" class="post-item" @click="goToDetail(post.id)">
          <view class="post-main">
            <view class="post-img-box" v-if="post.image">
              <image class="post-img" :src="post.image" mode="aspectFill"></image>
            </view>
            <view class="post-info">
              <text class="post-title">{{ post.title }}</text>
              <view class="post-footer">
                <view class="post-author-info">
                  <image class="post-author-avatar" :src="post.userAvatar || '/static/soccer-logo.png'" mode="aspectFill"></image>
                  <text class="post-author-name">{{ post.userName }}</text>
                </view>
                <view class="post-stats">
                  <text class="post-time">{{ formatTime(post.createTime) }}</text>
                  <view class="stat-item">
                    <u-icon name="thumb-up" color="#f9d406" size="28rpx"></u-icon>
                    <text class="stat-num">{{ post.likes || 0 }}</text>
                  </view>
                  <view class="stat-item">
                    <u-icon name="chat" color="#f9d406" size="28rpx"></u-icon>
                    <text class="stat-num">{{ post.commentCount || 0 }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
        <u-loadmore :status="loadStatus" lineColor="#666" color="#999" />
      </view>
      
      <!-- Empty State -->
      <view class="empty-state" v-else-if="!loading">
        <u-icon name="star" color="#666" size="60"></u-icon>
        <text class="empty-text">暂无收藏</text>
      </view>
      
      <!-- Loading State -->
      <view class="loading-state" v-if="loading && posts.length === 0">
        <u-loading-icon mode="circle" color="#f9d406"></u-loading-icon>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { favoriteApi, fileApi } from '@/api';

const posts = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const loadStatus = ref('loadmore');

onLoad(() => {
  loadFavorites();
});

const loadFavorites = async () => {
  if (loading.value || !hasMore.value) return;
  
  loading.value = true;
  loadStatus.value = 'loading';
  
  try {
    const res = await favoriteApi.list({
      page: page.value,
      size: pageSize.value
    });
    
    if (res && res.records) {
      const newPosts = res.records.map(item => {
        // The backend returns PostPageResp objects in the records list
        // We need to map them to our display format
        return {
          id: item.id,
          title: item.title,
          content: item.content,
          image: item.images && item.images.length > 0 ? fileApi.getFileUrl(item.images[0]) : '',
          userAvatar: item.userAvatar ? fileApi.getFileUrl(item.userAvatar) : '',
          userName: item.userName,
          createTime: item.createTime,
          likes: item.likes,
          commentCount: item.commentCount,
          views: item.views
        };
      });
      
      if (page.value === 1) {
        posts.value = newPosts;
      } else {
        posts.value = [...posts.value, ...newPosts];
      }
      
      hasMore.value = newPosts.length === pageSize.value;
      loadStatus.value = hasMore.value ? 'loadmore' : 'nomore';
      page.value++;
    } else {
      hasMore.value = false;
      loadStatus.value = 'nomore';
    }
  } catch (error) {
    console.error('Failed to load favorites:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
    loadStatus.value = 'nomore';
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  loadFavorites();
};

const goBack = () => {
  uni.navigateBack();
};

const goToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/post/detail?id=${id}`
  });
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  // Within 24 hours
  if (diff < 24 * 60 * 60 * 1000) {
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return `${minutes > 0 ? minutes : 1}分钟前`;
    }
    const hours = Math.floor(diff / (60 * 60 * 1000));
    return `${hours}小时前`;
  }
  
  // Within current year
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  }
  
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
};
</script>

<style lang="scss" scoped>
.container {
  background-color: #1A1811;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  height: 44px;
  padding-top: var(--status-bar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 16px;
  padding-right: 16px;
  background-color: #1A1811;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.nav-title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}

.nav-left, .nav-right {
  width: 40px;
  display: flex;
  align-items: center;
}

.content-scroll {
  flex: 1;
  height: 0;
}

.post-list {
  padding: 16px;
}

.post-item {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.post-main {
  display: flex;
  padding: 12px;
  gap: 12px;
}

.post-img-box {
  width: 100px;
  height: 75px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.post-img {
  width: 100%;
  height: 100%;
  background-color: #2C2C2C;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-title {
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.post-author-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.post-author-avatar {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.post-author-name {
  color: #999;
  font-size: 12px;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-time {
  color: #666;
  font-size: 11px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-num {
  color: #999;
  font-size: 11px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  gap: 16px;
}

.empty-text {
  color: #666;
  font-size: 14px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 20px;
}
</style>