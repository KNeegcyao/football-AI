<template>
  <view class="container">
    <!-- Status Bar Placeholder -->
    <view class="status-bar"></view>

    <!-- Header (Aligned with index.vue) -->
    <view class="nav-bar" :style="{ paddingRight: navbarPaddingRight + 'px' }">
      <view class="logo-area">
        <view class="logo-icon">
          <image class="logo-img" src="/static/soccer-logo.png" mode="aspectFit"></image>
        </view>
        <text class="logo-text">PULSE<text class="primary">DISCOVERY</text></text>
      </view>

      <view class="nav-actions">
        <view class="action-btn" @click="navigateToNotification">
          <u-icon name="bell" color="#fff" size="44rpx"></u-icon>
          <view class="notification-badge" v-if="unreadCount > 0">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </view>
        </view>
      </view>
    </view>

    <!-- Main Content -->
    <view class="main-content">
      <!-- Search Bar (Integrated into main content) -->
      <view class="search-section">
        <view class="search-bar">
          <u-icon name="search" size="40rpx" color="rgba(249, 212, 6, 0.4)" class="search-icon"></u-icon>
          <input class="search-input" v-model="searchKey" placeholder="搜索圈子、话题" placeholder-style="color: rgba(255, 255, 255, 0.2)" />
        </view>
      </view>

      <!-- Hot Circles -->
      <view class="section" v-if="filteredHotCircles.length > 0">
        <view class="section-header">
          <text class="section-title">热门圈子</text>
          <text class="view-all" @click="navigateToCircleList">查看全部</text>
        </view>
        
        <scroll-view scroll-x class="circles-scroll" show-scrollbar="false">
          <view class="circles-container">
            <view class="circle-item" v-for="(circle, index) in filteredHotCircles" :key="index" @click="navigateToCircle(circle)">
              <view class="circle-avatar-wrapper" :class="{'highlight': index === 0}">
                <view class="circle-avatar-inner">
                  <image class="circle-avatar" :src="circle.image" mode="aspectFill"></image>
                </view>
              </view>
              <text class="circle-name">{{ circle.name }}</text>
              <text class="circle-members">{{ circle.members }} 成员</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Trend Topics -->
      <view class="section" v-if="filteredTrendTopics.length > 0">
        <view class="section-header">
          <text class="section-title">趋势话题</text>
          <view class="trending-badge">
            <u-icon name="level" size="32rpx" color="#f9d406"></u-icon>
            <text class="trending-text">正在热议</text>
          </view>
        </view>

        <view class="trends-list">
          <view class="trend-item glass-panel" v-for="(topic, index) in filteredTrendTopics" :key="index" @click="navigateToPost(topic)">
            <view class="trend-bg-icon">
              <u-icon name="tags" size="96rpx" color="#f9d406" style="opacity: 0.1;"></u-icon>
            </view>
            <view class="trend-content">
              <text class="trend-title">{{ topic.title }}</text>
              <text class="trend-stats">{{ topic.stats }}</text>
              
              <view class="trend-avatars" v-if="topic.avatars">
                <view class="avatar-group">
                  <image v-for="(avatar, i) in topic.avatars" :key="i" :src="getAvatarUrl(avatar)" class="mini-avatar" mode="aspectFill"></image>
                  <view class="mini-avatar-count" v-if="topic.extraCount">
                    <text class="count-text">+{{ topic.extraCount }}</text>
                  </view>
                </view>
              </view>
              
              <view class="trend-meta" v-if="topic.tags">
                <text class="meta-tag">{{ topic.tags[0] }}</text>
                <text class="meta-time">{{ topic.time }}</text>
              </view>
            </view>
            
            <button class="action-btn" :class="{'btn-join': topic.action === '加入', 'btn-explore': topic.action === '探索'}">
              {{ topic.action }}
            </button>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view class="empty-state" v-if="searchKey && filteredHotCircles.length === 0 && filteredTrendTopics.length === 0">
        <u-icon name="search" size="120rpx" color="rgba(249, 212, 6, 0.2)" class="empty-icon"></u-icon>
        <text class="empty-text">未找到与“{{ searchKey }}”相关的结果</text>
        <text class="empty-sub">换个关键词试试吧</text>
      </view>
      
      <!-- Bottom Padding for TabBar -->
      <view class="safe-area-bottom"></view>
    </view>



    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
        @tap="handleTabClick(index)">
        <u-icon :name="tab.icon" :color="currentTab === index ? '#f9d406' : '#7A7E83'" size="24"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { communityApi, fileApi, notificationApi } from '@/api';
import { BASE_URL } from '@/utils/request.js';

const unreadCount = ref(0);
const navbarPaddingRight = ref(0);
const searchKey = ref('');
const { proxy } = getCurrentInstance();

const hotCircles = ref([]);
const trendTopics = ref([]);

