<template>
  <view class="container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 固定顶部容器 -->
    <view class="fixed-header">
      <!-- 状态栏占位 -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="logo-area">
          <view class="logo-icon">
            <image src="/static/soccer-logo.png" mode="aspectFit" class="logo-img"></image>
          </view>
          <view class="logo-text italic">
            PITCH<text class="highlight text-theme-main">PULSE</text>
          </view>
        </view>
        <view class="nav-actions">
          <view class="action-btn" @click="handleSearch">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">search</text>
          </view>
          <view class="action-btn" @click="handleNotification">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">notifications</text>
            <view class="notification-badge" v-if="totalUnread > 0">{{ totalUnread > 99 ? '99+' : totalUnread }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 顶部占位 -->
    <view class="header-placeholder"></view>

    <!-- 主体内容滚动 -->
    <scroll-view scroll-y class="main-content-scroll" @scrolltolower="loadMore">
      <!-- 搜索栏 -->
      <view class="search-section anim-fade-in">
        <view class="search-bar" hover-class="btn-active" @click="handleSearch">
          <text class="material-icons" style="font-size: 44rpx; color: var(--text-secondary); margin-right: 16rpx;">search</text>
          <input type="text" class="search-input" placeholder="搜索话题、圈子或用户..." disabled />
        </view>
      </view>

      <!-- 热门圈子 -->
      <view class="section anim-slide-up stagger-1">
        <view class="section-header">
          <text class="section-title">热门圈子</text>
          <text class="view-all" hover-class="btn-active" @click="viewAllCircles">查看全部</text>
        </view>
        <view class="circles-grid">
          <view class="circle-item" hover-class="btn-active" :hover-start-time="20" :hover-stay-time="70" v-for="(circle, index) in hotCircles.slice(0, 4)" :key="circle.id" @click="navigateToCircle(circle)">
            <view class="circle-avatar-wrapper" :class="{ 
              'rank-1': index === 0,
              'rank-2': index === 1,
              'rank-3': index === 2 
            }">
              <view class="circle-avatar-inner">
                <image :src="getFullImageUrl(circle.image) || '/static/soccer-logo.png'" mode="aspectFill" class="circle-avatar"></image>
              </view>
            </view>
            <text class="circle-name text-ellipsis">{{ circle.name }}</text>
            <text class="circle-members">{{ circle.members }} 成员</text>
          </view>
        </view>
      </view>

      <!-- 热门话题 -->
      <view class="section anim-slide-up stagger-2">
        <view class="section-header">
          <view class="trending-badge">
            <text class="material-icons" style="font-size: 28rpx; color: #f9d406;">trending_up</text>
            <text class="trending-text">热门话题</text>
          </view>
        </view>

        <view class="trends-list">
          <!-- 加载状态 -->
          <view v-if="loading && page === 1" class="loading-state">
            <view class="loading-spinner"></view>
            <text class="loading-text">正在发现新鲜事...</text>
          </view>

          <!-- 空状态 -->
          <view v-else-if="trends.length === 0" class="empty-state anim-fade-in">
            <text class="material-icons" style="font-size: 120rpx; color: var(--text-secondary); opacity: 0.2;">forum</text>
            <text class="empty-text">暂无热门话题</text>
            <text class="empty-sub">去其他圈子看看吧</text>
          </view>

          <!-- 话题列表 -->
          <template v-else>
            <view class="trend-item anim-slide-up" :class="'stagger-' + ((index % 5) + 3)" hover-class="btn-active" :hover-start-time="20" :hover-stay-time="70" v-for="(trend, index) in trends" :key="trend.id" @click="navigateToTopic(trend)">
              <view class="trend-bg-icon">
                <text class="material-icons" style="font-size: 160rpx; color: var(--text-main); opacity: 0.03;">{{ trend.icon || 'topic' }}</text>
              </view>
              
              <view class="trend-content">
                <text class="trend-title">{{ trend.title }}</text>
                <text class="trend-stats">{{ trend.postsCount }}</text>
                
                <view class="trend-avatars" v-if="trend.activeUsers && trend.activeUsers.length > 0">
                  <view class="avatar-group">
                    <image v-for="(user, idx) in trend.activeUsers.slice(0, 3)" :key="idx" :src="getFullImageUrl(user.avatar) || '/static/default-avatar.png'" class="mini-avatar" mode="aspectFill"></image>
                    <view class="mini-avatar-count" v-if="trend.activeUsers.length > 3">
                      <text class="count-text">+{{ trend.activeUsers.length - 3 }}</text>
                    </view>
                  </view>
                </view>

                <view class="trend-meta">
                  <text class="meta-tag"># {{ trend.tagName || '热门' }}</text>
                  <text class="meta-time">{{ formatTime(trend.createTime) }}</text>
                </view>
              </view>
            </view>
          </template>
        </view>

        <!-- 分页 -->
        <view class="pagination-box" v-if="totalPages > 1">
          <view class="page-btn" :class="{ disabled: page <= 1 }" @click="prevPage">
            <text class="material-icons btn-icon">chevron_left</text>
            <text>上一页</text>
          </view>
          
          <view class="page-info">
            <text class="current-page">{{ page }}</text>
            <text class="page-divider">/</text>
            <text class="total-label">{{ totalPages }}</text>
          </view>

          <view class="page-btn" :class="{ disabled: page >= totalPages }" @click="nextPage">
            <text>下一页</text>
            <text class="material-icons btn-icon">chevron_right</text>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="3" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useThemeStore } from '@/store/theme';
