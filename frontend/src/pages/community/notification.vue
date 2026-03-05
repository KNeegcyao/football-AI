<template>
  <view class="container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="brand-text">PITCHPULSE</text>
        </view>
        <view class="right" :style="{ paddingRight: navbarPaddingRight + 'px' }">
          <view class="icon-btn" @click="markAllRead" title="全部已读">
            <u-icon name="order" color="#fff" size="20"></u-icon>
          </view>
          <view class="icon-btn" title="设置">
            <u-icon name="setting" color="#fff" size="20"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类功能区 -->
    <section class="category-section">
      <view class="category-item" @click="filterByType('reply')">
        <view class="icon-wrapper bg-emerald">
          <u-icon name="chat" color="#10b981" size="28"></u-icon>
        </view>
        <text class="label">回复与@</text>
      </view>
      <view class="category-item" @click="filterByType('like')">
        <view class="icon-wrapper bg-rose">
          <u-icon name="thumb-up" color="#f43f5e" size="28"></u-icon>
        </view>
        <text class="label">收到喜欢</text>
      </view>
      <view class="category-item" @click="filterByType('follow')">
        <view class="icon-wrapper bg-sky">
          <u-icon name="account" color="#0ea5e9" size="28"></u-icon>
        </view>
        <text class="label">新增粉丝</text>
      </view>
    </section>

    <view class="notification-list">
      <view class="notification-item" v-for="(item, index) in sessions" :key="index" @click="handleSessionClick(item)">
        <!-- 头像区域 -->
        <view class="avatar-box">
          <image :src="getAvatarUrl(item.otherAvatar)" class="avatar-img" mode="aspectFill"></image>
        </view>

        <!-- 内容区域 -->
        <view class="content-box">
          <view class="header-row">
            <view class="user-info">
              <text class="nickname">{{ item.otherNickname || '用户' }}</text>
            </view>
            <text class="time">{{ formatTime(item.lastMessageTime) }}</text>
          </view>

          <!-- 消息正文 -->
          <view class="message-row">
            <text class="desc-text">{{ formatMessage(item.lastMessage) }}</text>
            <view class="unread-badge" v-if="item.unreadCount > 0">{{ item.unreadCount }}</view>
          </view>
        </view>
      </view>

      <view class="empty-tip" v-if="sessions.length === 0">
        <text>暂无私聊消息</text>
      </view>
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script>
import { useChatStore } from '@/store/chat';
import { fileApi } from '@/api';
import { mapState, mapActions } from 'pinia';

export default {
  data() {
    return {
      navbarPaddingRight: 16,
      statusBarHeight: 0
    };
  },
  computed: {
    ...mapState(useChatStore, ['sessions'])
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync();
    this.statusBarHeight = systemInfo.statusBarHeight || 0;
    
    this.fetchSessions();

    // #ifdef MP-WEIXIN
    try {
      const menuButton = uni.getMenuButtonBoundingClientRect();
      this.navbarPaddingRight = (systemInfo.screenWidth - menuButton.left) + 8;
    } catch (e) {
      this.navbarPaddingRight = 94;
    }
    // #endif
  },
  onPullDownRefresh() {
    this.fetchSessions().finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  methods: {
    fetchSessions() {
      return useChatStore().fetchSessions();
    },

    filterByType(type) {
      const routes = {
        reply: '/pages/community/reply-detail',
        like: '/pages/community/like-detail',
        follow: '/pages/community/fan-detail'
      };
      const url = routes[type];
      if (url) {
        uni.navigateTo({ url });
      }
    },
    
    getAvatarUrl(url) {
      return fileApi.getFileUrl(url) || '/static/default-avatar.png';
    },

    formatMessage(content) {
      if (!content) return '暂无新消息';
      // 如果消息内容是图片路径，则显示为 [图片]
      if (content.match(/\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i) || content.startsWith('/uploads/')) {
        return '[图片]';
      }
      return content;
    },
    
    formatTime(time) {
      if (!time) return '';
      // 兼容 T 格式
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
    },
    
    markAllRead() {
      // 私聊消息暂不支持一键已读，或者需要后端支持
      uni.showToast({ title: '私聊消息暂不支持一键已读', icon: 'none' });
    },
    
    goBack() {
      uni.navigateBack();
    },
    
    handleSessionClick(session) {
      const otherNickname = encodeURIComponent(session.otherNickname || '');
      const otherAvatar = encodeURIComponent(session.otherAvatar || '');
      uni.navigateTo({
        url: `/pages/message/chat?sessionId=${session.id}&otherUserId=${session.otherUserId}&otherNickname=${otherNickname}&otherAvatar=${otherAvatar}`
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $pitch-pulse-bg-dark;
  color: #fff;
}

/* 自定义导航栏样式 */
.custom-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba($pitch-pulse-bg-dark, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

  .navbar-inner {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 40rpx;

    .left {
      display: flex;
      align-items: center;
      min-width: 80rpx;
    }

    .center {
      flex: 1;
      display: flex;
      justify-content: center;
      
      .brand-text {
        font-size: 34rpx;
        font-weight: 800;
        color: #fff;
        letter-spacing: 2rpx;
        
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
      gap: 20rpx;
      min-width: 80rpx;
    }
    
    .icon-btn {
      width: 64rpx;
      height: 64rpx;
      background-color: rgba(255, 255, 255, 0.05);
      border-radius: 16rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      &:active {
        background-color: rgba(255, 255, 255, 0.1);
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
      background-color: rgba(255, 255, 255, 0.05);
      border: 1rpx solid rgba(255, 255, 255, 0.1);
      transition: all 0.2s;

      &:active {
        transform: scale(0.95);
        background-color: rgba(255, 255, 255, 0.1);
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
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.notification-list {
  padding: 0 40rpx 40rpx;
  
  .notification-item {
    display: flex;
    padding: 32rpx 0;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
    
    &:active {
      background-color: rgba(255, 255, 255, 0.02);
    }
    
    .avatar-box {
      position: relative;
      margin-right: 24rpx;
      flex-shrink: 0;

      .avatar-img {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50rpx;
        border: 1rpx solid rgba(255, 255, 255, 0.1);
        background-color: rgba(255, 255, 255, 0.05);
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
            color: #fff;
          }
        }
        
        .time {
          font-size: 22rpx;
          color: rgba(255, 255, 255, 0.4);
        }
      }
      
      .message-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20rpx;
        
        .desc-text {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.5);
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
    color: rgba(255, 255, 255, 0.3);
    font-size: 28rpx;
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>