// 过滤后的热门圈子
const filteredHotCircles = computed(() => {
  if (!searchKey.value) return hotCircles.value;
  const key = searchKey.value.toLowerCase();
  return hotCircles.value.filter(circle => 
    circle.name.toLowerCase().includes(key)
  );
});

// 过滤后的趋势话题
const filteredTrendTopics = computed(() => {
  if (!searchKey.value) return trendTopics.value;
  const key = searchKey.value.toLowerCase();
  return trendTopics.value.filter(topic => 
    topic.title.toLowerCase().includes(key)
  );
});

const currentTab = ref(2); // 社区是第3个，索引为2
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/community/community' },
  { text: '我的', icon: 'account', path: 'pages/my/my' }
];

const handleTabClick = (index) => {
  const tab = tabs[index]
  if (!tab || !tab.path) return
  
  if (currentTab.value === index) return

  const url = tab.path.startsWith('/') ? tab.path : '/' + tab.path
  
  uni.switchTab({
    url: url,
    fail: () => {
      uni.reLaunch({ url })
    }
  })
}



const isNavigating = ref(false);

const navigateToNotification = () => {
  uni.navigateTo({
    url: '/pages/community/notification'
  });
};

const navigateToCircle = (circle) => {
  if (isNavigating.value) return;
  isNavigating.value = true;
  
  uni.navigateTo({
    url: `/pages/community/circle-detail?id=${circle.id || ''}&name=${encodeURIComponent(circle.name)}&members=${encodeURIComponent(circle.members)}&image=${encodeURIComponent(circle.image)}`,
    complete: () => {
      setTimeout(() => {
        isNavigating.value = false;
      }, 500);
    },
    fail: () => {
      isNavigating.value = false;
      uni.showToast({
        title: '圈子详情页加载失败',
        icon: 'none'
      });
    }
  });
};

const navigateToPost = (topic) => {
  if (isNavigating.value) return;
  isNavigating.value = true;

  uni.navigateTo({
    url: `/pages/community/topic-detail?id=${topic.id || 0}&title=${encodeURIComponent(topic.title)}`,
    complete: () => {
      setTimeout(() => {
        isNavigating.value = false;
      }, 500);
    },
    fail: () => {
      isNavigating.value = false;
      uni.showToast({
        title: '话题详情页加载失败',
        icon: 'none'
      });
    }
  });
};