import { useChatStore } from '@/store/chat';
import request from '@/utils/request';
import { getFullImageUrl } from '@/utils/request.js';
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue';

const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight);
const chatStore = useChatStore();

// 数据状态
const hotCircles = ref([]);
const trends = ref([]);
const unreadCount = ref(0);
const loading = ref(false);
const page = ref(1);
const totalPages = ref(1);
const pageSize = ref(10);

const totalUnread = computed(() => unreadCount.value + chatStore.totalUnreadCount);

// 获取热门圈子
const fetchHotCircles = async () => {
  try {
    const res = await request.get('/api/community/circles/hot');
    if (res) {
      hotCircles.value = res;
    }
  } catch (e) {
    console.error('获取热门圈子失败:', e);
  }
};

// 获取热门话题
const fetchTrends = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await request.get('/api/community/topics/trending', { 
      page: page.value, 
      size: pageSize.value 
    });
    if (res && res.records) {
      // 适配后端返回的 trending 接口数据结构
      trends.value = res.records.map(item => ({
        id: item.id,
        title: item.title,
        postsCount: item.stats, // 后端已格式化好的字符串，如 "每小时100 帖子"
        viewCount: item.viewCount || 0,
        tagName: item.tags ? item.tags[0] : '热门',
        createTime: item.createTime || item.time || new Date().toISOString(),
        activeUsers: item.avatars ? item.avatars.map(av => ({ avatar: av })) : [],
        isJoined: item.action === '查看'
      }));
      totalPages.value = res.pages || 1;
    }
  } catch (e) {
    console.error('获取热门话题失败:', e);
  } finally {
    loading.value = false;
  }
};

// 分页处理
const prevPage = () => {
  if (page.value > 1) {
    page.value--;
    fetchTrends();
    scrollToTop();
  }
};

const nextPage = () => {
  if (page.value < totalPages.value) {
    page.value++;
    fetchTrends();
    scrollToTop();
  }
};

const scrollToTop = () => {
  // 滚动到顶部的逻辑，如果需要的话
};

// 事件处理
const handleSearch = () => {
  uni.navigateTo({ url: '/pages/community/circle-search' });
};

const handleNotification = () => {
  uni.navigateTo({ url: '/pages/community/notification' });
};

const viewAllCircles = () => {
  uni.navigateTo({ url: '/pages/community/circle-list' });
};

const navigateToCircle = (circle) => {
  uni.navigateTo({
    url: `/pages/community/circle-detail?id=${circle.id}&name=${encodeURIComponent(circle.name)}`
  });
};

