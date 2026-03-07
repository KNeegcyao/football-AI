<template>
  <view class="container" :class="themeClass">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="var(--text-main)" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="brand-text text-theme-main">PITCHPULSE</text>
        </view>
        <view class="right" :style="{ paddingRight: navbarPaddingRight + 'px' }">
          <view class="icon-btn bg-theme-secondary" @click="markAllRead" title="全部已读">
            <u-icon name="order" color="var(--text-main)" size="24"></u-icon>
          </view>
          <view class="icon-btn bg-theme-secondary" title="设置">
            <u-icon name="setting" color="var(--text-main)" size="24"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类功能区 -->
    <section class="category-section">
      <view class="category-item" @click="filterByType('reply')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-emerald">
          <u-icon name="chat" color="#10b981" size="28"></u-icon>
        </view>
        <text class="label text-theme-secondary">回复与@</text>
      </view>
      <view class="category-item" @click="filterByType('like')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-rose">
          <u-icon name="thumb-up" color="#f43f5e" size="28"></u-icon>
        </view>
        <text class="label text-theme-secondary">收到喜欢</text>
      </view>
      <view class="category-item" @click="filterByType('follow')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-sky">
          <u-icon name="account" color="#0ea5e9" size="28"></u-icon>
        </view>
        <text class="label text-theme-secondary">新增粉丝</text>
      </view>
    </section>

    <view class="notification-list">
      <view class="notification-item border-theme-main" v-for="(item, index) in sessions" :key="index" @click="handleSessionClick(item)">
        <!-- 头像区域 -->
        <view class="avatar-box">
          <image :src="getAvatarUrl(item.otherAvatar)" class="avatar-img border-theme-main bg-theme-secondary" mode="aspectFill"></image>
        </view>

        <!-- 内容区域 -->
        <view class="content-box">
          <view class="header-row">
            <view class="user-info">
              <text class="nickname text-theme-main">{{ item.otherNickname || '用户' }}</text>
            </view>
            <text class="time text-theme-secondary">{{ formatTime(item.lastMessageTime) }}</text>
          </view>

          <!-- 消息正文 -->
          <view class="message-row">
            <text class="desc-text text-theme-secondary">{{ formatMessage(item.lastMessage) }}</text>
            <view class="unread-badge" v-if="item.unreadCount > 0">{{ item.unreadCount }}</view>
          </view>
        </view>
      </view>

      <view class="empty-tip text-theme-secondary" v-if="sessions.length === 0">
        <text>暂无私聊消息</text>
      </view>
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useChatStore } from '@/store/chat';
import { useThemeStore } from '@/store/theme';
import { fileApi } from '@/api';
import { onPullDownRefresh, onLoad } from '@dcloudio/uni-app';

const chatStore = useChatStore();
const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const navbarPaddingRight = ref(16);
const statusBarHeight = ref(0);
const sessions = computed(() => chatStore.sessions);

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  fetchSessions();

  // #ifdef MP-WEIXIN
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.left) + 8;
  } catch (e) {
    navbarPaddingRight.value = 94;
  }
  // #endif
});

onPullDownRefresh(() => {
  fetchSessions().finally(() => {
    uni.stopPullDownRefresh();
  });
});

const fetchSessions = () => {
  return chatStore.fetchSessions();
};

const filterByType = (type) => {
  console.log('filterByType:', type);
  const routes = {
    reply: '/pages/community/reply-detail',
    like: '/pages/community/like-detail',
    follow: '/pages/community/fan-detail'
  };
  const url = routes[type];
  if (url) {
    console.log('Navigating to:', url);
    uni.navigateTo({ 
      url,
      success: () => {
        console.log('Navigation success');
      },
      fail: (err) => {
        console.error('Navigation fail:', err);
        uni.showToast({
          title: '页面跳转失败: ' + (err.errMsg || ''),
          icon: 'none'
        });
      }
    });
  }
};

const getAvatarUrl = (url) => {
  return fileApi.getFileUrl(url) || '/static/default-avatar.png';
};