// 获取头像 URL
const getAvatarUrl = (url) => {
  if (!url) return '/static/default-team.png';
  if (url.startsWith('http') || url.startsWith('https')) return url;
  if (url.startsWith('/static/')) return url;
  return `${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
};

const navigateToCircleList = () => {
  uni.navigateTo({
    url: '/pages/community/circle-list',
    fail: () => {
      uni.showToast({
        title: '列表页加载失败',
        icon: 'none'
      });
    }
  });
};

const loadUnreadCount = async () => {
  try {
    const res = await notificationApi.getUnreadCount();
    if (res.code === 200) {
      unreadCount.value = res.data;
    }
  } catch (e) {
    console.error('Failed to load unread count:', e);
  }
};

onShow(async () => {
  uni.hideTabBar();
  const token = uni.getStorageSync('token');
  if (token) {
    loadUnreadCount();
  }
  loadData();
});

const loadData = async () => {
  try {
    // 尝试从 API 获取数据
    const [circlesRes, topicsRes] = await Promise.allSettled([
      communityApi.getHotCircles(),
      communityApi.getTrendTopics()
    ]);

    if (circlesRes.status === 'fulfilled' && circlesRes.value) {
      // API 返回的数据可能直接是数组，或者是包含 data 字段的对象
      // 根据 CommunityController，getHotCircles 返回 R<List<Map>>
      // 假设 request.js 返回 data 字段
      hotCircles.value = circlesRes.value.map(circle => ({
        ...circle,
        image: circle.image ? fileApi.getFileUrl(circle.image) : '/static/default-team.png'
      }));
    } else {
      console.warn('Failed to load hot circles');
    }

    if (topicsRes.status === 'fulfilled' && topicsRes.value) {
      trendTopics.value = topicsRes.value.map(topic => ({
        ...topic,
        avatars: topic.avatars ? topic.avatars.map(avatar => fileApi.getFileUrl(avatar)) : []
      }));
    } else {
      console.warn('Failed to load trend topics');
    }
  } catch (e) {
    console.error('Error loading community data:', e);
  }
};


onMounted(() => {
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
</script>

<style lang="scss" scoped>
/* Colors & Base */
.container {
  background-color: $pitch-pulse-bg-dark;
  min-height: 100vh;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow-x: hidden;
  box-sizing: border-box;
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.nav-bar {
  display: flex;
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 40rpx;
  background-color: rgba($pitch-pulse-bg-dark, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.logo-icon {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10rpx;
  
  .logo-img {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 2rpx 4rpx rgba(0,0,0,0.3));
  }
}

.logo-text {
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: -1rpx;
  color: #fff;
  .primary {
    color: $pitch-pulse-primary;
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.action-btn {
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  background-color: #f20d33;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  min-width: 32rpx;
  text-align: center;
  line-height: 24rpx;
  border: 4rpx solid $pitch-pulse-bg-dark;
  font-weight: 700;
}

.search-section {
  padding: 0 40rpx;
  margin-bottom: 40rpx;
}

.search-bar {
  position: relative;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  transition: all 0.3s ease;
}

.search-bar:focus-within {
  background-color: rgba(255, 255, 255, 0.08);
  border-color: rgba($pitch-pulse-primary, 0.4);
  box-shadow: 0 0 20rpx rgba($pitch-pulse-primary, 0.1);
}

.search-icon {
  margin-right: 20rpx;
}

.search-input {
  flex: 1;
  color: #fff;
  font-size: 28rpx;
}

.main-content {
  flex: 1;
  padding: 30rpx 0;
}

/* Sections */
.section {
  margin-bottom: 50rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.view-all {
  color: $pitch-pulse-primary;
  font-size: 24rpx;
  font-weight: 600;
}

/* Hot Circles */
.circles-scroll {
  white-space: nowrap;
  width: 100%;
}

.circles-container {
  display: flex;
  padding: 10rpx 40rpx;
}

.circle-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 40rpx;
  transition: all 0.3s ease;
}

.circle-item:active {
  transform: scale(0.95);
}

.circle-avatar-wrapper {
  width: 140rpx;
  height: 140rpx;
  border-radius: 24rpx;
  padding: 6rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
  margin-bottom: 16rpx;
}

.circle-avatar-wrapper.highlight {
  background: linear-gradient(135deg, $pitch-pulse-primary, #ff8a00);
  border: none;
  box-shadow: 0 10rpx 20rpx rgba($pitch-pulse-primary, 0.2);
}

.circle-avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  background-color: $pitch-pulse-bg-dark;
  overflow: hidden;
  border: 4rpx solid $pitch-pulse-bg-dark;
}

.circle-avatar {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.circle-name {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4rpx;
}

.circle-members {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

/* Trending */
.trending-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background-color: rgba($pitch-pulse-primary, 0.1);
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  border: 1rpx solid rgba($pitch-pulse-primary, 0.1);
}

.trending-text {
  font-size: 22rpx;
  font-weight: 800;
  color: $pitch-pulse-primary;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.trends-list {
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  padding: 32rpx;
  border-radius: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.trend-bg-icon {
  position: absolute;
  top: -20rpx;
  right: -20rpx;
  z-index: 0;
  pointer-events: none;
}

.trend-content {
  position: relative;
  z-index: 1;
  flex: 1;
  padding-right: 20rpx;
}

.trend-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #fff;
  margin-bottom: 12rpx;
  display: block;
}

.trend-stats {
  font-size: 22rpx;
  color: $pitch-pulse-primary;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  display: block;
  margin-bottom: 20rpx;
  opacity: 0.8;
}

.trend-avatars {
  display: flex;
  margin-bottom: 24rpx;
}

.avatar-group {
  display: flex;
}

.mini-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 8rpx;
  border: 2rpx solid $pitch-pulse-bg-dark;
  margin-left: -16rpx;
}

.mini-avatar:first-child {
  margin-left: 0;
}

.mini-avatar-count {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  background-color: #35332c;
  border: 4rpx solid #25231c;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -16rpx;
}

.count-text {
  font-size: 16rpx;
  font-weight: 800;
  color: #fff;
}

.trend-meta {
  display: flex;
  gap: 20rpx;
  align-items: center;
}

.meta-tag {
  background-color: rgba($pitch-pulse-primary, 0.1);
  color: $pitch-pulse-primary;
  font-size: 20rpx;
  font-weight: 800;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.meta-time {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

.action-btn {
  position: relative;
  z-index: 1;
  font-size: 24rpx;
  font-weight: 800;
  padding: 16rpx 32rpx;
  border-radius: 20rpx;
  line-height: 1;
  margin: 0;
  border: none;
  min-width: 120rpx;
}

.btn-join {
  background-color: $pitch-pulse-primary;
  color: #1a1811;
  box-shadow: 0 8rpx 16rpx rgba($pitch-pulse-primary, 0.2);
}

.btn-explore {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-icon {
  margin-bottom: 40rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.4);
}

/* TabBar Styles */
.safe-area-bottom {
  height: 160rpx;
}

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  margin: 0 auto;
  width: 100%;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
  
  height: 120rpx;
  background-color: rgba(26, 24, 17, 0.98);
  backdrop-filter: blur(20px);
  border-top: 1rpx solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 9999;
  box-sizing: border-box;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.tab-text {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 600;
}

.tab-item.active .tab-text {
  color: $pitch-pulse-primary;
  font-weight: 800;
}

/* Utils */
::v-deep .uni-input-input {
  outline: none !important;
  box-shadow: none !important;
}

::v-deep input {
  outline: none !important;
  box-shadow: none !important;
}
</style>