const navigateToTopic = (topic) => {
  uni.navigateTo({
    url: `/pages/community/topic-detail?id=${topic.id}&title=${encodeURIComponent(topic.title)}`
  });
};

const formatTime = (time) => {
  if (!time) return '';
  // 如果已经是格式化好的时间描述，直接返回
  if (typeof time === 'string' && (time.includes('前') || time.includes('刚刚'))) {
    return time;
  }
  
  const date = new Date(time);
  // 检查无效日期
  if (isNaN(date.getTime())) {
    return time; // 返回原样，可能是后端返回的描述
  }
  
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

// 生命周期
onShow(() => {
  uni.hideTabBar();
  // 更新未读消息数
  fetchUnreadCount();
});

const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/api/notifications/unread-count');
    unreadCount.value = res || 0;
  } catch (e) {
    console.error('获取未读消息失败:', e);
    // 如果是 404 错误，通常是后端接口还没实现或路径不对，静默处理
    unreadCount.value = 0;
  }
};

onMounted(() => {
  fetchHotCircles();
  fetchTrends();
});
</script>

<style lang="scss" scoped>
/* Colors & Base */
.container {
  background-color: var(--bg-main);
  height: 100vh;
  color: var(--text-main);
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow: hidden;
  box-sizing: border-box;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.status-bar {
  width: 100%;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  background-color: #1a1811;
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.header-placeholder {
  height: calc(var(--status-bar-height) + 100rpx);
  width: 100%;
}

.nav-bar {
  width: 100%;
  height: 100rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  background-color: #1a1811;
  border-bottom: 1rpx solid var(--border-main);
}

.main-content-scroll {
  flex: 1;
  height: 0; /* 必须：强制 flex 计算生效 */
  width: 100%;
}

.bottom-placeholder {
  height: calc(120rpx + env(safe-area-inset-bottom));
  width: 100%;
}

.logo-area {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;

  .logo-icon {
    width: 56rpx;
    height: 56rpx;
    margin-right: 12rpx;
    display: flex;
    align-items: center;

    .logo-img {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 32rpx;
    font-weight: 800;
    letter-spacing: 2rpx;
    line-height: 1;
    color: #d4af37;
    
    &.italic {
      font-style: italic;
    }
    
    .highlight {
      color: #fff;
      margin-left: 6rpx;
    }
  }
}

.nav-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  flex-shrink: 0;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
    background-color: rgba(255, 255, 255, 0.05);
    border-radius: 16rpx;
    position: relative;
  }
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
  border: 4rpx solid var(--nav-bar-bg);
  font-weight: 700;
}

.search-section {
  padding: 30rpx 32rpx;
}

.search-bar {
  position: relative;
  background-color: var(--bg-secondary);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  transition: all 0.3s ease;
}

.search-input {
  flex: 1;
  color: var(--text-main);
  font-size: 28rpx;
}

/* Sections */
.section {
  margin-bottom: 50rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-main);
}

.view-all {
  color: #f9d406;
  font-size: 24rpx;
  font-weight: 600;
}

/* Hot Circles */
.circles-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 0 32rpx;
  gap: 20rpx;
}

.circle-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  width: 100%;
}

.circle-avatar-wrapper {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  padding: 6rpx;
  border: 2rpx solid var(--border-color);
  margin-bottom: 12rpx;
}

