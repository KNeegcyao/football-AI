<template>
  <view class="container">
    <!-- Navbar with Search -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <view class="nav-btn-glass">
          <text class="material-icons back-icon">arrow_back</text>
        </view>
      </view>
      <view class="search-box">
        <text class="material-icons search-icon">search</text>
        <input 
          class="search-input" 
          type="text" 
          :placeholder="`搜索${circleName}圈内的帖子`" 
          v-model="keyword"
          confirm-type="search"
          @confirm="handleSearch"
          :focus="true"
        />
        <view v-if="keyword" class="clear-btn" @click="clearSearch">
          <text class="material-icons clear-icon">close</text>
        </view>
      </view>
      <view class="nav-right" @click="handleSearch">
        <text class="search-btn-text">搜索</text>
      </view>
    </view>

    <!-- Content Area -->
    <scroll-view scroll-y class="content-scroll" @scrolltolower="loadMore">
      <view class="content-area">
        <!-- Loading State -->
        <view v-if="loading && page === 1" class="loading-state">
          <text>搜索中...</text>
        </view>

        <!-- Empty State -->
        <view v-else-if="searched && posts.length === 0" class="empty-state">
          <text class="material-icons empty-icon">search_off</text>
          <text class="empty-text">未找到相关帖子</text>
        </view>

        <!-- Post List -->
        <view class="post-card" v-for="(post, index) in posts" :key="post.id" @click="navigateToPost(post)">
          <view class="post-header">
            <view class="user-info">
              <image class="user-avatar" :src="post.userAvatar || '/static/default-avatar.png'" mode="aspectFill"></image>
              <text class="user-name">{{ post.userName }}</text>
            </view>
            <text class="post-time">{{ post.time }}</text>
          </view>
          
          <text class="post-content-text" v-if="post.title && post.title !== post.content">
            <text class="highlight-title">【{{ post.title }}】</text>
            {{ post.content }}
          </text>
          <text class="post-content-text" v-else>{{ post.content }}</text>
          
          <image v-if="post.image" class="post-main-img" :src="post.image" mode="aspectFill"></image>
          
          <view class="post-footer">
            <view class="interaction-item">
              <text class="material-icons footer-icon" :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">
                {{ post.isLiked ? 'favorite' : 'favorite_border' }}
              </text>
              <text :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">{{ post.likes }}</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">chat_bubble_outline</text>
              <text>{{ post.comments }}</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">share</text>
              <text>{{ post.shares }}</text>
            </view>
          </view>
        </view>

        <!-- Load More / No More -->
        <view class="loading-status" v-if="posts.length > 0">
          <text v-if="loading">加载中...</text>
          <text v-else-if="noMore">没有更多了</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { communityApi, fileApi } from '@/api/index';

const circleId = ref(null);
const circleName = ref('');
const keyword = ref('');
const posts = ref([]);
const loading = ref(false);
const noMore = ref(false);
const searched = ref(false);
const page = ref(1);
const statusBarHeight = ref(20);

onLoad((options) => {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;

  if (options.circleId) {
    circleId.value = options.circleId;
  }
  if (options.circleName) {
    circleName.value = decodeURIComponent(options.circleName);
  }
});

const goBack = () => {
  uni.navigateBack();
};

const clearSearch = () => {
  keyword.value = '';
  posts.value = [];
  searched.value = false;
};

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' });
    return;
  }
  
  page.value = 1;
  posts.value = [];
  noMore.value = false;
  searched.value = true;
  await loadPosts();
};

