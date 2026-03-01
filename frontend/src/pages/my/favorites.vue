<template>
  <view class="container">
    <!-- Navigation Bar -->
    <view class="nav-bar" :style="{ paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="24"></u-icon>
      </view>
      <text class="nav-title">我的收藏</text>
      <view class="nav-right"></view>
    </view>

    <!-- Tabs -->
    <view class="tabs-container">
      <u-tabs :list="tabList" :current="currentTab" @click="handleTabClick" lineColor="#f9d406" activeStyle="color: #f9d406; font-weight: bold;" inactiveStyle="color: #999;" itemStyle="height: 88rpx; padding: 0 30rpx;"></u-tabs>
    </view>

    <!-- Content Area -->
    <scroll-view scroll-y class="content-scroll" @scrolltolower="loadMore">
      <!-- Post List -->
      <view class="post-list" v-if="currentTab === 0 && posts.length > 0">
        <view v-for="(post, index) in posts" :key="index" class="post-item" @click="goToDetail(post.id)">
          <view class="post-main">
            <view class="post-img-box" v-if="post.image">
              <image class="post-img" :src="post.image" mode="aspectFill"></image>
            </view>
            <view class="post-info">
              <text class="post-title">{{ post.title }}</text>
              <view class="post-footer">
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
                <view class="post-author-info">
                  <image class="post-author-avatar" :src="post.userAvatar || '/static/soccer-logo.png'" mode="aspectFill"></image>
                  <text class="post-author-name">{{ post.userName }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <u-loadmore :status="loadStatus" lineColor="#666" color="#999" />
      </view>

      <!-- News List -->
      <view class="news-list" v-if="currentTab === 1 && newsList.length > 0">
        <view v-for="(news, index) in newsList" :key="index" class="news-item" @click="goToNewsDetail(news.id)">
          <view class="news-content">
            <view class="news-info">
              <text class="news-title">{{ news.title }}</text>
              <view class="news-meta">
                <text class="news-tag" v-if="news.category">{{ news.category }}</text>
                <text class="news-time">{{ formatTime(news.createTime) }}</text>
              </view>
            </view>
            <view class="news-img-box" v-if="news.coverUrl">
              <image class="news-img" :src="news.coverUrl" mode="aspectFill"></image>
            </view>
          </view>
        </view>
        <u-loadmore :status="loadStatus" lineColor="#666" color="#999" />
      </view>

      <!-- Player List -->
      <view class="player-list" v-if="currentTab === 2 && playerList.length > 0">
        <view v-for="(player, index) in playerList" :key="index" class="player-card" @click="goToPlayerDetail(player.id)">
          <image class="player-photo" :src="player.photo" mode="aspectFill"></image>
          <view class="player-info">
            <text class="player-name">{{ player.name }}</text>
            <text class="player-team">{{ player.teamName }}</text>
          </view>
          <u-icon name="arrow-right" color="#666" size="20"></u-icon>
        </view>
        <u-loadmore :status="loadStatus" lineColor="#666" color="#999" />
      </view>
      
      <!-- Empty State -->
      <view class="empty-state" v-else-if="!loading && ((currentTab === 0 && posts.length === 0) || (currentTab === 1 && newsList.length === 0) || (currentTab === 2 && playerList.length === 0))">
        <u-icon name="star" color="#666" size="60"></u-icon>
        <text class="empty-text">暂无收藏</text>
      </view>
      
      <!-- Loading State -->
      <view class="loading-state" v-if="loading && ((currentTab === 0 && posts.length === 0) || (currentTab === 1 && newsList.length === 0) || (currentTab === 2 && playerList.length === 0))">
        <u-loading-icon mode="circle" color="#f9d406"></u-loading-icon>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { favoriteApi, fileApi, playerApi } from '@/api';

const currentTab = ref(0);
const navbarPaddingRight = ref(0);
const tabList = [
  { name: '帖子' },
  { name: '新闻' },
  { name: '球员' }
];

const getCategoryName = (categoryId) => {
  const maps = {
    1: '中超',
    2: '英超',
    3: '德甲',
    4: '意甲',
    5: '欧战',
    6: '西甲'
  }
  return maps[categoryId] || '足球'
}

const posts = ref([]);
const newsList = ref([]);
const playerList = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const loadStatus = ref('loadmore');

onLoad(() => {
  // #ifdef MP-WEIXIN
  // 适配小程序胶囊按钮，防止遮挡右上角功能键
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    const systemInfo = uni.getSystemInfoSync();
    // 胶囊到右边的距离 + 胶囊宽度 + 额外间距 (8px)
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.right) + menuButton.width + 8;
  } catch (e) {
    console.error('获取胶囊按钮信息失败:', e);
    navbarPaddingRight.value = 94; // 微信小程序默认胶囊区域宽度约为 94px
  }
  // #endif

  loadData();
});

const handleTabClick = (item) => {
  if (currentTab.value === item.index) return;
  currentTab.value = item.index;
  resetData();
  loadData();
};