const formatMessage = (content) => {
  if (!content) return '暂无新消息';
  if (content.match(/\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i) || content.startsWith('/uploads/')) {
    return '[图片]';
  }
  return content;
};

const formatTime = (time) => {
  if (!time) return '';
  const dateStr = time.replace('T', ' ');
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  if (diff < 7 * day) return Math.floor(diff / day) + '天前';
  
  return dateStr.substring(0, 10);
};

const markAllRead = () => {
  uni.showToast({ title: '私聊消息暂不支持一键已读', icon: 'none' });
};

const goBack = () => {
  uni.navigateBack();
};

const handleSessionClick = (session) => {
  const otherNickname = encodeURIComponent(session.otherNickname || '');
  const otherAvatar = encodeURIComponent(session.otherAvatar || '');
  uni.navigateTo({
    url: `/pages/message/chat?sessionId=${session.id}&otherUserId=${session.otherUserId}&otherNickname=${otherNickname}&otherAvatar=${otherAvatar}`
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
}

/* 自定义导航栏样式 */
.custom-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);

  .navbar-inner {
    height: 60px; /* 从 44px 增加到 56px */
    display: flex;
    align-items: center;
    padding: 0 0 0 12rpx; /* 左边保持 32rpx，右边缩小到 24rpx */

    .left {
      display: flex;
      align-items: center;
      min-width: 80rpx; /* 缩小左侧宽度以腾出空间 */
    }

    .center {
      flex: 1;
      display: flex;
      justify-content: center;
      
      .brand-text {
        font-size: 38rpx; /* 稍微调大字体 */
        font-weight: 800;
        letter-spacing: 2rpx;
        margin-left: 20rpx; /* 稍微向右偏移，以平衡视觉中心 */
        
        &::after {
          content: 'PULSE';
          color: $pitch-pulse-primary;
        }
      }
    }

    .right {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12rpx; /* 缩小按钮之间的间距 */
      min-width: 180rpx; /* 增加右侧最小宽度，确保按钮不被挤压 */
    }
    
    .icon-btn {
      width: 80rpx; /* 从 64rpx 增加到 80rpx */
      height: 80rpx; /* 从 64rpx 增加到 80rpx */
      border-radius: 20rpx; /* 稍微调圆一点 */
      display: flex;
      justify-content: center;
      align-items: center;
      &:active {
        opacity: 0.7;
      }
    }
  }
}

/* 分类功能区 */
.category-section {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 32rpx;
  background-color: transparent;

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    .icon-wrapper {
      width: 100rpx;
      height: 100rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;

      &:active {
        transform: scale(0.95);
        opacity: 0.8;
      }

      &.bg-emerald { 
        border-color: rgba(16, 185, 129, 0.3);
      }
      &.bg-rose { 
        border-color: rgba(244, 63, 94, 0.3);
      }
      &.bg-sky { 
        border-color: rgba(14, 165, 233, 0.3);
      }
    }

    .label {
      font-size: 24rpx;
      font-weight: 500;
    }
  }
}

.notification-list {
  padding: 0 40rpx 40rpx;
  
  .notification-item {
    display: flex;
    padding: 32rpx 0;
    
    &:active {
      opacity: 0.7;
    }
    
    .avatar-box {
      position: relative;
      margin-right: 24rpx;
      flex-shrink: 0;

      .avatar-img {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50rpx;
      }
    }
    
    .content-box {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .header-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8rpx;
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 12rpx;
          
          .nickname {
            font-size: 30rpx;
            font-weight: 700;
          }
        }
        
        .time {
          font-size: 22rpx;
        }
      }
      
      .message-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20rpx;
        
        .desc-text {
          font-size: 26rpx;
          line-height: 1.4;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .unread-badge {
          min-width: 32rpx;
          height: 32rpx;
          padding: 0 8rpx;
          background-color: $pitch-pulse-primary;
          border-radius: 16rpx;
          color: #000;
          font-size: 20rpx;
          font-weight: bold;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 0 10rpx rgba($pitch-pulse-primary, 0.5);
        }
      }
    }
  }

  .empty-tip {
    padding: 100rpx 0;
    text-align: center;
    font-size: 28rpx;
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>