const loadPosts = async () => {
  if (loading.value || noMore.value) return;
  
  if (!circleName.value) {
    console.error('Circle name is missing');
    uni.showToast({ title: '参数错误', icon: 'none' });
    return;
  }

  loading.value = true;
  console.log('Searching posts with keyword:', keyword.value, 'in circle:', circleName.value);
  try {
    const pageData = await communityApi.getCirclePosts(circleName.value, {
      page: page.value,
      size: 10,
      keyword: keyword.value
    });
    console.log('Search results:', pageData);
    
    if (pageData && pageData.records) {
      const newPosts = pageData.records.map(post => {
        let postImages = [];
        try {
          if (post.images) {
            if (Array.isArray(post.images)) {
                postImages = post.images;
            } else if (typeof post.images === 'string') {
                if (post.images.startsWith('[')) {
                    postImages = JSON.parse(post.images);
                } else {
                    postImages = [post.images];
                }
            }
          }
        } catch (e) {
          console.error('Error parsing post images:', e);
        }

        return {
          id: post.id,
          title: post.title,
          content: post.content,
          image: postImages.length > 0 ? fileApi.getFileUrl(postImages[0]) : '', 
          likes: post.likes || 0,
          isLiked: post.isLiked || false,
          comments: post.commentCount || 0,
          shares: 0, 
          userName: post.userName || '未知用户', 
          userAvatar: post.userAvatar ? fileApi.getFileUrl(post.userAvatar) : '/static/default-avatar.png', 
          time: new Date(post.createdAt).toLocaleDateString()
        };
      });
      
      if (pageData.records.length < 10) {
        noMore.value = true;
      }
      
      if (page.value === 1) {
        posts.value = newPosts;
      } else {
        posts.value = [...posts.value, ...newPosts];
      }
      page.value++;
    } else {
        if (page.value === 1) {
            posts.value = [];
        }
        noMore.value = true;
    }
  } catch (error) {
    console.error('Failed to search posts:', error);
    uni.showToast({ title: '搜索失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (posts.length > 0) {
    loadPosts();
  }
};

const navigateToPost = (post) => {
  uni.navigateTo({
    url: `/pages/post/detail?id=${post.id}`
  });
};
</script>

<style lang="scss">
/* Color Palette */
$primary: #f2b90d;
$bg-dark: #12110a;
$surface-dark: #1c1a11;
$border-dark: #2d2a1d;
$text-gray: #9ca3af;
$text-white: #ffffff;

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-dark;
  color: $text-white;
}

/* Navbar */
.navbar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background-color: $surface-dark;
  border-bottom: 1px solid $border-dark;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  margin-right: 12px;
}

.nav-btn-glass {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.1);
  display: flex;
  justify-content: center;
  align-items: center;
}

.back-icon {
  color: #fff;
  font-size: 20px;
}

.search-box {
  flex: 1;
  height: 36px;
  background-color: rgba(255,255,255,0.08);
  border-radius: 18px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  margin-right: 12px;
}

.search-icon {
  color: $text-gray;
  font-size: 18px;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: #fff;
}

.clear-btn {
  padding: 4px;
}

.clear-icon {
  color: $text-gray;
  font-size: 16px;
}

.nav-right {
  padding: 4px 8px;
}

.search-btn-text {
  color: $primary;
  font-size: 14px;
  font-weight: bold;
}

/* Content */
.content-scroll {
  flex: 1;
  height: 0; /* Important for flex child scroll */
}

.content-area {
  padding: 16px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: $text-gray;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
}

/* Post Card (Copied from circle-detail) */
.post-card {
  background-color: $surface-dark;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid $border-dark;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
}

.post-time {
  font-size: 12px;
  color: #6b7280;
}

.post-content-text {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.625;
  display: block;
  margin-bottom: 16px;
}

.highlight-title {
  color: $primary;
  font-weight: bold;
}

.post-main-img {
  width: 100%;
  height: 192px;
  border-radius: 8px;
  margin-bottom: 16px;
  background-color: $border-dark;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 24px;
}

.interaction-item {
  display: flex;
  align-items: center;
  color: $text-gray;
  font-size: 12px;
}

.footer-icon {
  font-size: 14px;
  margin-right: 6px;
}

.loading-status {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 12px;
}
</style>