.circle-avatar-wrapper.rank-1 {
  background: linear-gradient(135deg, #f9d406, #ff8a00); /* 金色/黄橙渐变 */
  border: none;
}

.circle-avatar-wrapper.rank-2 {
  background: linear-gradient(135deg, #e5e7eb, #9ca3af); /* 银色渐变 */
  border: none;
}

.circle-avatar-wrapper.rank-3 {
  background: linear-gradient(135deg, #fb923c, #b45309); /* 铜色渐变 */
  border: none;
}

.circle-avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  background-color: var(--bg-main);
  overflow: hidden;
  border: 4rpx solid var(--bg-main);
}

.circle-avatar {
  width: 100%;
  height: 100%;
}

.circle-name {
  font-size: 22rpx;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 4rpx;
  width: 100%;
  text-align: center;
}

.circle-members {
  font-size: 18rpx;
  color: var(--text-secondary);
  font-weight: 500;
  opacity: 0.6;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Trending */
.trending-badge {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background-color: rgba(249, 212, 6, 0.1);
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  border: 1rpx solid rgba(249, 212, 6, 0.2);
}

.trending-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #f9d406;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.trends-list {
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.trend-item {
  padding: 32rpx;
  position: relative;
  border-radius: 30rpx;
  background-color: var(--bg-secondary);
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow: hidden;
  border: 1rpx solid var(--border-main);
  transition: all 0.3s ease;
}

.trend-bg-icon {
  position: absolute;
  right: -20rpx;
  bottom: -20rpx;
  transform: rotate(-15deg);
  pointer-events: none;
  z-index: 0;
}

.trend-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  z-index: 1;
  padding-right: 20rpx;
}

.trend-title {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 4rpx;
}

.trend-stats {
  font-size: 22rpx;
  color: #f9d406;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  opacity: 0.8;
}

.trend-avatars {
  margin: 16rpx 0;
}

.avatar-group {
  display: flex;
  align-items: center;
}

.mini-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  border: 2rpx solid var(--bg-secondary);
  margin-left: -16rpx;
  background-color: var(--bg-main);
}

.mini-avatar:first-child {
  margin-left: 0;
}

.mini-avatar-count {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-main);
  border: 2rpx solid var(--bg-secondary);
  margin-left: -16rpx;
  z-index: 1;
}

.count-text {
  font-size: 16rpx;
  font-weight: 800;
  color: var(--text-main);
}

.trend-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 10rpx;
}

.meta-tag {
  font-size: 20rpx;
  font-weight: 800;
  padding: 6rpx 16rpx;
  background-color: rgba(249, 212, 6, 0.1);
  border-radius: 8rpx;
  color: #f9d406;
}

.meta-time {
  font-size: 20rpx;
  color: var(--text-secondary);
  font-weight: 500;
  opacity: 0.6;
}

.btn-join {
  position: relative;
  z-index: 1;
  padding: 16rpx 32rpx;
  background-color: #f9d406;
  color: #1a1811;
  font-size: 24rpx;
  font-weight: 800;
  border-radius: 20rpx;
  border: none;
  box-shadow: 0 8rpx 16rpx rgba(249, 212, 6, 0.2);
  min-width: 120rpx;
  line-height: 1;
  margin: 0;
}

.btn-explore {
  position: relative;
  z-index: 1;
  padding: 16rpx 32rpx;
  background-color: var(--bg-secondary);
  color: var(--text-main);
  font-size: 24rpx;
  font-weight: 800;
  border-radius: 20rpx;
  border: 1rpx solid var(--border-main);
  min-width: 120rpx;
  line-height: 1;
  margin: 0;
}

/* Pagination */
.pagination-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
  gap: 30rpx;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(249, 212, 6, 0.1);
  color: #f9d406;
  padding: 16rpx 32rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  transition: all 0.2s ease;
  border: 1rpx solid rgba(249, 212, 6, 0.2);
  min-width: 180rpx;
  gap: 8rpx;
  
  &.disabled {
    opacity: 0.3;
    background: var(--bg-secondary);
    color: var(--text-secondary);
    border-color: transparent;
    pointer-events: none;
  }

  &:active:not(.disabled) {
    transform: scale(0.95);
    background: rgba(249, 212, 6, 0.2);
  }
}

.btn-icon {
  font-size: 32rpx;
}

.page-info {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.current-page {
  font-size: 36rpx;
  font-weight: 800;
  color: #f9d406;
}

.page-divider {
  font-size: 24rpx;
  color: var(--text-secondary);
  opacity: 0.3;
}

.total-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  font-weight: 500;
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

.empty-text {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-main);
  margin-top: 30rpx;
  margin-bottom: 16rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: var(--text-secondary);
  opacity: 0.6;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(249, 212, 6, 0.1);
  border-top: 4rpx solid #f9d406;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24rpx;
}

.loading-text {
  font-size: 24rpx;
  color: var(--text-secondary);
  font-weight: 500;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