const resetData = () => {
  page.value = 1;
  posts.value = [];
  newsList.value = [];
  playerList.value = [];
  hasMore.value = true;
  loadStatus.value = 'loadmore';
};

const loadData = async () => {
  if (loading.value || !hasMore.value) return;
  
  loading.value = true;
  loadStatus.value = 'loading';
  
  try {
    let res;
    if (currentTab.value === 0) {
      res = await favoriteApi.list({
        page: page.value,
        size: pageSize.value
      });
      
      if (res && res.records) {
        const newPosts = res.records.map(item => ({
          id: item.id,
          title: item.title,
          content: item.content,
          image: item.images && item.images.length > 0 ? fileApi.getFileUrl(item.images[0]) : '',
          userAvatar: item.userAvatar ? fileApi.getFileUrl(item.userAvatar) : '',
          userName: item.userName,
          createTime: item.createTime,
          likes: item.likes,
          commentCount: item.commentCount
        }));
        
        if (page.value === 1) {
          posts.value = newPosts;
        } else {
          posts.value = [...posts.value, ...newPosts];
        }
        
        hasMore.value = newPosts.length === pageSize.value;
      } else {
        hasMore.value = false;
      }
    } else if (currentTab.value === 1) {
      res = await favoriteApi.listNews({
        page: page.value,
        size: pageSize.value
      });
      
      if (res && res.records) {
        const newNews = res.records.map(item => ({
          id: item.id,
          title: item.title,
          coverUrl: item.coverUrl ? fileApi.getFileUrl(item.coverUrl) : '',
          category: getCategoryName(item.categoryId),
          createTime: item.createTime || item.publishTime
        }));
        
        if (page.value === 1) {
          newsList.value = newNews;
        } else {
          newsList.value = [...newsList.value, ...newNews];
        }
        
        hasMore.value = newNews.length === pageSize.value;
      } else {
        hasMore.value = false;
      }
    } else if (currentTab.value === 2) {
      res = await favoriteApi.listPlayers({
        page: page.value,
        size: pageSize.value
      });

      if (res && res.records) {
        // 这里的 res.records 包含 { playerId, createTime }
        // 我们需要通过 playerApi 获取基础信息来展示
        const newPlayers = await Promise.all(res.records.map(async (item) => {
          try {
            const detailRes = await playerApi.getSportApiDetail(item.playerId);
            // 这里解析详情数据，取最基本信息
            let p = null;
            if (detailRes.response && detailRes.response.length > 0) {
              p = detailRes.response[0].player;
            } else if (detailRes.data && detailRes.data.player) {
              p = detailRes.data.player;
            } else if (detailRes.player) {
              p = detailRes.player;
            }

            if (p) {
              return {
                id: p.id,
                name: p.name,
                photo: fileApi.getFileUrl(p.photo) || `https://images.fotmob.com/image_resources/playerimages/${p.id}.png`,
                teamName: p.teamName || '未知球队'
              };
            }
          } catch (e) {
            console.error('Fetch player detail for favorite failed:', e);
          }
          return { id: item.playerId, name: '未知球员', photo: '', teamName: '' };
        }));

        if (page.value === 1) {
          playerList.value = newPlayers;
        } else {
          playerList.value = [...playerList.value, ...newPlayers];
        }
        hasMore.value = newPlayers.length === pageSize.value;
      } else {
        hasMore.value = false;
      }
    }
    
    loadStatus.value = hasMore.value ? 'loadmore' : 'nomore';
    if (hasMore.value) {
      page.value++;
    }
  } catch (e) {
    console.error(e);
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (hasMore.value && !loading.value) {
    loadData();
  }
};

const goBack = () => {
  uni.navigateBack();
};

const goToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/post/detail?id=${id}`
  });
};

const goToNewsDetail = (id) => {
  uni.navigateTo({
    url: `/pages/news/detail?id=${id}`
  });
};

const goToPlayerDetail = (id) => {
  uni.navigateTo({
    url: `/pages/player-detail/player-detail?id=${id}`
  });
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  // Within 1 hour
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return `${minutes}分钟前`;
  }
  
  // Within 24 hours
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000);
    return `${hours}小时前`;
  }
  
  // Format as YYYY-MM-DD
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
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

.tabs-container {
  background-color: #1A1811;
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

.post-list, .news-list {
  padding: 16px;
}

.post-item {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.news-item {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.news-content {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.news-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.news-title {
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 8px;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.news-tag {
  color: #f9d406;
  font-size: 10px;
  background-color: rgba(249, 212, 6, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.news-time {
  color: #666;
  font-size: 11px;
}

.news-img-box {
  width: 100px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.news-img {
  width: 100%;
  height: 100%;
  background-color: #2C2C2C;
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

.player-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.player-card {
  background-color: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  
  &:active {
    background-color: rgba(255, 255, 255, 0.06);
    transform: scale(0.98);
  }
}

.player-photo {
  width: 50px;
  height: 50px;
  border-radius: 25px;
  background-color: #2C2C2C;
}

.player-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.player-name {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.player-team {
  color: #999;
  font-size: 12px;
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